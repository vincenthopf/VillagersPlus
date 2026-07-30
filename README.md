# VillagersPlus - a villager extension

## Introduction
This mod adds new villager types, trades and unique and useful workstations.

Currently added professions:
- Horticulturist
- Oceanographer
- Alchemist
- Occultist
- Miner

Currently added workstations:
- **Flower Tub** (Horticulturist): Holds up to 4 small plants or 1 tall plant, including flowers, corals, grass ...
- **Aquarium** (Oceanographer): Holds up to 4 seaplants and 1 bucketable entity. Feed it Fish Food to enlarge the fish, Diet Food to shrink it again, and Calming Food to settle it down. Place more aquariums next to it, to create an even larger one!
- **Alchemist Table**: Converts one potion to three random potions. It has a 2 in 3 chance to explode (configurable).
- **Enchanted Basin** (Occultist): Stores experience. Right-click to take experience, shift right-click to store experience.
- **Ore Grinder** (Miner): A furnace-style ore doubler. Four slots: input, fuel, output and a pickaxe.
  - The pickaxe **material** sets the speed - no pickaxe 800 ticks, wood 600, stone 300, iron 200, diamond 150, netherite 133, gold 100.
  - **Fortune** on the pickaxe adds extra yield on top of the doubling (configurable).
  - The pickaxe takes durability damage per grind and breaks when depleted.
  - A **redstone signal pauses** the machine.

### The Miner's mineshaft
A shaft descends from underneath every miner's house. By default it ends in a real vanilla mineshaft -
corridors, crossings, rails, cobwebs and minecart chests - instead of dead-ending. Turn it off with
`mineshaft_connects_to_vanilla_mineshaft`.

The miner's house generates in plains, savanna and taiga villages.


## Configuration

### Trades
| Option | Default | What it does |
| --- | --- | --- |
| `trade_offers_per_level` | 2 | Max new trades a villager gains per level. |
| `trade_offers_wandering_trader` | 5 | Max trades the wandering trader offers. |
| `trade_price_multiplier_scale` | 1.0 | Scales how strongly prices swing from demand and reputation. `0.0` freezes both. Does **not** change a trade's starting price. |
| `trade_cost_scale` | 1.0 | Multiplies the base cost of every trade (first input slot only). `2.0` doubles all prices. Result is clamped to 1 .. one stack. |
| `enable_conditional_trades` | true | Whether condition-gated trades are actually checked. `false` makes every trade always appear. |
| `enable_time_of_day_pricing` | false | Randomly nudges a trade's cost based on the in-game time it is unlocked. The price is locked in at unlock and does not keep changing. |
| `time_of_day_price_variance` | 0.15 | How far that nudge can go, as a fraction of the cost. Only used when the option above is on. |

### Trading screen controls
All three are **off by default**. When on, **any** player can use them - they are meant for creative
building and testing, not for survival servers.

| Option | Default | What it does |
| --- | --- | --- |
| `allow_trade_reroll` | false | Buttons to re-roll a villager's or wandering trader's trades, all at once or one level at a time. |
| `allow_set_villager_level` | false | A control to set a villager's level (1-5) and regenerate its trades. Wandering traders have no levels and ignore it. |
| `allow_view_all_trades` | false | A button that opens a read-only catalog panel listing every trade a villager could roll at a chosen level, with weights, chances and conditions. |

### Workstations
| Option | Default | What it does |
| --- | --- | --- |
| `max_exp_amount` | 500 | Max experience storable in the Enchanted Basin. |
| `exp_amount` | 50 | Experience taken or given per interaction. |
| `can_explode` | true | Whether the Alchemist Table can explode. |
| `explosion_chance` | 3 | Chance of **not** exploding, as 1 in N. Must be greater than 0. |
| `ore_grinder_output_multiplier` | 1.0 | Scales the Ore Grinder's yields. Defaults: iron/gold/coal/diamond/emerald/quartz ore → 2, copper → 3, lapis/redstone → 8. Never drops below 1 item. |
| `ore_grinder_fortune_enabled` | true | Whether Fortune on the pickaxe adds extra yield. |
| `ore_grinder_pickaxe_damage` | 3 | Durability lost per ground ore. Unbreaking is honoured. |

