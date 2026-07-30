package com.lion.villagersplus.tradeoffers.gui;

/**
 * Duck interface implemented by merchant entities (via mixin) so the trade-screen controls can
 * reset/re-roll trades and set levels without a networking layer. The server-side handler mixin
 * casts the {@code Merchant} entity to this and calls the actions.
 */
public interface TradeControl {

    /** Clears and regenerates every trade tier the merchant currently has. */
    void villagersplus$rerollAll();

    /** Re-rolls only the given tier's trades, keeping the others. Traders without levels re-roll all. */
    default void villagersplus$rerollLevel(int level) {
        villagersplus$rerollAll();
    }

    /** Whether this merchant has villager-style levels (so "set level" applies). */
    default boolean villagersplus$supportsLevels() {
        return false;
    }

    /** Sets the merchant's level (1-5) and regenerates its trades. No-op for level-less merchants. */
    default void villagersplus$setLevel(int level) {
    }
}
