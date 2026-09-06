# Map System Vertical Slice

This is the first deliberately testable version of the CozyCrazyCraft map/information loop.

The important design choice is that **maps are knowledge rewards, not teleportation and not universal waypoints**. A bounty gives a reason to care; a cartographer/map gives a place; the Atlas remembers what the player has learned.

## Layer 0 — already owned by CozyCrazyZones 0.3.6

The starter opening is now a real working precedent rather than a mockup:

- Shared Core remains broadly ordinary through roughly 700 blocks.
- Cardinal ecology transitions from roughly 700–1,200 blocks.
- The first village is reserved roughly 1,000–1,650 blocks from the starter spawn, preferring roughly 1,050–1,250.
- `StarterDeskVillageMapService` prepares the physical map on the starter-house desk.
- `StarterAtlasService` grants the starter Atlas.
- `StarterVillageMapService` links/paints a guide route from the starter location toward that first village.
- The supplied Map Atlases config removes the empty-map/paper expansion tax and raises the map cap to 10,000.

That is exactly the relationship we want the rest of the quest system to imitate:

```text
someone knows about a place
        ↓
player receives a physical map/lead
        ↓
Atlas absorbs/records exploration knowledge
        ↓
player still has to travel through the world
```

## Layer 1 — low-risk structure-map prototype

Supplementaries exposes a 1.20.x server command shaped like:

```text
/supplementaries structure_map <structure-or-tag> [zoom]
```

Its implementation creates a real structure map/quill for the executing player and uses Minecraft's structure lookup. The command accepts either a structure ID or a structure tag.

Bountiful 6.0.4 supports `command` rewards. A command reward is executed by the server and replaces `%PLAYER_NAME%` with the player turning in the bounty.

That means we can connect the two systems without inventing a custom event listener:

```text
execute as %PLAYER_NAME% at @s run supplementaries structure_map betterjungletemples:jungle_temple 2
```

The repository now contains four deterministic technical-test decrees:

```text
/bo decree ccc_map_test_north
/bo decree ccc_map_test_east
/bo decree ccc_map_test_south
/bo decree ccc_map_test_west
```

Each decree has exactly one simple objective (`1 compass`) and exactly one map reward so the test cannot randomly pair with the wrong destination.

Current test destinations:

| Test | Destination | Intended cell |
|---|---|---|
| Frostmarch | `mowziesmobs:frostmaw_spawn` | North / Wildlands |
| Greenveil | `betterjungletemples:jungle_temple` | East / Frontier |
| Sunscar | `mowziesmobs:umvuthana_grove` | South / Wildlands |
| Harvestwood | `dungeons_enhanced:stables` | Hearthlands; first local-adventure prototype |

These are **technical test decrees**, not the final player-facing quest design. They exist to prove this exact loop:

```text
accept/complete Bountiful bounty
        ↓
turn bounty in
        ↓
Bountiful command reward fires
        ↓
Supplementaries finds a structure
        ↓
player receives a real structure map
        ↓
Map Atlases can consume/use the map normally
```

## Suggested in-game smoke test

1. Install/copy the repository's `deployment/config/bountiful/` files into a disposable test instance.
2. Run `/reload` and `/bo test`.
3. Obtain one technical decree with `/bo decree ccc_map_test_west`.
4. Put it into a Bountiful board and let the board generate its test bounty.
5. Accept the bounty.
6. Give/obtain one compass and complete/turn in the bounty.
7. Confirm that a real Supplementaries structure map is given rather than a command error or empty reward.
8. Open/use that map and confirm it behaves sensibly with Map Atlases.
9. Repeat in an appropriate region/tier for the North/East/South test decrees.

For North/South Wildlands tests, stand in the correct broad area before generating/redeeming the test. Supplementaries' implementation uses a finite nearby structure search, so a player standing in the Shared Core should not be expected to receive a map to a structure several thousand blocks beyond the search radius.

## Why this is a prototype rather than the final locator

Supplementaries intentionally solves the ordinary case: "give me a map to a nearby structure of this kind."

The final story locator may eventually need stronger constraints:

- correct macro-region
- correct radial tier
- correct influence band
- minimum/ideal/maximum travel distance from the issuing settlement
- prefer unexplored targets
- avoid reusing a structure already assigned to the player
- guarantee an outward quest actually points farther from spawn
- remember which exact structure instance a proof item belongs to

Those constraints are **not required to prove the gameplay idea**. We should first make sure receiving and using real structure maps from quest rewards feels good.

## Layer 2 — regional cartographer inventory

Once the technical reward works, the next low-complexity step is to stop making every map originate from a bounty.

The intended division remains:

```text
Bounty Board = why somebody wants the place visited
Cartographer  = where known places are
Atlas         = what the player has learned
Journals/maps = lost or unusual knowledge
Structure     = what actually happened there
```

A board can therefore say:

> The old stable has gone quiet. Ask the cartographer about the road west.

The cartographer can then provide/sell the actual map. This prevents the board from becoming a magical omniscient GPS machine.

## Layer 3 — authored recovery contracts

The strongest structure quests should eventually use a unique proof item:

```text
The Empty Stalls
  target: Dungeons Enhanced Stables
  objective: recover Stablemaster's Seal
  navigation: local structure map / cartographer lead
  completion: bring exact proof item back
```

The proof item is intentionally simpler than detecting "structure cleared." The hard part is deterministic proof placement in the chosen structure instance; once the item exists, Bountiful only needs to track an exact item turn-in.

## Current stop line

Do **not** write a large custom locator or Atlas renderer yet.

First prove:

1. custom-only Bountiful content loads,
2. story-style notices are readable,
3. command rewards fire reliably,
4. Supplementaries maps find the intended structures,
5. the maps coexist cleanly with Map Atlases,
6. the loop still feels like Minecraft exploration rather than a quest-menu GPS system.

If those six things feel good, the custom runtime work becomes much more focused and much less risky.
