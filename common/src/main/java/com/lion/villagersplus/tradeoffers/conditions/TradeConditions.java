package com.lion.villagersplus.tradeoffers.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lion.villagersplus.VillagersPlus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Registry and parser for the cross-cutting {@code "conditions"} block that any trade may carry.
 * A condition is JSON {@code {"type": "...", ...params}}. {@link #parse} returns the combined list,
 * evaluated with AND semantics by default or OR when the array is preceded by a {@code logic}
 * marker (see {@link #parse}). An unknown condition type fails closed (the trade is withheld) with
 * a loud log so typos are visible instead of silently disabling gating.
 */
public final class TradeConditions {

    private static final Map<String, Function<JsonObject, TradeCondition>> REGISTRY = new HashMap<>();
    private static final Map<String, GameRules.Key<GameRules.BooleanRule>> BOOLEAN_RULES = new HashMap<>();

    static {
        GameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
            }

            @Override
            public void visitBoolean(GameRules.Key<GameRules.BooleanRule> key, GameRules.Type<GameRules.BooleanRule> type) {
                BOOLEAN_RULES.put(key.getName(), key);
            }
        });
        registerBuiltins();
    }

    private TradeConditions() {
    }

    public static void register(String type, Function<JsonObject, TradeCondition> factory) {
        REGISTRY.put(type, factory);
    }

    /**
     * Parses the {@code conditions} array into a single combined {@link TradeCondition}. Default
     * logic is AND; pass {@code "logic": "or"} on the owning trade object to switch (read separately
     * — see {@link #parse(JsonArray, boolean)}).
     */
    public static TradeCondition parse(JsonArray conditions) {
        return parse(conditions, false);
    }

    public static TradeCondition parse(JsonArray conditions, boolean orLogic) {
        List<TradeCondition> parsed = new ArrayList<>();
        for (JsonElement element : conditions) {
            JsonObject obj = element.getAsJsonObject();
            String type = stripNamespace(obj.get("type").getAsString());
            Function<JsonObject, TradeCondition> factory = REGISTRY.get(type);
            if (factory == null) {
                VillagersPlus.LOGGER.error("Unknown trade condition type: " + type + " -- this trade will be withheld.");
                parsed.add(villager -> false); // fail closed
                continue;
            }
            parsed.add(factory.apply(obj));
        }

        List<TradeCondition> finalList = parsed;
        if (orLogic) {
            return villager -> {
                for (TradeCondition c : finalList) {
                    if (c.test(villager)) return true;
                }
                return finalList.isEmpty();
            };
        }
        return villager -> {
            for (TradeCondition c : finalList) {
                if (!c.test(villager)) return false;
            }
            return true;
        };
    }

    private static String stripNamespace(String type) {
        int colon = type.indexOf(':');
        return colon >= 0 ? type.substring(colon + 1) : type;
    }

    private static void registerBuiltins() {
        register("biome", TradeConditions::parseBiome);
        register("dimension", TradeConditions::parseDimension);
        register("weather", TradeConditions::parseWeather);
        register("day_night", TradeConditions::parseDayNight);
        register("moon_phase", TradeConditions::parseMoonPhase);
        register("config_flag", TradeConditions::parseConfigFlag);
        register("gamerule", TradeConditions::parseGamerule);
        register("job_site_block", TradeConditions::parseJobSite);
    }

    // --- Built-in condition parsers ---------------------------------------------------------

    private static TradeCondition parseBiome(JsonObject json) {
        if (json.has("tag")) {
            TagKey<net.minecraft.world.biome.Biome> tag = TagKey.of(RegistryKeys.BIOME,
                    Identifier.tryParse(json.get("tag").getAsString()));
            return villager -> villager.getWorld().getBiome(villager.getBlockPos()).isIn(tag);
        }
        Set<String> biomes = toStringSet(json.getAsJsonArray("biomes"));
        return villager -> villager.getWorld().getBiome(villager.getBlockPos()).getKey()
                .map(key -> biomes.contains(key.getValue().toString()))
                .orElse(false);
    }

    private static TradeCondition parseDimension(JsonObject json) {
        String dimension = json.get("dimension").getAsString();
        return villager -> villager.getWorld().getRegistryKey().getValue().toString().equals(dimension);
    }

    private static TradeCondition parseWeather(JsonObject json) {
        String state = json.get("state").getAsString();
        return villager -> {
            World world = villager.getWorld();
            return switch (state) {
                case "thunder" -> world.isThundering();
                case "rain" -> world.isRaining();
                default -> !world.isRaining(); // "clear"
            };
        };
    }

    private static TradeCondition parseDayNight(JsonObject json) {
        boolean wantDay = "day".equals(json.get("time").getAsString());
        return villager -> villager.getWorld().isDay() == wantDay;
    }

    private static TradeCondition parseMoonPhase(JsonObject json) {
        Set<Integer> phases = new HashSet<>();
        for (JsonElement element : json.getAsJsonArray("phases")) {
            phases.add(element.getAsInt());
        }
        return villager -> phases.contains(villager.getWorld().getMoonPhase());
    }

    private static TradeCondition parseConfigFlag(JsonObject json) {
        String fieldName = json.get("field").getAsString();
        boolean expected = !json.has("value") || json.get("value").getAsBoolean();
        return villager -> {
            try {
                Field field = VillagersPlus.CONFIG.getClass().getField(fieldName);
                return field.getBoolean(VillagersPlus.CONFIG) == expected;
            } catch (ReflectiveOperationException e) {
                VillagersPlus.LOGGER.error("config_flag condition references unknown boolean field: " + fieldName, e);
                return false;
            }
        };
    }

    private static TradeCondition parseGamerule(JsonObject json) {
        String ruleName = json.get("rule").getAsString();
        boolean expected = !json.has("value") || json.get("value").getAsBoolean();
        GameRules.Key<GameRules.BooleanRule> key = BOOLEAN_RULES.get(ruleName);
        if (key == null) {
            VillagersPlus.LOGGER.error("gamerule condition references unknown boolean rule: " + ruleName);
            return villager -> false;
        }
        return villager -> villager.getWorld().getGameRules().getBoolean(key) == expected;
    }

    private static TradeCondition parseJobSite(JsonObject json) {
        if (json.has("wood_variant")) {
            String variant = json.get("wood_variant").getAsString();
            return villager -> jobSiteBlockId(villager)
                    .map(id -> id.getPath().contains(variant))
                    .orElse(false);
        }
        Set<String> blocks = toStringSet(json.getAsJsonArray("blocks"));
        return villager -> jobSiteBlockId(villager)
                .map(id -> blocks.contains(id.toString()))
                .orElse(false);
    }

    private static Optional<Identifier> jobSiteBlockId(Entity villager) {
        if (!(villager instanceof VillagerEntity villagerEntity)) {
            return Optional.empty();
        }
        Optional<GlobalPos> jobSite = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.JOB_SITE);
        if (jobSite.isEmpty()) {
            return Optional.empty();
        }
        GlobalPos pos = jobSite.get();
        if (!pos.getDimension().equals(villager.getWorld().getRegistryKey())) {
            return Optional.empty();
        }
        return Optional.of(net.minecraft.registry.Registries.BLOCK.getId(
                villager.getWorld().getBlockState(pos.getPos()).getBlock()));
    }

    private static Set<String> toStringSet(JsonArray array) {
        Set<String> set = new HashSet<>();
        for (JsonElement element : array) {
            set.add(element.getAsString());
        }
        return set;
    }
}