### Worldgen
| Option | Default | What it does |
| --- | --- | --- |
| `<biome>_<profession>_weight` | 7-15 | Weight of each house in the village pool. Higher generates more often. Biomes: plains, taiga, savanna, snowy, desert. The miner exists for plains, savanna and taiga only. |
| `mineshaft_connects_to_vanilla_mineshaft` | true | Whether the miner's shaft opens into a real vanilla mineshaft. |

Plus a set of `*_flower_in_*` floats that position the plants inside the Flower Tub.


## Datapack Support
### Introduction

VillagerPlus provides full support of customizing trades for:
- VillagerPlus Professions
- Vanilla Villager Professions
- The Wandering Trader

It not only allows to overwrite, modify or delete trades. VillagerPlus also provides more customization of trades.
You can change the currency from emeralds to whatever item you like, for example.

Further VillagersPlus is able to handle **multiple** datapacks modifying the same villager by merging its trades.

### Available datapacks
- Supplementaries datapack for compatability
- Promenade datapack for compatability

### Replacing the default trades for VillagerPlus villagers
Create a datapack with this directory structure:

    data/villagersplus/default_villager_trades

Copy the JSON-file from the VP-villager whose trades you'd like to modify into your datapack. <br>
The default trade files are located in inside the jar-file under:

    data/villagersplus/default_villager_trades

Modify the trades in the JSON as you please. Your datapack JSON will overwrite the default JSON from the VP.

### Replacing the default trades for Minecraft villagers
_Notice: The vanilla trades aren't in JSON-form yet. So modifying those, is a bit more work._

Create a datapack with this directory structure:

    data/villagersplus/default_villager_trades

Create a JSON with the name of villager-profession whose trades you'd like to modify. <br>
For example:

    butcher.json

Below is the basic structure:

    {
        "profession": "minecraft:butcher",
        "trades": {
            "novice": [],
            "apprentice": [],
            "journeyman": [],
            "expert": [],
            "master": []
        }
    }

Add trades as you like.

### Adding trades to villagers
Create a datapack with this directory structure:

    data/villagersplus/villager_trades

Create a JSON with the name of villager-profession to whose trades you'd like to add trades. <br>
For example:

    horticulturist.json

Below is an example for adding a sell-trade. One diamond block is sold for 12 emeralds.

    {
        "profession": "villagersplus:horticulturist",
        "trades": {
            "novice": [
                {
                    "type": "villagersplus:sell_item",
                    "sell": { "item": "diamond_block", "count": 1 },
                    "priceIn": { "item": "emerald", "count": 12 },
                    "max_uses": 12,
                    "villager_experience": 4
                }
            ]
        }
    }

---

## Trade types

Every snippet below is a single trade object. It goes inside one of the five tier arrays
(`novice`, `apprentice`, `journeyman`, `expert`, `master`) of a profession file.

These fields work on **every** type and are left out of the examples to keep them short:

