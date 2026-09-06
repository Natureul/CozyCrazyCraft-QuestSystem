# Integration Boundary

This file keeps the quest layer coupled to the **real** world-substrate API instead of duplicating or guessing geography.

## Current handoff: CozyCrazyZones 0.3.6

The quest layer now targets the actual public API shipped in `CozyCrazyZones 0.3.6`.

Production quest code may depend on these public methods:

```java
CozyZonesApi.distanceFromSpawn(ServerLevel level, double x, double z)
CozyZonesApi.regionAt(ServerLevel level, double x, double z)
CozyZonesApi.regionForDistance(double distance)
CozyZonesApi.influenceBandAt(ServerLevel level, double x, double z)
CozyZonesApi.influenceBandForDistance(double distance)
CozyZonesApi.regionalStrengthAt(ServerLevel level, double x, double z)
CozyZonesApi.regionalStrengthForDistance(double distance)
CozyZonesApi.macroRegionAt(ServerLevel level, double x, double z)
CozyZonesApi.macroRegionForOffset(long worldSeed, double dx, double dz)
CozyZonesApi.macroBoundaryStrengthAt(ServerLevel level, double x, double z)
CozyZonesApi.regionalCellAt(ServerLevel level, double x, double z)
CozyZonesApi.structureAllowed(ServerLevel level, ResourceLocation structureId, double x, double z)
CozyZonesApi.naturalEntityAllowed(ServerLevel level, ResourceLocation entityId, double x, double z)
CozyZonesApi.isDaytimeCandidate(ResourceLocation entityId)
```

The authoritative classifications are:

```text
Region:
  HEARTHLANDS
  FRONTIER
  WILDLANDS
  DREAD_REACHES

MacroRegion:
  NORTH  -> Frostmarch
  EAST   -> Greenveil
  SOUTH  -> Sunscar
  WEST   -> Harvestwood

RegionalInfluenceBand:
  SHARED_CORE
  CARDINAL_TRANSITION
  ESTABLISHED
```

`RegionalCell` supplies all three classifications together plus distance, regional strength, and macro-boundary strength. It also exposes `ecologyDisplayName()` and `cellDisplayName()` for presentation.

## Default geography in 0.3.6

Do not hard-code these values in quest runtime logic; they are recorded here because they explain the intended play curve.

```text
0–700        Shared Core: ordinary starter countryside
700–1200     Organic cardinal transition
1200+        Cardinal ecology clearly established

0–2500       Hearthlands
2500–5500    Frontier
5500–9000    Wildlands
9000+        Dread Reaches
```

The macro borders are warped and blended rather than rigid compass quadrants. The default angular border blend is 11 degrees.

## What 0.3.6 additionally gives the information layer

This update is important because the zoning project now owns a working **starter map/Atlas vertical slice** rather than only geography classification.

### First-village reservation

`VillageRingPlanner` reserves a locator-compatible village candidate with this preference:

```text
preferred pass: 1050–1250 blocks
fallback:       1000–1450 blocks
last fallback:  1000–1650 blocks
```

Candidates are filtered for plausible village land and away from weak macro-boundary locations.

That is almost exactly the desired first-act geography: the starter house sits in ordinary Shared Core countryside, and the first village lands near the point where regional identity begins to become legible.

### Starter desk map

`StarterDeskVillageMapService` finds the starter-house desk item frame and prepares/paints a village guide map toward the reserved first village.

### Starter Atlas

`StarterAtlasService` ensures the player receives the Atlas, and `StarterVillageMapService` links a guide route between the starter location and first village. The supplied root overlay also configures Map Atlases for effectively unbounded expansion (`max_map_count = 10000`), no paper/empty-map tax, and inventory activation.

The quest project should **reuse this precedent** rather than invent a competing starter map implementation.

## What this unlocks now

### Regional board assignment

This is no longer blocked.

The server can classify a Bountiful board from its real block position:

```text
cell = CozyZonesApi.regionalCellAt(level, boardX, boardZ)
```

The quest layer should use that result to choose the board's permitted decree family. It should not reimplement the geographic math itself.

### Regional quest filtering

A candidate quest can be checked against:

- radial tier
- macro-region
- shared/transition/established influence

For world content, `ZoneRuleRegistry` and the API's `structureAllowed` / `naturalEntityAllowed` methods are authoritative.

### Structure and mob catalog joins

`ZoneRuleRegistry` exposes:

```java
structureRule(ResourceLocation)
minimumStructureRegion(ResourceLocation)
structureExplicitlySuppressed(ResourceLocation)
structures()
structurePrefixes()
naturalEntityRule(ResourceLocation)
naturalEntityNamespaceSuppressed(ResourceLocation)
naturalEntities()
```

The 0.3.6 rules now contain stronger cardinal assignments, including Greenveil/Sunscar/Harvestwood structure families and region-specific natural mobs. Quest issuance should cross-check these rules instead of assuming that registry presence alone means a target is legal at a board's position.

## Map-system direction after 0.3.6

There are now two map layers:

1. **Starter navigation** — owned by CozyCrazyZones 0.3.6; already working toward the first village and Atlas.
2. **Quest/cartographer maps** — owned by this project; should build on Minecraft/Supplementaries maps and Map Atlases rather than replacing the starter system.

For a low-risk first implementation, Supplementaries exposes a server command:

```text
supplementaries structure_map <structure-or-tag> [zoom]
```

Its map command creates a real structure map/quill for the executing player and searches nearby structures. Bountiful 6.0.4 command rewards execute as the server and support `%PLAYER_NAME%`, so a reward can safely issue a map with a command shaped like:

```text
execute as %PLAYER_NAME% at @s run supplementaries structure_map betterjungletemples:jungle_temple 2
```

That is now the preferred **playtest bridge** for cartographer/map rewards because it lets us validate the gameplay loop before writing a custom structure-locator subsystem.

The long-term locator can replace the selection step later when we need strict same-region/tier, unexplored-target, outward-only, or no-repeat guarantees.

## Still separate work

### Useful structure targeting

For production story contracts we still want a quest-side locator that can find a real generated instance under tighter constraints:

```text
structure id/tag
correct radial tier
correct macro-region
minimum/ideal/maximum distance from issuing board
prefer unexplored
avoid repeat targets
outward quests must actually point outward
```

Supplementaries maps are an excellent prototype and may remain sufficient for many ordinary cartographer maps, but main-story targeting should eventually be deterministic enough to respect these rules.

### Same-board redemption

Still needs source-board metadata and redemption interception if we keep the same-board rule.

### Structure proof placement

Still needs a deterministic insertion path for a unique proof item in a generated structure. Do not fake exact-structure completion with a generic Bountiful kill criterion.

### Main-story state

Regional boss completion, final relic requirements, Stronghold/End progression, and one-time story flags remain a separate lightweight state layer.

### Rescue-pet ownership transfer

Still requires a deterministic tested method. Prefer giving taming materials/knowledge unless a one-off rescue specifically justifies direct transfer.

---

# Client-facing geography

CozyCrazyZones 0.3.6 synchronizes the current radial region, macro-region, and influence band to the client through `ClientRegionState`.

That is enough for the quest UI to decorate a board with contextual labels such as:

```text
HEARTHLANDS NOTICE
Frostmarch

FRONTIER CONTRACT
Sunscar
```

The story UI should treat that as presentation context only. Server-side board/decree assignment must still be based on the board's authoritative server position.

---

# Principle

The zoning project owns the truth of **where things can exist**.

The quest project owns the truth of **what people know about those things, why the player might care, and what the player receives for engaging with them**.

Neither project should duplicate the other's core logic.
