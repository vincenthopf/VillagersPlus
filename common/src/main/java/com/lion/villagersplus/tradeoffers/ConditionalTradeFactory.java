package com.lion.villagersplus.tradeoffers;

import com.lion.villagersplus.tradeoffers.conditions.TradeCondition;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;

/**
 * Wraps another trade factory and yields its offer only when the condition passes. Returning
 * {@code null} plugs directly into vanilla's existing null-filtering during offer selection, so a
 * failed condition simply means the trade is not offered by that villager.
 */
public final class ConditionalTradeFactory implements TradeOffers.Factory {

    private final TradeOffers.Factory delegate;
    private final TradeCondition condition;

    public ConditionalTradeFactory(TradeOffers.Factory delegate, TradeCondition condition) {
        this.delegate = delegate;
        this.condition = condition;
    }

    @Override
    public TradeOffer create(Entity entity, Random random) {
        if (!condition.test(entity)) {
            return null;
        }
        return delegate.create(entity, random);
    }
}
