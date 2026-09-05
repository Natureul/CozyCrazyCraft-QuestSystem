# Integration Boundary

This file keeps the quest layer coupled to the **real** world-substrate API instead of duplicating or guessing geography.

## Current handoff: CozyCrazyZones 0.3.4

The previous conceptual geography placeholder is now superseded by the actual public API shipped in `CozyCrazyZones 0.3.4`.

Production quest code may now depend on these public methods:

```java
CozyZonesApi.distanceFromSpawn(ServerLevel level, double x, double z)
CozyZonesApi.regionAt(ServerLevel level, double x, double z)
CozyZonesApi.regionForDistance(double distance)
CozyZonesApi.influenceBandAt(ServerLevel level, double x, double z)
CozyZonesApi.regionalStrengthAt(ServerLevel level, double x, double z)
CozyZonesApi.macroRegionAt(ServerLevel level, double x, double z)
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
  WEST   -> Harvestlands

RegionalInfluenceBand:
  SHARED_CORE
  CARDINAL_TRANSITION
  ESTABLISHED
```

`RegionalCell` supplies all three classifications together plus distance, regional strength, and macro-boundary strength.

## What this unlocks now

### Regional board assignment

This is no longer blocked.

The server can classify a Bountiful board from its real block position:

```text
cell = CozyZonesApi.regionalCellAt(level, boardX, boardZ)
```

The quest layer should use that result to choose the board's permitted decree family. It should **not** reimplement the 700/1200/2500/5500/9000 distance logic itself.

### Regional quest filtering

This is no longer blocked.

A candidate quest can be checked against:

- radial tier
- macro-region
- shared/transition/established influence

For world content, `ZoneRuleRegistry` and the API's `structureAllowed` / `naturalEntityAllowed` methods are authoritative.

### Structure and mob catalog joins

This is no longer blocked at the classification level.

`ZoneRuleRegistry` exposes:

```java
structureRule(ResourceLocation)
minimumStructureRegion(ResourceLocation)
structures()
naturalEntityRule(ResourceLocation)
naturalEntities()
```

Structure rules expose minimum region, macro-region restrictions, minimum influence band, and a note. Natural-entity rules additionally expose daytime-candidate and enabled flags.

The 0.3.4 registry already contains concrete quest-relevant IDs including Dungeons Enhanced, Better Dungeons, Valhelsia Structures, Born in Chaos, Mowzie's Mobs, Skarrier Mobs, Myths of the Sea, and the permitted Cataclysm exception.

## Still separate work

The zoning handoff answers **where** a thing is legal. It does not by itself solve every quest mechanic.

### Useful structure targeting

Still needed: a quest-side locator that finds a real generated instance under constraints such as:

```text
structure id/tag
correct radial tier
correct macro-region
minimum/ideal/maximum distance from issuing board
prefer unexplored
avoid repeat targets
outward quests must actually point outward
```

This should consume CozyCrazyZones truth rather than duplicate it.

### Regional cartographer maps

Still needs the target selector plus map creation/integration.

### Same-board redemption

Still needs source-board metadata and redemption interception if we keep the same-board rule.

### Structure proof placement

Still needs a deterministic insertion path for a unique proof item in a generated structure. Do not fake exact-structure completion with a generic Bountiful kill criterion.

### Main-story state

Regional boss completion, final relic requirements, Stronghold/End progression, and one-time story flags remain a separate lightweight state layer.

### Rescue-pet ownership transfer

Still requires a deterministic tested method.

---

# Client-facing geography

CozyCrazyZones 0.3.4 already synchronizes the current radial region, macro-region, and influence band to the client through `ClientRegionState`.

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
