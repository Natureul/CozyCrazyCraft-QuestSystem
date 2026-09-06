# CozyCrazyCraft Quest ↔ Structure Browser

This page is the human-readable structure side of the quest system, refreshed against **CozyCrazyZones 0.3.6**.

> A structure being legal in a region/tier does **not** mean a board may automatically send the player there. The target selector must still find a real sensible instance inside the travel envelope in [`docs/TARGETING_AND_DISTANCE_POLICY.md`](docs/TARGETING_AND_DISTANCE_POLICY.md). If it cannot, that posting does not appear.

Status language:

- **Explicit regional** — CozyCrazyZones itself binds the structure to the relevant cardinal region/tier.
- **Generic radial** — structure is legal by radial tier but not cardinal direction; quest runtime must choose an instance in the issuing region.
- **Authored/future** — destination requires story glue or encounter integration beyond ordinary structure rules.

---

# Shared Core / generic Hearthlands

Good ordinary local-adventure families:

| Structure | Minimum | Quest use |
|---|---|---|
| `dungeons_enhanced:stables` | Hearthlands | **The Empty Stalls**, stable jobs, saddle route |
| `dungeons_enhanced:dungeon_variant` | Hearthlands | courier/recovery fallback |
| `dungeons_enhanced:watch_tower` | Hearthlands | **The Watchman's Token**, survey jobs |
| `dungeons_enhanced:witch_tower` | Hearthlands | local witch/ingredient/recovery work |
| `dungeons_enhanced:sunken_shrine` | Hearthlands | coastal local exploration |
| `betterdungeons:spider_dungeon` | Hearthlands | spider trouble / mapped local dungeon |
| `valhelsia_structures:spawner_dungeon` | Hearthlands | modest local dungeon |
| `valhelsia_structures:tower_ruin` | Hearthlands | missing courier / journal recovery |
| `born_in_chaos_v1:observation_tower_*` | Hearthlands | observation/survey jobs |

These are **Generic radial** targets. A Frostmarch board should select a Frostmarch instance; a Harvestwood board should select a Harvestwood instance. Never use the nearest instance in the wrong cardinal region as a fallback.

---

# Frostmarch — North

## Hearthlands / transition-country structures

0.3.6 gives Frostmarch much stronger early structure identity than the older audit showed:

| Structure | Rule | Useful role |
|---|---|---|
| `beautify:botanist_house_snowy` | North / Hearthlands / transition+ | herbalist, winter supplies, local knowledge |
| `beautify:botanist_house_taiga` | North / Hearthlands / transition+ | botanist/trapper flavor |
| `minecraft:village_snowy` | North / Hearthlands / transition+ | northern civilization |
| `minecraft:village_taiga` | North+West / Hearthlands / transition+ | shared cold/old-forest settlement |
| `bettermineshafts:mineshaft_spruce` | North+West / Hearthlands / transition+ | early mining/map lead |

Generic towers/ruins remain useful for **A Marker in the Snow** and **The Trapper's Satchel**, provided the selected instance lies in Frostmarch.

## Frontier

| Structure | Rule | Useful role |
|---|---|---|
| `minecraft:igloo` | North / Frontier / established | local cartographer map, cold-country recovery |
| `bettermineshafts:mineshaft_ice` | North / Frontier / established | mining expedition |
| `bettermineshafts:mineshaft_spruce_snowy` | North / Frontier / established | northern mining expedition |

The current static cartographer prototype deliberately tests an Igloo map because its worldgen rule itself already provides the cardinal restriction.

## Wildlands

| Quest / use | Target | Status |
|---|---|---|
| **The Sleeping Mountain** | `mowziesmobs:frostmaw_spawn` | **Explicit regional** — North + Wildlands + established |
| frozen recovery / **Last Warm Camp** style job | `dungeons_enhanced:ice_pit` | **Explicit regional** — North + Wildlands + established |
| Ferrous Wroughtnaut side contract | `mowziesmobs:wrought_chamber` | Generic Wildlands |
| ancient monastery side expedition | `mowziesmobs:monastery` | Generic Wildlands |

## Dread Reaches

**Captain Cornelia / Aquamirae Ice Maze remains authored-story integration.** Do not invent a generic substitute merely because another frozen structure is available. The final North destination should become a real North+Dread target once the encounter/world hook is authoritative.

