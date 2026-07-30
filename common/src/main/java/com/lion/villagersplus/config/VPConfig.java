package com.lion.villagersplus.config;

import com.lion.villagersplus.config.annotations.Description;

public class VPConfig implements Config {

    @Override
    public String getName() {
        return "villagersplus-3.0-config";
    }

    @Override
    public String getExtension() {
        return "json5";
    }

    @Override
    public String getDirectory() {
        return "villagersplus";
    }

    @Description("Amount of new trades per level a villager can have at max. (Default: 2)")
    public int trade_offers_per_level = 2;
    @Description("Amount of trades the wandering trader can have at max. (Default: 5)")
    public int trade_offers_wandering_trader = 5;

    @Description("Controls how strongly trade prices change over time. When a villager is used a lot its prices rise (demand), and trading builds reputation which lowers prices; this value scales how big both of those swings are for ALL trades at once. 1.0 = normal. 2.0 = prices rise and fall twice as fast. 0.0 = prices never move from demand or reputation. It does NOT change the starting price of a trade. (Default: 1.0)")
    public float trade_price_multiplier_scale = 1.0F;

    @Description("Multiplies the base cost of every trade, i.e. the amount in the first input slot. 1.0 = unchanged. 2.0 = everything is twice as expensive (a 5 emerald trade becomes 10). 0.5 = half price. The result is rounded and never goes below 1 or above a full stack. Only the first input slot is affected. (Default: 1.0)")
    public float trade_cost_scale = 1.0F;

    @Description("Some trades in the data files are set to only appear under certain conditions (for example a specific biome, dimension, or time of day). true = these conditions are checked, so a gated trade can be hidden when its condition is not met. false = conditions are ignored and every trade always shows up. (Default: true)")
    public boolean enable_conditional_trades = true;

    @Description("When true, a trade's cost is randomly raised or lowered a little based on the in-game time when that trade is first unlocked. Important: a price is locked in the moment a villager unlocks the trade (on level up) and stays fixed after that, so it does not keep changing through the day. The size of the change is set by time_of_day_price_variance below. (Default: false)")
    public boolean enable_time_of_day_pricing = false;

    @Description("Only used when enable_time_of_day_pricing is true. Sets how far the time-based price change can go, as a fraction of the cost. 0.15 = up to 15% cheaper or more expensive. 0.0 = no change. (Default: 0.15)")
    public float time_of_day_price_variance = 0.15F;

    @Description("Adds buttons to the trading screen to reset and re-roll a villager's or wandering trader's trades (all of them, or just one level). When true, ANY player can use it. (Default: false)")
    public boolean allow_trade_reroll = false;

    @Description("Adds a control to the trading screen to set a villager's level (1-5). Regenerates the villager's trades for the new level. Wandering traders have no levels and ignore it. When true, ANY player can use it. (Default: false)")
    public boolean allow_set_villager_level = false;

    @Description("Adds a button to the trading screen that opens a read-only catalog of all possible trades for a chosen level, so you can see what a villager could offer. When true, ANY player can use it. (Default: false)")
    public boolean allow_view_all_trades = false;

    @Description("Max amount of experience storable in the enchanted basin. (Default: 500)")
    public int max_exp_amount = 500;

    @Description("Amount of experience taken or given to the enchanted basin per interaction. (Default: 50)")
    public int exp_amount = 50;

    @Description("Enable explosion potential of the alchemist workstation. (Default: true)")
    public boolean can_explode = true;

    @Description("Chance of the alchemist workstation NOT exploding (1 in ?). Must be greater than 0! (Default: 3)")
    public int explosion_chance = 3;

    @Description("Weight of the house in the village structure pool. Higher values increase the chance of generating.")
    public int plains_alchemist_weight = 10;
    public int plains_occultist_weight = 10;
    public int plains_horticulturist_weight = 7;
    public int plains_oceanographer_weight = 15;
    public int plains_miner_weight = 25;

    @Description("Weight of the house in a village. Higher values increase the chance of generating.")
    public int taiga_alchemist_weight = 10;
    public int taiga_occultist_weight = 10;
    public int taiga_horticulturist_weight = 7;
    public int taiga_oceanographer_weight = 15;
    public int taiga_miner_weight = 25;

    @Description("Weight of the house in a village. Higher values increase the chance of generating.")
    public int savanna_alchemist_weight = 10;
    public int savanna_occultist_weight = 10;
    public int savanna_horticulturist_weight = 7;
    public int savanna_oceanographer_weight = 15;
    public int savanna_miner_weight = 25;

    @Description("Weight of the house in a village. Higher values increase the chance of generating.")
    public int snowy_alchemist_weight = 10;
    public int snowy_occultist_weight = 10;
    public int snowy_horticulturist_weight = 7;
    public int snowy_oceanographer_weight = 15;

    @Description("Weight of the house in a village. Higher values increase the chance of generating.")
    public int desert_alchemist_weight = 10;
    public int desert_occultist_weight = 10;
    public int desert_horticulturist_weight = 7;
    public int desert_oceanographer_weight = 15;

    @Description("When true, the mineshaft below the miner's house ends in a real vanilla mineshaft instead of just dead-ending. (Default: true)")
    public boolean mineshaft_connects_to_vanilla_mineshaft = true;

    @Description("Scales the ore grinder's base yields. 1.0 keeps the defaults: iron/gold/coal/diamond/emerald/quartz ore -> 2, copper -> 3, lapis/redstone -> 8. Examples: 0.5 halves everything (iron ore -> 1 raw iron), 2.0 doubles it (iron ore -> 4 raw iron). Minimum result is always 1 item. (Default: 1.0)")
    public float ore_grinder_output_multiplier = 1.0F;

    @Description("Whether Fortune on the inserted pickaxe additionally increases the ore grinder's yield. (Default: true)")
    public boolean ore_grinder_fortune_enabled = true;

    @Description("Durability the inserted pickaxe loses per ground ore. Unbreaking is honored per durability point. (Default: 3)")
    public int ore_grinder_pickaxe_damage = 3;

    @Description("Two flower positions in the flower tub (absolute X/Z offset from the block, applied per plant).")
    public float first_flower_in_two_X = 0.15F;
    public float first_flower_in_two_Z = 0.15F;

    public float second_flower_in_two_X = -0.15F;
    public float second_flower_in_two_Z = -0.20F;

    @Description("Three flower positions in the flower tub (absolute X/Z offset from the block, applied per plant).")
    public float first_flower_in_three_X = 0.15F;
    public float first_flower_in_three_Z = 0F;

    public float second_flower_in_three_X = -0.15F;
    public float second_flower_in_three_Z = -0.15F;

    public float third_flower_in_three_X = -0.20F;
    public float third_flower_in_three_Z = 0.15F;

    @Description("Four flower positions in the flower tub (absolute X/Z offset from the block, applied per plant).")
    public float first_flower_in_four_X = 0.15F;
    public float first_flower_in_four_Z = 0.15F;

    public float second_flower_in_four_X = 0.15F;
    public float second_flower_in_four_Z = -0.20F;

    public float third_flower_in_four_X = -0.15F;
    public float third_flower_in_four_Z = -0.20F;

    public float forth_flower_in_four_X = -0.20F;
    public float forth_flower_in_four_Z = 0.17F;


}


