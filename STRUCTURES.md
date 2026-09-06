# CozyCrazyCraft Quest ↔ Structure Browser

This page shows where current quest concepts can already attach to **real CozyCrazyZones 0.3.4 structure rules**, and where they are still waiting on the world layer.

For the full bytecode-audited zoning contract, see [`docs/COZY_ZONES_API_0_3_4.md`](docs/COZY_ZONES_API_0_3_4.md) and [`data/world_bindings/cozy_zones_0_3_4.json`](data/world_bindings/cozy_zones_0_3_4.json).

Status:

- **Explicit** — current CozyCrazyZones has a direct/prefix rule that fits the quest.
- **Generic-rule** — structure is explicitly allowed from this radial tier outward, but the quest locator must still select an instance in the issuing board's macro-region.
- **Unbound** — loaded structure/content exists, but 0.3.4 does not yet provide a regional rule strong enough for a required regional quest.
- **Future** — encounter/structure is intentionally not integrated yet.

> A structure being legal does not mean it exists near the board. The future locator must still find a real generated instance and reject the quest if no sensible target exists.

---

# Shared Core / Hearthlands

| Quest idea | Candidate target | Status | Notes |
|---|---|---|---|
| **The Watchman's Token** | `dungeons_enhanced:watch_tower` | Generic-rule | Explicit Hearthlands minimum; strong first proof-item candidate. |
| **The Missing Courier** | `valhelsia_structures:tower_ruin` | Generic-rule | Hearthlands minimum; use only if a sensible nearby ruin exists. |
| first generic dungeon recovery | `dungeons_enhanced:dungeon_variant` | Generic-rule | Good fallback local adventure. |
| spider trouble | `betterdungeons:spider_dungeon` | Generic-rule | Useful if we want a mapped spider-dungeon contract rather than just a kill bounty. |
| local spawner recovery | `valhelsia_structures:spawner_dungeon` | Generic-rule | Hearthlands legal; reward must stay modest. |
| observation/survey job | `born_in_chaos_v1:observation_tower_*` | Generic-rule | Prefix rule explicitly starts in Hearthlands. |

---

# Frostmarch — North

## Hearthlands / Frontier

| Quest idea | Candidate target | Status | Notes |
|---|---|---|---|
| **A Marker in the Snow** | `dungeons_enhanced:watch_tower` or `born_in_chaos_v1:observation_tower_*` | Generic-rule | Locator must choose a Frostmarch/transition instance; no North-specific H1 tower rule exists. |
| **The Trapper's Satchel** | `valhelsia_structures:tower_ruin` | Generic-rule | Same-region target selection required. |
| **The Missing Expedition Journal** | `valhelsia_structures:castle_ruin` or another audited Frontier ruin | Generic-rule | Current 0.3.4 has no strongly North-specific Frontier structure rule. |

## Wildlands

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Sleeping Mountain** | `mowziesmobs:frostmaw_spawn` | **Explicit** | NORTH + WILDLANDS + ESTABLISHED. Clean regional Great Hunt anchor. |
| frozen-ruin recovery | `dungeons_enhanced:ice_pit` | **Explicit** | NORTH + WILDLANDS + ESTABLISHED. Excellent proof-item expedition target. |
| ancient sideboss | `mowziesmobs:wrought_chamber` | Generic-rule | Wildlands minimum, intentionally not North-exclusive. |

## Dread Reaches

| Quest | Target | Status | Notes |
|---|---|---|---|
| **Cornelia's Wake / The Captain in the Ice** | Aquamirae Ice Maze / Cornelia destination | **Unbound** | Aquamirae structures are loaded, but current ZoneRuleRegistry has no explicit North-Dread structure rule. Wait for developer certification/path. |

---

# Greenveil — East

## Hearthlands

| Quest idea | Candidate target | Status | Notes |
|---|---|---|---|
| **The Naturalist's Satchel** | low-tier local ruin/tower | Generic-rule | No strongly East-specific H1 structure is explicitly bound yet. Keep flexible. |

## Frontier

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Shrine Under Vines** | `betterjungletemples:jungle_temple` | **Explicit** | EAST + FRONTIER + ESTABLISHED. Excellent proof-item target. |
| alternate overgrown recovery | `dungeons_enhanced:jungle_monument` | **Explicit** | EAST + FRONTIER + ESTABLISHED. Useful second structure family. |

## Wildlands

| Quest | Target | Status | Notes |
|---|---|---|---|
| **Temple of Eight Roots** | `betterjungletemples:jungle_temple` or `dungeons_enhanced:jungle_monument` in Wildlands | **Explicit** | Rules are minimum Frontier, so they remain legal outward. Locator should demand a Wildlands instance for the T3 contract. |
| Wroughtnaut side contract | `mowziesmobs:wrought_chamber` | Generic-rule | Good non-regional sideboss option. |

## Dread Reaches

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Thing Beneath the Canopy** | future Jungle Abomination arena | **Future** | Intentionally not integrated yet. Do not invent a stand-in structure. |

---

# Sunscar — South

Sunscar currently has the strongest explicit structure ladder in CozyCrazyZones 0.3.4.

## Hearthlands

