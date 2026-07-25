package com.lion.villagersplus.tradeoffers;

import com.lion.villagersplus.VillagersPlus;
import com.google.gson.*;
import com.lion.villagersplus.tradeoffers.conditions.TradeConditions;
import com.lion.villagersplus.tradeoffers.trades.*;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TradeOfferManager {
    public static final Map<String, Integer> professionMapping = new HashMap<>();
    public static final Map<Identifier, JsonTradeOffer> tradeOfferRegistry = new HashMap<>();

    static {
        professionMapping.put("novice", 1);
        professionMapping.put("apprentice", 2);
        professionMapping.put("journeyman", 3);
        professionMapping.put("expert", 4);
        professionMapping.put("master", 5);
    }

    public static void registerTradeOffers() {
        VillagersPlus.LOGGER.info("Registered JSON trade offer adapter.");
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_item"), new JsonSellItemTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"buy_item"), new JsonBuyItemTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"process_item"), new JsonProcessItemTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_potion"), new JsonSellPotionTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_enchanted_tool"), new JsonSellEnchantedToolTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_specific_enchanted_tool"), new JsonSellSpecificEnchantedToolTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_enchanted_book"), new JsonSellEnchantedBookTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_specific_enchanted_book"), new JsonSellSpecificEnchantedBookTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_map"), new JsonSellStructureMapTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"weighted_pool"), new JsonWeightedPoolTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"buy_tagged_item"), new JsonBuyTaggedItemTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_tagged_item"), new JsonSellTaggedItemTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"sell_enchanted_book_from_list"), new JsonSellEnchantedBookFromListTradeOffer());
        tradeOfferRegistry.put(new Identifier(VillagersPlus.MOD_ID,"multi_input"), new JsonMultiInputTradeOffer());
    }

    public static void deserializeJson(JsonObject jsonRoot) {
        Identifier professionId = Identifier.tryParse(jsonRoot.get("profession").getAsString());

        Registries.VILLAGER_PROFESSION
                .getOrEmpty(professionId)
                .ifPresent(villagerProfession
                        -> deserializeTrades(jsonRoot, (integer, factory)
                        -> TradeOfferRegistryLoader.registerVillagerTrade(villagerProfession, integer, factory)));
    }

    private static void deserializeTrades(@NotNull JsonObject jsonRoot, BiConsumer<Integer, TradeOffers.Factory> tradeConsumer) {
        for (Map.Entry<String, JsonElement> entry : jsonRoot.get("trades").getAsJsonObject().entrySet()) {

            int level = professionMapping.get(entry.getKey());
            JsonArray tradesArray = entry.getValue().getAsJsonArray();

            for (JsonElement tradeElement : tradesArray) {
                JsonObject trade = tradeElement.getAsJsonObject();
                TradeOffers.Factory factory = deserializeTrade(trade);

                if (factory == null) {
                    VillagersPlus.LOGGER.error("Trade type: " + trade.get("type").getAsString() + " is broken.");
                    VillagersPlus.LOGGER.error("Error in deserializing trades." +
                            "Trade element: " + tradeElement + " and " +
                            "Trade: " + trade + " in " + tradesArray + " is broken. \n" +
                            "Sending faulty JSON: " + jsonRoot);
                } else {
                    tradeConsumer.accept(level, factory);
                }

            }
        }
    }

    /**
     * Central choke-point every trade flows through — both top-level trades and nested ones
     * (e.g. inside {@code weighted_pool}). Deserializes the adapter, then layers the cross-cutting
     * {@code conditions} block and global pricing on top so those features apply to every type
     * without editing individual adapters. Returns {@code null} when the trade type is unknown.
     */
    @Nullable
    public static TradeOffers.Factory deserializeTrade(JsonObject trade) {
        JsonTradeOffer adapter = tradeOfferRegistry.get(Identifier.tryParse(trade.get("type").getAsString()));
        if (adapter == null) {
            return null;
        }

        TradeOffers.Factory factory = adapter.deserialize(trade);

        if (VillagersPlus.CONFIG.enable_conditional_trades && trade.has("conditions")) {
            boolean orLogic = "or".equalsIgnoreCase(readString(trade, "logic"));
            factory = new ConditionalTradeFactory(factory,
                    TradeConditions.parse(trade.getAsJsonArray("conditions"), orLogic));
        }

        return PricingTradeFactory.wrapIfNeeded(factory, trade);
    }

    private static String readString(JsonObject object, String key) {
        return object.has(key) ? object.get(key).getAsString() : "";
    }
}
