# CozyCrazyZones 0.3.6 — Quest-Facing Audit

This is a quest-system audit of the user-provided `CozyCrazyZones-0.3.6.jar`, not a replacement for the zoning project itself.

Direct bytecode inspection of `ZoneRuleRegistry` found:

- **67 explicit structure rules**
- **2 structure-prefix rules**
- **51 natural-entity rules**

That is substantially richer than the earlier 0.3.4 handoff and opens many more low-complexity map, bounty, and cartographer hooks.

The quest runtime should always prefer the live `CozyZonesApi`/`ZoneRuleRegistry` at runtime; this document exists so content authors can understand the current design surface.

---

# Important new early regional structure families

## Frostmarch / North

Hearthlands transition:

- `beautify:botanist_house_snowy`
- `beautify:botanist_house_taiga`
- `minecraft:village_snowy`
- `minecraft:village_taiga` (North + West)
- `bettermineshafts:mineshaft_spruce` (North + West)

Frontier established:

- `minecraft:igloo`
- `bettermineshafts:mineshaft_ice`
- `bettermineshafts:mineshaft_spruce_snowy`

Wildlands established:

- `dungeons_enhanced:ice_pit`
- `mowziesmobs:frostmaw_spawn`

Quest consequence: Frostmarch now has enough real regional structure identity for proper early cartographer maps and recovery jobs instead of relying almost entirely on generic towers.

## Greenveil / East

Hearthlands transition:

- `minecraft:ruined_portal_jungle`
- `minecraft:ruined_portal_swamp`
- `bettermineshafts:mineshaft_jungle`
- `bettermineshafts:mineshaft_lush`

Frontier established:

- `minecraft:jungle_pyramid`
- `minecraft:swamp_hut`
- `betterwitchhuts:witch_hut`
- `betterwitchhuts:witch_circle`
- `bettermineshafts:mineshaft_overgrown`
- `dungeons_enhanced:jungle_monument`
- `betterjungletemples:jungle_temple`

Quest consequence: Greenveil has a particularly good **ruins → witch country → jungle temple** information ladder before the future Jungle Abomination finale.

## Sunscar / South

Hearthlands transition:

- `beautify:botanist_house_desert`
- `beautify:botanist_house_savanna`
- `valhelsia_structures:desert_house`
- `minecraft:village_desert`
- `minecraft:village_savanna`
- `minecraft:ruined_portal_desert`
- `bettermineshafts:mineshaft_acacia`
- `bettermineshafts:mineshaft_desert`
- `dungeons_enhanced:desert_tomb`

Frontier established:

- `minecraft:desert_pyramid`
- `bettermineshafts:mineshaft_mesa`
- `bettermineshafts:mineshaft_red_desert`
- `born_in_chaos_v1:clown_caravan_savanna`
- `dungeons_enhanced:desert_temple`
- `betterdeserttemples:desert_temple`

Wildlands established:

- `mowziesmobs:umvuthana_grove`

Dread Reaches established:

- `cataclysm:cursed_pyramid`

Quest consequence: Sunscar currently has the cleanest full regional structure ladder from first-village country all the way to its finale.

## Harvestwood / West

Hearthlands regional/shared:

- `minecraft:village_taiga` (North + West)
- `bettermineshafts:mineshaft_spruce` (North + West)
- generic Hearthlands structures selected by actual West position

Frontier:

- `born_in_chaos_v1:clown_caravan_taiga` (North + West)

Wildlands established:

- `born_in_chaos_v1:infernal_pumpkin`

Dread Reaches established prefix:

- `born_in_chaos_v1:dark_tower_*`

Quest consequence: Harvestwood intentionally remains more dependent on **generic structures selected by actual regional position** at low tiers. This makes it the best test case for the custom target selector. Its explicit pumpkin/Born in Chaos identity becomes much stronger outward.

---

# Generic radial structure families still useful everywhere

## Hearthlands+

- `dungeons_enhanced:stables`
- `dungeons_enhanced:dungeon_variant`
- `dungeons_enhanced:watch_tower`
- `dungeons_enhanced:witch_tower`
- `dungeons_enhanced:sunken_shrine`
- `betterdungeons:spider_dungeon`
- `valhelsia_structures:spawner_dungeon`
- `valhelsia_structures:tower_ruin`
- `born_in_chaos_v1:observation_tower_*`