| Field | Meaning |
| --- | --- |
| `max_uses` | How often the trade can be used before it locks. |
| `villager_experience` | Experience the villager gains per use. |
| `price_multiplier` | How strongly reputation and Hero of the Village move this trade's price. |
| `demand` | Seeds vanilla's demand pricing - the cost climbs with use and decays over time. |
| `conditions` / `logic` | See [Conditions](#conditions). |

### `sell_item`
The villager sells an item for a price.

    {
      "type": "villagersplus:sell_item",
      "sell": { "item": "minecraft:oxeye_daisy", "count": 1 },
      "priceIn": { "item": "minecraft:emerald", "count": 2 }
    }

### `buy_item`
The villager buys an item and pays you.

    {
      "type": "villagersplus:buy_item",
      "buy": { "item": "minecraft:wheat_seeds", "count": 4 },
      "reward": { "item": "minecraft:emerald", "count": 1 }
    }

### `sell_tagged_item`
Sells **one random member** of an item tag. The member is chosen when the offer is generated, so
two villagers with this trade will usually sell different things. Vanilla trades match a concrete
item, so this is not an "any of" match.

    {
      "type": "villagersplus:sell_tagged_item",
      "sell": { "tag": "minecraft:saplings", "count": 1 },
      "priceIn": { "item": "minecraft:emerald", "count": 3 }
    }

### `buy_tagged_item`
The same idea for buying.

    {
      "type": "villagersplus:buy_tagged_item",
      "buy": { "tag": "minecraft:flowers", "count": 8 },
      "reward": { "item": "minecraft:emerald", "count": 1 }
    }

### `process_item`
Takes `convertible` plus `priceIn` and returns `sell` - the classic "bring me raw material" trade.

    {
      "type": "villagersplus:process_item",
      "convertible": { "item": "minecraft:dead_tube_coral", "count": 1 },
      "priceIn": { "item": "minecraft:emerald", "count": 5 },
      "sell": { "item": "minecraft:tube_coral", "count": 1 }
    }

### `multi_input`
Two inputs, one output. Vanilla has exactly two buy slots, so one of the two inputs **is** the currency.

    {
      "type": "villagersplus:multi_input",
      "input_a": { "item": "minecraft:emerald", "count": 5 },
      "input_b": { "item": "minecraft:book", "count": 1 },
      "sell": {
        "item": "minecraft:enchanted_book",
        "enchantments": [ { "id": "minecraft:mending", "lvl": 1 } ]
      }
    }

### `weighted_pool`
Picks one of several trades by weight when the offer is generated. Below, the daisy is ten times as
likely as the wither rose.

    {
      "type": "villagersplus:weighted_pool",
      "pool": [
        {
          "weight": 10,
          "trade": {
            "type": "villagersplus:sell_item",
            "sell": { "item": "minecraft:oxeye_daisy", "count": 1 },
            "priceIn": { "item": "minecraft:emerald", "count": 1 }
          }
        },
        {
          "weight": 1,
          "trade": {
            "type": "villagersplus:sell_item",
            "sell": { "item": "minecraft:wither_rose", "count": 1 },
            "priceIn": { "item": "minecraft:emerald", "count": 8 }
          }
        }
      ]
    }

### `sell_potion`
Takes `convertible` plus `priceIn` and returns `sell`, for potion-shaped trades.

    {
      "type": "villagersplus:sell_potion",
      "convertible": { "item": "minecraft:potion", "count": 3 },
      "priceIn": { "item": "minecraft:emerald", "count": 5 },
      "sell": { "item": "minecraft:splash_potion", "count": 7 }
    }

### `sell_map`
Sells a filled map pointing at a structure. `structure_id` is a structure tag.

    {
      "type": "villagersplus:sell_map",
      "structure_id": "minecraft:on_woodland_explorer_maps",
      "name": "filled_map",
      "buy": { "item": "minecraft:compass", "count": 1 },
      "priceIn": { "item": "minecraft:emerald", "count": 2 },
      "price_multiplier": 0.2
    }

### `sell_enchanted_tool`
Sells a tool with a **random** enchantment, vanilla-style. The price scales with what was rolled.

    {
      "type": "villagersplus:sell_enchanted_tool",
      "sell": { "item": "minecraft:diamond_pickaxe", "count": 1 },
      "basePriceIn": { "item": "minecraft:emerald", "count": 2 }
    }

### `sell_specific_enchanted_tool`
Sells a tool with a **fixed** enchantment at a fixed level - a guaranteed result rather than a roll.
`enchantment` defaults to `minecraft:fortune`, `level` to 1.

    {
      "type": "villagersplus:sell_specific_enchanted_tool",
      "sell": { "item": "minecraft:diamond_pickaxe", "count": 1 },
      "basePriceIn": { "item": "minecraft:emerald", "count": 32 },
      "enchantment": "minecraft:efficiency",
      "level": 4
    }

### `sell_enchanted_book`
Sells a randomly enchanted book, vanilla-style.

    {
      "type": "villagersplus:sell_enchanted_book",
      "currency": { "item": "minecraft:emerald" },
      "price_multiplier": 0.2
    }

### `sell_specific_enchanted_book`
Sells a book with a **fixed** enchantment and level. There is no `sell` field - the item is always a
book. `enchantment` defaults to `minecraft:unbreaking`, `level` to 1.

    {
      "type": "villagersplus:sell_specific_enchanted_book",
      "basePriceIn": { "item": "minecraft:emerald", "count": 20 },
      "enchantment": "minecraft:mending",
      "level": 1
    }

### `sell_enchanted_book_from_list`
Sells an enchanted book drawn from a **weighted list** you define, so you control exactly which
enchantments a librarian can offer. Cost is `base_cost + cost_per_level * level`, multiplied by
`treasure_multiplier` for treasure enchantments.

    {
      "type": "villagersplus:sell_enchanted_book_from_list",
      "currency": { "item": "minecraft:emerald" },
      "enchantments": [
        { "id": "minecraft:sharpness",  "min_level": 1, "max_level": 3, "weight": 5 },
        { "id": "minecraft:unbreaking", "min_level": 1, "max_level": 3, "weight": 5 },
        { "id": "minecraft:mending",    "min_level": 1, "max_level": 1, "weight": 1 }
      ],
      "base_cost": 2,
      "cost_per_level": 3,
      "treasure_multiplier": 2,
      "price_multiplier": 0.2
    }

---

## Conditions

Add a `conditions` array to **any** trade to gate it. The default logic is AND; set `"logic": "or"`
on the trade to require only one of them.

**Timing matters.** Conditions are evaluated once, at the moment the offer is generated as the
villager levels up - not continuously. Location conditions (`biome`, `dimension`, `job_site_block`,
`config_flag`) are stable for a settled villager and therefore reliable. World-state conditions
(`weather`, `day_night`, `moon_phase`, `gamerule`) are a snapshot of that moment: a trade gated on
`night` stays available in broad daylight once it has been rolled.

Conditions can be switched off globally with `enable_conditional_trades`.

| Type | Fields | Example |
| --- | --- | --- |
| `biome` | `tag` (string) **or** `biomes` (array) | `{ "type": "biome", "tag": "minecraft:is_ocean" }` |
| `dimension` | `dimension` | `{ "type": "dimension", "dimension": "minecraft:overworld" }` |
| `weather` | `state`: `clear`, `rain`, `thunder` | `{ "type": "weather", "state": "rain" }` |
| `day_night` | `time`: `day` or `night` | `{ "type": "day_night", "time": "night" }` |
| `moon_phase` | `phases` (array of 0-7) | `{ "type": "moon_phase", "phases": [0] }` |
| `config_flag` | `field` (a boolean config field name), optional `value` | `{ "type": "config_flag", "field": "can_explode" }` |
| `gamerule` | `rule` (a boolean gamerule), optional `value` | `{ "type": "gamerule", "rule": "doInsomnia", "value": true }` |
| `job_site_block` | `blocks` (array) **or** `wood_variant` (string matched against the block id) | `{ "type": "job_site_block", "blocks": [ "villagersplus:oceanographer_table" ] }` |

Note that `biome`, `blocks` and `phases` take **arrays**, while `tag`, `dimension` and `wood_variant`
take a single string.

    {
      "type": "villagersplus:sell_item",
      "sell": { "item": "minecraft:heart_of_the_sea", "count": 1 },
      "priceIn": { "item": "minecraft:emerald", "count": 30 },
      "logic": "or",
      "conditions": [
        { "type": "biome", "tag": "minecraft:is_ocean" },
        { "type": "day_night", "time": "night" }
      ]
    }

## Pricing

`price_multiplier` and `demand` work on any trade and feed vanilla's own price machinery:

- `price_multiplier` - how strongly reputation and Hero of the Village move this trade's price.
- `demand` - seeds vanilla's demand tracking. The cost climbs as the trade is used and decays again over time.

Both are scaled server-wide by `trade_price_multiplier_scale` (swing size) and `trade_cost_scale`
(base cost). Reputation and Hero of the Village discounts are applied by vanilla on top.

    {
      "type": "villagersplus:sell_item",
      "sell": { "item": "minecraft:diamond", "count": 1 },
      "priceIn": { "item": "minecraft:emerald", "count": 12 },
      "price_multiplier": 0.1,
      "demand": 10
    }

## Custom items

Any item stack in any trade type accepts this sugar - no special trade type needed:

| Field | Meaning |
| --- | --- |
| `name` | Display name. Plain string or a raw JSON text component. |
| `lore` | Array of lore lines. |
| `enchantments` | Array of `{ "id": ..., "lvl": ... }`. |
| `color` | Hex colour for dyeable armour. |
| `potion` | Potion id for potion items. |
| `skull_owner` | Player name for player heads. |
| `book` | `{ "title": ..., "author": ..., "pages": [...] }` for written books. |
| `nbt` | Raw SNBT, applied **last** as an escape hatch. |

    {
      "type": "villagersplus:sell_item",
      "sell": {
        "item": "minecraft:diamond_sword",
        "name": "{\"text\":\"Excalibur\",\"color\":\"gold\",\"italic\":false}",
        "lore": [ "A blade of legend" ],
        "enchantments": [ { "id": "minecraft:sharpness", "lvl": 5 } ],
        "nbt": "{Unbreakable:1b}"
      },
      "priceIn": { "item": "minecraft:emerald", "count": 40 }
    }

## Wandering trader trades

Create a datapack with this directory structure:

    data/villagersplus/wandering_trader_trades

Any file name works. Instead of the five villager tiers, the wandering trader has two:

- `common` - level 1. Several are picked, the count is set by `trade_offers_wandering_trader`.
- `rare` - level 2. Exactly one is picked.

Set `"replace": true` to drop the vanilla wandering-trader trades and use **only** yours. Omit it
(or set `false`) to add on top of vanilla. Every trade type, the conditions block and pricing all
work here exactly as they do on villagers.

    {
      "replace": false,
      "trades": {
        "common": [
          {
            "type": "villagersplus:sell_item",
            "sell": { "item": "minecraft:glowstone_dust", "count": 4 },
            "priceIn": { "item": "minecraft:emerald", "count": 2 },
            "conditions": [ { "type": "day_night", "time": "night" } ],
            "max_uses": 6,
            "villager_experience": 1
          }
        ],
        "rare": [
          {
            "type": "villagersplus:sell_item",
            "sell": {
              "item": "minecraft:trident",
              "name": "{\"text\":\"Traveler's Trident\",\"color\":\"aqua\",\"italic\":false}",
              "enchantments": [ { "id": "minecraft:loyalty", "lvl": 3 } ],
              "nbt": "{Unbreakable:1b}"
            },
            "priceIn": { "item": "minecraft:emerald", "count": 30 },
            "max_uses": 2,
            "villager_experience": 1
          }
        ]
      }
    }

## Ready-made example files

Inside the jar under `data/villagersplus/available_trade_examples` you will find complete,
copyable files for the cases a snippet cannot show well:

- `full_profession.json` - one profession across all five tiers, mixing many trade types.
- `conditions_and_pricing.json` - conditions and pricing on realistic trades.
- `wandering_trader.json` - a complete wandering trader file.

---

## Trades

#### Horticulturist - Level 1:

* Oxyeye Daisy, Poppy, Allium, Azure Bluet, Cornflower, Lily of the Vally, Dandelion - Buy 1 for 2 Emeralds
* Wheat Seeds, Beetroot Seeds, Melon Seeds, Pumpkin Seeds - Sell 1 for 1 Emerald

#### Horticulturist - Level 2:

* All leaves block - Buy 2 for 4 Emeralds
* Dead Bush - Buy 1 for 3 Sticks, 1 Emerald
* Grass - Sell 4 for 1 Emerald
* Fern - Sell 2 for 1 Emerald

#### Horticulturist - Level 3:

* All tulips - Buy 1 for 4 Emeralds
* All saplings - Sell 2 for 1 Emerald

#### Horticulturist - Level 4:

* Sunflower, Rose Bush, Lilac, Peony - Buy 1 for 5 Emeralds
* Rooted Dirt, Podzol - Sell 2 for 1 Emerald

#### Horticulturist - Level 5:

* Blue Orchid - Buy 1 for 4 Emeralds
* Wither Rose - Buy 1 for 8 Emeralds
* Spore Blossom - Buy 1 for 12 Emeralds
* Flowering Azelea Leaves - Buy 2 for 4 Emeralds
* Moss Block - Sell 2 for 1 Emerald
* Bone Meal - Sell 4 for 1 Emerald

<br>
<br>

#### Occultist - Level 1:

* All Amethyst Buds - Buy 1 for 1, 2, 3 or 5 Emeralds
* Tinted Glass - Buy 4 for 8 Emeralds
* Glow Ink Sac - Sell 1 for 1 Emerald
* Amethyst Shard - Sell 1 for 1 Emerald

#### Occultist - Level 2:

* Soul Lantern - Buy 1 for 4 Emeralds
* Soul Torch - Buy 1 for 2 Emeralds
* Glowstone Dust - Buy 2 for 4 Emeralds
* Soul Sand, Soul Soil - Sell 2 for 1 Emerald

#### Occultist - Level 3:

* All colored candles - Buy 2 for 4 Emeralds

#### Occultist - Level 4:

* Quartz - Buy 2 for 4 Emeralds
* Spectral Arrow - Buy 2 for 2 Emeralds
* Candle - Buy 2 for 4 Emeralds

#### Occultist - Level 5:

* Experience Bottle - Buy 1 for 3 Emeralds
* Poisonous Potato - Sell 1 for 1 Emerald

<br>
<br>

#### Oceanographer - Level 1:

* Sea Pickle - Buy 1 for 3 Emeralds
* Water Bucket - Buy 1 for 5 Emeralds
* Seagrass - Sell 4 for 1 Emerald
* Kelp - Sell 6 for 1 Emerald

#### Oceanographer - Level 2:

* All corals - Buy 1 for 3 Emeralds
* All dead corals - Exchange for living coral + 1 Emerald

#### Oceanographer - Level 3:

* All coral fans - Buy 1 for 3 Emeralds
* All dead coral fans - Exchange for living coral fan + 1 Emerald

#### Oceanographer - Level 4:

* All coral blocks - Buy 1 for 5 Emeralds
* All dead coral blocks - Exchange for living coral block + 1 Emerald

#### Oceanographer - Level 5:

* Nautilus Shell - Buy 1 for 6 Emeralds
* Sponge - Buy 1 for 8 Emeralds
* Scute - Sell 2 for 1 Emerald
* Tropical Fish Bucket, Axototl Bucket, Pufferfish Bucket - Sell 1 for 1 Emerald

<br>
<br>

#### Alchemist - Level 1:

* Sugar, Brown Mushroom, Red Mushroom - Buy 1 for 3 Emeralds
* Nether Wart - Buy 1 for 5 Emeralds
* Glass Bottle - Sell 3 for 1 Emerald

#### Alchemist - Level 2:

* Magma Cream - Buy 1 for 4 Emeralds
* Pufferfish, Fermented Spider Eye - Buy 1 for 5 Emeralds
* All potions - Buy 1 for 5 Emeralds

#### Alchemist - Level 3:

* Blaze Powder, Ghast Tear, Phantom Membrane - Buy 1 for 5 Emeralds
* Gunpowder - Buy 1 for 3 Emeralds
* Ender Pearl, Blaze Rod - Sell 1 for 1 Emerald

#### Alchemist - Level 4:

* All splash potions - Buy 1 for 8 Emeralds

#### Alchemist - Level 5:

* Dragon Breath - Buy 1 for 8 Emeralds
* Glistering Melon Slice - Buy 1 for 4 Emeralds
* Rabbit Foot - Buy 1 for 4 Emeralds

<br>
<br>

#### Miner - Level 1:

* Coal - Sell 15 for 1 Emerald
* Cobblestone - Sell 20 for 1 Emerald
* Torch - Buy 8 for 1 Emerald
* Ladder - Buy 6 for 1 Emerald
* Rail - Buy 4 for 1 Emerald

#### Miner - Level 2:

* Raw Iron - Sell 4 for 1 Emerald
* Raw Iron - Exchange for Iron Ingot + 1 Emerald
* Lantern - Buy 1 for 3 Emeralds
* Minecart - Buy 1 for 4 Emeralds
* Iron Bars - Buy 8 for 2 Emeralds

#### Miner - Level 3:

* Raw Copper - Sell 6 for 1 Emerald
* Cobbled Deepslate - Buy 8 for 1 Emerald
* Tuff - Buy 8 for 1 Emerald
* Powered Rail - Buy 2 for 3 Emeralds
* Chain - Buy 4 for 2 Emeralds
* **Iron Pickaxe with Efficiency II** - Buy 1 for 9 Emeralds

#### Miner - Level 4:

* Raw Gold - Sell 4 for 1 Emerald
* Glowstone - Buy 4 for 3 Emeralds
* TNT - Buy 1 for 8 Emeralds

#### Miner - Level 5:

* Diamond - Buy 1 for 10 Emeralds
* **Diamond Pickaxe with Efficiency IV** - Buy 1 for 32 Emeralds
* Amethyst Shard - Buy 1 for 1 Emerald
* Obsidian - Buy 2 for 3 Emeralds
* Ancient Debris - Sell 1 for 8 Emeralds