---

# Greenveil — East

## Hearthlands / transition-country structures

| Structure | Rule | Useful role |
|---|---|---|
| `minecraft:ruined_portal_jungle` | East / Hearthlands / transition+ | early ruin/map discovery |
| `minecraft:ruined_portal_swamp` | East / Hearthlands / transition+ | wet-country ruin |
| `bettermineshafts:mineshaft_jungle` | East / Hearthlands / transition+ | early mining/exploration |
| `bettermineshafts:mineshaft_lush` | East / Hearthlands / transition+ | lush underground lead |

This gives **The Naturalist's Satchel** and other H1 Greenveil recovery jobs real regional candidates rather than forcing every early East contract onto a generic tower.

## Frontier

| Structure | Rule | Useful role |
|---|---|---|
| `minecraft:jungle_pyramid` | East / Frontier / established | vanilla jungle archaeology |
| `minecraft:swamp_hut` | East / Frontier / established | witch/swamp knowledge |
| `betterwitchhuts:witch_hut` | East / Frontier / established | richer witch expedition |
| `betterwitchhuts:witch_circle` | East / Frontier / established | ritual/recovery lead |
| `bettermineshafts:mineshaft_overgrown` | East / Frontier / established | overgrown mine expedition |
| `dungeons_enhanced:jungle_monument` | East / Frontier / established | major regional recovery |
| `betterjungletemples:jungle_temple` | East / Frontier / established | **The Shrine Under Vines** / map target |

The last two are excellent anchors because the structure rule itself certifies the region.

## Wildlands

A Frontier-minimum Greenveil structure may still generate in Wildlands because rules are cumulative. Therefore **Temple of Eight Roots** can deliberately target a *Wildlands instance* of a Jungle Monument/Better Jungle Temple, but the custom locator must check the instance's actual radial cell rather than merely trusting the structure's minimum tier.

Generic Wildlands structures such as `mowziesmobs:wrought_chamber`, `dungeons_enhanced:deep_crypt`, or `monster_maze` can also appear as non-regional side adventures.

## Dread Reaches

**Jungle Abomination arena is future/authored.** Do not let a random temple stand in for the regional finale.

---

# Sunscar — South

Sunscar currently has the richest explicit structure ladder, which makes it an excellent map/cartographer test region.

## Hearthlands / transition country

| Structure | Rule | Useful role |
|---|---|---|
| `beautify:botanist_house_desert` | South / Hearthlands / transition+ | botanist / Acacia Blossom flavor |
| `beautify:botanist_house_savanna` | South / Hearthlands / transition+ | savanna ecology / elephant knowledge |
| `valhelsia_structures:desert_house` | South / Hearthlands / transition+ | local inhabited/abandoned site |
| `minecraft:village_desert` | South / Hearthlands / transition+ | regional civilization |
| `minecraft:village_savanna` | South / Hearthlands / transition+ | regional civilization |
| `minecraft:ruined_portal_desert` | South / Hearthlands / transition+ | early ruin/map lead |
| `bettermineshafts:mineshaft_acacia` | South / Hearthlands / transition+ | dry-country mine |
| `bettermineshafts:mineshaft_desert` | South / Hearthlands / transition+ | dry-country mine |
| `dungeons_enhanced:desert_tomb` | South / Hearthlands / transition+ | **Watering-Hole Ledger** / first archaeology map |

`dungeons_enhanced:desert_tomb` is one of our best first-village regional-adventure candidates because it is small enough for H1 and explicitly South.

## Frontier

| Structure | Rule | Useful role |
|---|---|---|
| `minecraft:desert_pyramid` | South / Frontier / established | ordinary archaeology |
| `bettermineshafts:mineshaft_mesa` | South / Frontier / established | mesa expedition |
| `bettermineshafts:mineshaft_red_desert` | South / Frontier / established | arid mine expedition |
| `born_in_chaos_v1:clown_caravan_savanna` | South / Frontier / established | strange road encounter / recovery |
| `dungeons_enhanced:desert_temple` | South / Frontier / established | archaeology/recovery |
| `betterdeserttemples:desert_temple` | South / Frontier / established | **The First Dig** / cartographer map |

## Wildlands

