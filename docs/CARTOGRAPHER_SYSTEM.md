# Cartographers, Maps & Local Knowledge

The cartographer system should make the world feel **known by people who live in it**, without turning Minecraft into a waypoint menu.

The division of labor remains:

```text
Bounty Board = why someone wants something done
Cartographer  = where known places are
Atlas         = what the player has learned
Found maps    = what lost travelers knew
Journals      = context, rumor, and partial knowledge
Structures    = the actual place
```

The most important consequence is that a bounty does **not** need to magically contain coordinates to its target.

---

## What Supplementaries already supports

Supplementaries registers a data-driven villager trade type:

```text
supplementaries:structure_map
```

Moonlight loads villager trade JSON from:

```text
data/<namespace>/moonlight/villager_trade/<profession>/<trade>.json
```

For ordinary cartographers the path is therefore shaped like:

```text
data/cozycrazyquests/moonlight/villager_trade/cartographer/<trade>.json
```

A structure-map trade can specify:

- target structure ID or structure tag,
- emerald price range,
- optional secondary price such as a compass,
- villager level 1–5,
- max uses,
- map display name,
- map color/marker.

When a cartographer generates the offer, Supplementaries performs the structure lookup from the **villager's actual block position**. If it cannot produce a map, the trade returns no offer.

This is an unusually good low-complexity fit for CozyCrazyCraft.

---

# Version 1: local cartographers

The first production-safe cartographer pass should be modest.

A village cartographer knows a selection of **local or regional structures within roughly the current/next practical travel range**. They do not know every boss in the world.

Suggested trade ladder:

| Cartographer level | Knowledge character | Typical map |
|---|---|---|
| Apprentice (2) | nearby roads/sites | Hearthlands local landmark |
| Journeyman (3) | regional travel | Frontier structure |
| Expert (4) | serious expeditions | selected current-tier major location, only where safe |
| Master (5) | rare knowledge | one unusual/next-tier lead, not automatic finale access |

Novice level 1 can remain ordinary vanilla paper/glass-type commerce so the cartographer still feels like a villager rather than a map vending terminal.

### Initial pricing language

Maps should cost enough that information has value without becoming grindy:

- local H1 map: roughly 5–9 emeralds,
- Frontier map: roughly 8–14,
- serious expedition map: roughly 12–20,
- rare authored lead: price can include a compass or proof/reputation requirement later.

Exact economics should be tuned in play rather than treated as sacred numbers.

---

# Region-specific known places

With CozyCrazyZones 0.3.6, we have real region-locked structures suitable for local cartographer inventories.

## Frostmarch

Early/local candidates:

- `beautify:botanist_house_snowy`
- `beautify:botanist_house_taiga`
- `minecraft:village_snowy`
- `minecraft:igloo` (Frontier)
- `bettermineshafts:mineshaft_ice` (Frontier)
- `bettermineshafts:mineshaft_spruce_snowy` (Frontier)

Later authored knowledge:

- `dungeons_enhanced:ice_pit`
- `mowziesmobs:frostmaw_spawn`

Frostmaw should not become an ordinary globally available Expert trade. It is a named Great Hunt/story lead and deserves authored/reputation-aware issuance later.

## Greenveil

Early/local candidates:

- `minecraft:ruined_portal_jungle`
- `minecraft:ruined_portal_swamp`
- `bettermineshafts:mineshaft_jungle`
- `bettermineshafts:mineshaft_lush`

Frontier candidates:

- `minecraft:jungle_pyramid`
- `minecraft:swamp_hut`
- `betterwitchhuts:witch_hut`
- `betterwitchhuts:witch_circle`
- `bettermineshafts:mineshaft_overgrown`
- `dungeons_enhanced:jungle_monument`
- `betterjungletemples:jungle_temple`

The last two are excellent named regional maps because they are explicit Greenveil + Frontier structures.

## Sunscar

Early/local candidates:

- `beautify:botanist_house_desert`
- `beautify:botanist_house_savanna`
- `valhelsia_structures:desert_house`
- `minecraft:ruined_portal_desert`
- `bettermineshafts:mineshaft_acacia`
- `bettermineshafts:mineshaft_desert`
- `dungeons_enhanced:desert_tomb`

Frontier candidates:

- `minecraft:desert_pyramid`
- `bettermineshafts:mineshaft_mesa`
- `bettermineshafts:mineshaft_red_desert`
- `born_in_chaos_v1:clown_caravan_savanna`
- `dungeons_enhanced:desert_temple`
- `betterdeserttemples:desert_temple`

Later authored knowledge:

- `mowziesmobs:umvuthana_grove`
- `cataclysm:cursed_pyramid`

The Cursed Pyramid is **not** a normal cartographer commodity. Its map belongs at the end of a serious Sunscar information chain.

## Harvestwood

Early regional structures are currently less exclusive than South/East, which is fine. Useful local candidates include generic structures chosen only when their actual position lies in Harvestwood:

- `dungeons_enhanced:stables`
- `dungeons_enhanced:watch_tower`
- `valhelsia_structures:tower_ruin`
- `born_in_chaos_v1:observation_tower_*`

Explicit later regional structures include:

- `born_in_chaos_v1:infernal_pumpkin` (Wildlands)
- `born_in_chaos_v1:dark_tower_*` (Dread Reaches)

Those later structures should be story/expedition knowledge, not routine Apprentice maps.

---

# Why static regional structure trades are useful but not sufficient