| Quest idea | Target | Status | Notes |
|---|---|---|---|
| early desert-site recovery | `dungeons_enhanced:desert_tomb` | **Explicit** | SOUTH + HEARTHLANDS + CARDINAL_TRANSITION. Nearly perfect first-village regional adventure if the first village is Sunscar transition. |
| **The Watering-Hole Ledger** | desert tomb or another nearby Sunscar H1 site | Explicit candidate | Story wording may need adjustment if desert tomb becomes the exact target. |

## Frontier

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The First Dig** | `betterdeserttemples:desert_temple` | **Explicit** | SOUTH + FRONTIER + ESTABLISHED. Strong archaeology contract. |
| alternate archaeology/recovery | `dungeons_enhanced:desert_temple` | **Explicit** | SOUTH + FRONTIER + ESTABLISHED. Gives variety without leaving region identity. |

## Wildlands

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Sunbird** | `mowziesmobs:umvuthana_grove` | **Explicit** | SOUTH + WILDLANDS + ESTABLISHED. Clean Umvuthi Great Hunt anchor. |
| deep archaeology side expedition | outward desert temple instance | Explicit minimum rule | A Frontier-minimum temple can still be selected in Wildlands if the locator intentionally asks for an outward instance. |

## Dread Reaches

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Cursed Inscription** | `cataclysm:cursed_pyramid` | **Explicit** | SOUTH + DREAD_REACHES + ESTABLISHED. Cataclysm namespace is otherwise suppressed. |
| **Guardian in the Sand** | Cursed Pyramid / Wadjet | **Explicit destination** | Entity is inside the isolated pyramid package; do not treat Cataclysm as normal natural ecology. |
| **The Arena Below** | Cursed Pyramid / Kobolediator | **Explicit destination** | Same isolation rule. |
| **The Ancient Remnant** | `cataclysm:cursed_pyramid` | **Explicit** | Strongest currently bound regional finale. |

---

# Harvestlands — West

## Hearthlands

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Empty Stalls** | `dungeons_enhanced:stables` | Generic-rule | Explicit Hearthlands structure. Locator must find the specific Stable in Harvestlands/transition if the quest is issued there. This remains one of our best first-adventure contracts. |

## Frontier

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Gravekeeper's Bell** | Born in Chaos `grave_*` family | **Unbound** | Graves are loaded but current ZoneRuleRegistry does not make them West/Frontier. Do not hard-bind yet. |
| **The Old Plow** | farm/ruin site | Unbound/generic | Needs a specific target after structure classification. |

## Wildlands

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Headless Road** | `born_in_chaos_v1:sir_pumpkinhead` | **Explicit entity rule** | WEST + WILDLANDS + ESTABLISHED natural rule. Great Hunt can use the entity objective; map/location method still separate. |
| **Mansion Correspondence** | woodland mansion or another old-forest major structure | Unbound/biome-driven | Current ZoneRuleRegistry does not explicitly bind vanilla mansion to West. Await biome certification/export if it is to be region-required. |

## Dread Reaches

| Quest | Target | Status | Notes |
|---|---|---|---|
| **The Last Road West** | `born_in_chaos_v1:dark_tower_*` | **Explicit** | Prefix rule is WEST + DREAD_REACHES + ESTABLISHED. Strong deep-West story/lead structure. |
| **The Old Harvest** | Lord Pumpkinhead encounter | **Unbound** | Lord entity exists in registry, but current zoning rule does not define the final encounter path. Need exact authored/structure route before live questing. |

---

# Strong generic structures for optional side work

These are useful because their zoning rules are radial rather than cardinal. A locator can use the board's region as an additional search constraint without pretending the structure itself is region-exclusive.

### Hearthlands+

- `dungeons_enhanced:stables`
- `dungeons_enhanced:dungeon_variant`
- `dungeons_enhanced:watch_tower`
- `dungeons_enhanced:witch_tower`
- `dungeons_enhanced:sunken_shrine`
- `betterdungeons:spider_dungeon`
- `valhelsia_structures:spawner_dungeon`
- `valhelsia_structures:tower_ruin`
- `born_in_chaos_v1:observation_tower_*`

### Frontier+

- `dungeons_enhanced:pillager_camp`
- `dungeons_enhanced:tall_witch_hut`
- `dungeons_enhanced:tower_of_the_undead`
- `dungeons_enhanced:castle`
- `dungeons_enhanced:pirate_ship`
- `dungeons_enhanced:large_dungeon`
- `betterdungeons:skeleton_dungeon`
- `betterdungeons:zombie_dungeon`
- `valhelsia_structures:forge`
- `valhelsia_structures:castle`
- `valhelsia_structures:castle_ruin`
- `born_in_chaos_v1:firewell`
- `born_in_chaos_v1:mound_of_hounds`

### Wildlands+

- `dungeons_enhanced:deep_crypt`
- `dungeons_enhanced:monster_maze`
- `dungeons_enhanced:elders_temple`
- `dungeons_enhanced:flying_dutchman`
- `mowziesmobs:wrought_chamber`
- `mowziesmobs:monastery`

These generic structures are especially useful for unrelated side adventures so the four regional stories do not become overly symmetrical or railroaded.
