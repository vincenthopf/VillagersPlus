package com.lion.villagersplus.tradeoffers.trades;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lion.villagersplus.tradeoffers.TradeOfferManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A rarity wrapper: picks one of N sub-trades by weight at create() time. Each sub-trade is a full
 * trade object routed back through {@link TradeOfferManager#deserializeTrade}, so nested conditions
 * and pricing apply automatically. A picked sub-trade may itself yield {@code null} (e.g. its own
 * condition failed) — that is the intended rarity behaviour.
 *
 * <pre>
 * { "type": "villagersplus:weighted_pool",
 *   "pool": [
 *     { "weight": 10, "trade": { "type": "villagersplus:sell_item", ... } },
 *     { "weight": 1,  "trade": { "type": "villagersplus:sell_enchanted_book_from_list", ... } } ] }
 * </pre>
 */
public class JsonWeightedPoolTradeOffer extends JsonTradeOffer {

    @Override
    @NotNull
    public TradeOffers.Factory deserialize(JsonObject json) {
        List<Entry> entries = new ArrayList<>();
        int totalWeight = 0;
        for (JsonElement element : json.getAsJsonArray("pool")) {
            JsonObject entry = element.getAsJsonObject();
            int weight = readInt(entry, "weight", 1);
            TradeOffers.Factory factory = TradeOfferManager.deserializeTrade(entry.getAsJsonObject("trade"));
            if (factory != null && weight > 0) {
                entries.add(new Entry(weight, factory));
                totalWeight += weight;
            }
        }
        return new Factory(entries, totalWeight);
    }

    private record Entry(int weight, TradeOffers.Factory factory) {
    }

    private static class Factory implements TradeOffers.Factory {
        private final List<Entry> entries;
        private final int totalWeight;

        public Factory(List<Entry> entries, int totalWeight) {
            this.entries = entries;
            this.totalWeight = totalWeight;
        }

        @Override
        public TradeOffer create(Entity entity, Random random) {
            if (entries.isEmpty() || totalWeight <= 0) {
                return null;
            }
            int roll = random.nextInt(totalWeight);
            for (Entry entry : entries) {
                roll -= entry.weight();
                if (roll < 0) {
                    return entry.factory().create(entity, random);
                }
            }
            return null;
        }
    }
}