A static Supplementaries structure-map trade has a lovely property: the lookup starts from the cartographer's actual position and returns **no offer when no matching structure can be found**.

That naturally prevents many nonsense maps.

However, the trade JSON itself does not ask CozyCrazyZones:

> Is this cartographer actually in Frostmarch Frontier?

Therefore a structure whose worldgen is strongly region-locked is reasonably safe for the v1 system, because an out-of-region cartographer usually has no matching structure within the finite search radius. But a village near a macro-region boundary can still potentially know about a site just across the border.

That is acceptable for ordinary geographical knowledge—people near borders can know what is nearby—but it means **strict story maps need the quest runtime**, not only static villager data.

---

# Recommended v1 static trade set

Start with maps whose structure rules themselves enforce the desired geography.

Good candidates for first testing:

```text
Frostmarch Frontier:
  minecraft:igloo
  bettermineshafts:mineshaft_ice

Greenveil Frontier:
  betterjungletemples:jungle_temple
  dungeons_enhanced:jungle_monument

Sunscar Hearthlands/Frontier:
  dungeons_enhanced:desert_tomb
  betterdeserttemples:desert_temple

Harvestwood Hearthlands:
  do NOT use a globally static generic-stable trade as the final solution;
  use our quest-side same-region locator or keep it as a technical prototype.
```

This gives us three clean regional trade laboratories and one useful case showing why custom targeting is eventually needed.

---

# How maps connect to bounties

There are three good patterns.

### Pattern A — Board supplies enough information itself

Use for obvious local work.

```text
Board: "Webs have taken over the old watchtower north of town."
Player already knows/recognizes the place.
No map needed.
```

### Pattern B — Board tells player to consult the cartographer

Use for mapped structure work.

```text
Board: The Empty Stalls
"The old stable on the western road went quiet. The cartographer has the road survey."

Cartographer: sells Old Stable Road Map
Player travels to the real selected structure.
```

This is the preferred pattern because it makes villagers feel like a community with different roles.

### Pattern C — Map exists first; bounty provides a reason later

The player buys/finds a map to an old temple. Later, a board posts a recovery contract for that same family or an item found there.

This makes knowledge feel like something the player can proactively collect, not only quest-issued UI state.

---

# Found maps and journals

Cartographers represent **living local knowledge**. Found maps represent information that is no longer part of ordinary settlement knowledge.

Good loot-placement roles:

- Watchtower journal → map to another watchtower/castle ruin.
- Desert tomb tablet → clue/map deeper into Sunscar.
- Jungle temple notes → another ruin or Toxic Cave lead.
- Mansion correspondence → deep Harvestwood destination.
- Frozen expedition journal → outer Frostmarch/coast lead.

A found map may legitimately point farther than a village cartographer's ordinary inventory, because the source itself explains why the knowledge is unusual.

---

# The Atlas is not the quest engine

Map Atlases should remain a **memory/exploration tool**.

The quest system should not require custom Atlas rendering, boss icons, or live objective GPS unless later testing proves it genuinely improves the pack.

Desired behavior:

1. receive/buy/find a physical map,
2. add/use it with the Atlas,
3. Atlas remembers that explored/map knowledge,
4. travel normally,
5. pins remain player-authored or deliberately sparse.

That preserves the Minecraft feeling of physically possessing information.

---

# Duplicate-map policy

Eventually the runtime should remember maps/targets offered to the player or settlement.

Suggested behavior:

- do not repeatedly sell the same one-use authored map to the same player,
- ordinary cartographer trades may repeat after restock because they are commerce,
- recovery-contract targets should avoid a currently assigned structure,
- recent assignment history should bias toward a different target instance,
- if there is only one sensible regional structure, reuse is preferable to selecting a wrong-region site—but the story text should not pretend it is a newly discovered place every time.

---

# Distance policy

Cartographer maps use the same semantic travel scopes as quests; see [`TARGETING_AND_DISTANCE_POLICY.md`](TARGETING_AND_DISTANCE_POLICY.md).

Important shorthand:

```text
local village knowledge  -> Local Site
named same-tier map       -> Regional Expedition
rare next-tier map        -> Outward Lead
final/boss destination    -> Legendary Destination / authored only
```

The backend should be willing to say **no map available**.

---

# Runtime upgrade path

V1 can use Moonlight/Supplementaries data-driven trades and Bountiful command-map tests.

The eventual small custom quest companion should add one shared service:

```java
Optional<TargetAssignment> findUsefulTarget(
    ServerLevel level,
    BlockPos source,
    TargetFamily family,
    TravelScope scope,
    @Nullable UUID player
)
```

It should consume CozyCrazyZones geography and return the selected real position/structure plus enough metadata for:

- making the map,
- putting a proof item into the correct target,
- preventing duplicate assignments,
- presenting honest distance language,
- and redeeming against the intended contract.

One locator service should serve **boards, cartographers, found-map generation, and story leads**. Do not build four independent locating systems.

---

# The intended feeling

A new village should feel like this:

> The board has five mundane/local concerns. One posting mentions an old site. The cartographer knows a couple of places in the surrounding country. Maybe a journal in one of those places points farther outward.

A deep settlement should feel like this:

> These people know their dangerous country. Their board has serious regional problems; their cartographer has maps the inner villages never possessed; old expedition records begin pointing toward legendary places.

That gives progression to **knowledge itself**, without adding a quest book.