| Quest / use | Target | Status |
|---|---|---|
| **The Sunbird** | `mowziesmobs:umvuthana_grove` | **Explicit regional** — South + Wildlands + established |
| deeper archaeology | a Wildlands instance of a South Frontier structure | needs actual-instance radial check |
| generic ancient sideboss | `mowziesmobs:wrought_chamber` | Generic Wildlands |

## Dread Reaches

| Quest | Target | Status |
|---|---|---|
| **The Cursed Inscription** | `cataclysm:cursed_pyramid` | **Explicit regional** — South + Dread + established |
| **Guardian in the Sand** | Cursed Pyramid / Wadjet | authored within isolated pyramid package |
| **The Arena Below** | Cursed Pyramid / Kobolediator | authored within isolated pyramid package |
| **The Ancient Remnant** | `cataclysm:cursed_pyramid` | regional finale |

The Cursed Pyramid is the explicit exception to the otherwise suppressed Cataclysm structure namespace. It should not become an ordinary static cartographer trade.

---

# Harvestwood — West

## Hearthlands

Harvestwood still relies more heavily on generic low-tier structures whose **actual generated position** must be West/Harvestwood:

| Quest / use | Candidate | Status |
|---|---|---|
| **The Empty Stalls** | `dungeons_enhanced:stables` | Generic Hearthlands; excellent custom-locator test |
| local survey | `dungeons_enhanced:watch_tower` | Generic Hearthlands |
| lost traveler | `valhelsia_structures:tower_ruin` | Generic Hearthlands |
| old watch / survey | `born_in_chaos_v1:observation_tower_*` | Generic Hearthlands prefix |
| taiga-side knowledge | `minecraft:village_taiga`, `bettermineshafts:mineshaft_spruce` | North+West regional |

This is actually useful system design pressure: Harvestwood proves why we need a real `findUsefulTarget()` service rather than relying entirely on static structure-map trades.

## Frontier

`born_in_chaos_v1:clown_caravan_taiga` is North+West Frontier and can support odd-road/caravan stories where appropriate.

**The Gravekeeper's Bell** still needs a certified graveyard family binding. Do not hard-code a random Born in Chaos grave structure until the zoning/worldgen side declares the intended family/placement.

## Wildlands

| Quest / use | Target | Status |
|---|---|---|
| **The Headless Road** | `born_in_chaos_v1:sir_pumpkinhead` | explicit West Wildlands natural-entity rule; kill objective is reliable |
| pumpkin escalation / mapped site | `born_in_chaos_v1:infernal_pumpkin` | **Explicit regional** — West + Wildlands + established |
| Ferrous Wroughtnaut | `mowziesmobs:wrought_chamber` | Generic Wildlands sideboss |

The Infernal Pumpkin gives Harvestwood a real Wildlands structure landmark even before Lord Pumpkinhead's final route is finished.

## Dread Reaches

| Quest / use | Target | Status |
|---|---|---|
| **The Last Road West** | `born_in_chaos_v1:dark_tower_*` | **Explicit regional prefix** — West + Dread + established |
| **The Old Harvest** | Lord Pumpkinhead encounter | Authored/future final route |

The Dark Tower family is excellent pre-finale story knowledge. Lord Pumpkinhead's exact encounter route still needs the dedicated final hook rather than being inferred from the tower family.

---

# Generic Frontier structures for side work

These remain useful across cardinal regions when the selected actual instance lies in the issuing board's region:

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

The Valhelsia Forge deserves special reward/loot caution because its raw-iron jackpot is progression-relevant.

# Generic Wildlands structures for side work

- `dungeons_enhanced:deep_crypt`
- `dungeons_enhanced:monster_maze`
- `dungeons_enhanced:elders_temple`
- `dungeons_enhanced:flying_dutchman`
- `mowziesmobs:wrought_chamber`
- `mowziesmobs:monastery`

These generic structures are valuable because not every regional story needs a perfectly symmetrical bespoke landmark ladder.

---

# Locator rule that applies to every table above

A structure family is **permission to search**, not a guaranteed quest target.

The final runtime should:

```text
board/cartographer position
  → classify region + radial tier with CozyCrazyZones
  → search a bounded distance for candidate structure instances
  → reject illegal/wrong-region/wrong-tier/too-near/too-far candidates
  → avoid active/recent duplicates
  → score around the intended travel distance
  → select one real instance
  → or issue no structure quest/map at all
```

That is what keeps the information system believable.