## Frontier+

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

## Wildlands+

- `dungeons_enhanced:deep_crypt`
- `dungeons_enhanced:monster_maze`
- `dungeons_enhanced:elders_temple`
- `dungeons_enhanced:flying_dutchman`
- `mowziesmobs:wrought_chamber`
- `mowziesmobs:monastery`

These are perfect for side adventures because the quest locator can impose the issuing board's region without pretending the structure family itself is cardinal-exclusive.

---

# Natural ecology useful for bounty generation

The natural rules now give us much stronger regional field-job material.

## Hearthlands regional wildlife

Sunscar:

- `alexsmobs:gazelle`
- `alexsmobs:elephant`
- `alexsmobs:emu`
- `alexsmobs:kangaroo`
- `alexsmobs:maned_wolf`
- `alexsmobs:rhinoceros`
- `alexsmobs:jerboa`
- `alexsmobs:roadrunner`

Greenveil:

- `alexsmobs:gorilla`
- `alexsmobs:toucan`
- `alexsmobs:capuchin_monkey`

Frostmarch + Harvestwood:

- `alexsmobs:moose`

These are ecology/knowledge material more than indiscriminate kill-bounty material. A regional quest can ask for food, observation-related items, plant materials, or taming support rather than paying the player to slaughter every recognizable animal.

## Frontier regional threats

Frostmarch:

- `alexsmobs:snow_leopard`
- `alexsmobs:grizzly_bear` (North + West)

Greenveil:

- `alexsmobs:tiger`
- `alexsmobs:anaconda`
- `alexsmobs:caiman`
- `alexsmobs:crocodile`
- `mowziesmobs:foliaath`
- `skarrier_mobs:dangle`
- `skarrier_mobs:slither_spawner_dummy`

Sunscar:

- `alexsmobs:rattlesnake`
- `alexsmobs:rocky_roller`
- `alexsmobs:guster`
- `alexsmobs:tarantula_hawk`
- `skarrier_mobs:snap`
- `skarrier_mobs:quake`
- `born_in_chaos_v1:spirit_guide`

Generic Frontier escalation also includes Born in Chaos bruisers/hounds/casters and selected Myths of the Sea threats.

## Wildlands regional threats

Greenveil:

- `skarrier_mobs:carniflore`
- `skarrier_mobs:slither_matriarch`

Sunscar:

- `alexsmobs:sunbird`

Harvestwood:

- `born_in_chaos_v1:sir_pumpkinhead`

Generic Wildlands includes Wrought, Lifestealer, Missioner, Nightmare Stalker, Mother Spider, etc. The Fallen Chaos Knight rule is currently disabled pending suppression testing and must not be used as a guaranteed natural-ecology bounty target.

## Dread Reaches

Generic Dread natural rules include:

- `myths_of_the_sea:leviathan`
- `myths_of_the_sea:kraken`
- `born_in_chaos_v1:supreme_bonescaller`

These are serious optional ecology rather than automatic regional-story steps.

---

# Natural-spawn caveat

A `natural_entity_rule` means the entity is appropriate for **natural ecology** in that cell. It does not mean:

- a player can always find one nearby,
- a structure-spawned instance obeys the same rule,
- a raid/summon/command appearance is forbidden elsewhere,
- every legal entity should become a bounty objective.

For a repeatable natural-mob hunt, the future board runtime should check `CozyZonesApi.naturalEntityAllowed(...)` at the issuing area and only use mobs with reliable enough local availability.

---

# Quest-system priorities created by 0.3.6

1. **Exploit the new H1/F2 regional structures for cartographer maps.** They are much safer than generic structure maps because the world layer already provides cardinal placement.
2. **Use regional wildlife to create non-combat identity.** Elephant/Capuchin/Moose/etc. are world texture first, quest targets second.
3. **Use regional threats for repeatable field jobs where appropriate.** This is where exact Bountiful entity objectives shine.
4. **Keep generic structure families available for variation.** Select the instance by actual region instead of banning useful dungeons from most of the map.
5. **Do not infer unfinished finals.** Cornelia, Jungle Abomination, and Lord Pumpkinhead still need their intended final encounter routes even if nearby substitute structures exist.

This audit is why the quest project now treats 0.3.6 as a substantially stronger information substrate than 0.3.4.
