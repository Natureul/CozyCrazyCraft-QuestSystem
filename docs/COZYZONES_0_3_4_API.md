# CozyCrazyZones 0.3.4 Quest Integration Contract

This document records the public surface actually present in the supplied `CozyCrazyZones-0.3.4.jar`.

The quest project should consume this API rather than reproducing zoning math.

## Public geography API

`com.natureul.cozycrazyzones.CozyZonesApi`

```java
double distanceFromSpawn(ServerLevel level, double x, double z);
Region regionAt(ServerLevel level, double x, double z);
Region regionForDistance(double distance);

RegionalInfluenceBand influenceBandAt(ServerLevel level, double x, double z);
RegionalInfluenceBand influenceBandForDistance(double distance);

double regionalStrengthAt(ServerLevel level, double x, double z);
double regionalStrengthForDistance(double distance);

MacroRegion macroRegionAt(ServerLevel level, double x, double z);
MacroRegion macroRegionForOffset(long seed, double dx, double dz);
double macroBoundaryStrengthAt(ServerLevel level, double x, double z);

RegionalCell regionalCellAt(ServerLevel level, double x, double z);

boolean structureAllowed(ServerLevel level, ResourceLocation structureId, double x, double z);
boolean naturalEntityAllowed(ServerLevel level, ResourceLocation entityId, double x, double z);
boolean isDaytimeCandidate(ResourceLocation entityId);
```

For quest-board classification, prefer one call to:

```java
RegionalCell cell = CozyZonesApi.regionalCellAt(level, boardX, boardZ);
```

The returned record contains:

```java
Region radialZone();
MacroRegion macroRegion();
RegionalInfluenceBand influenceBand();
double distanceFromSpawn();
double regionalStrength();
double macroBoundaryStrength();

boolean sharedCore();
String ecologyDisplayName();
String cellDisplayName();
```

## Region enums

### Radial zone

- `HEARTHLANDS` — `Hearthlands`
- `FRONTIER` — `The Frontier`
- `WILDLANDS` — `Wildlands`
- `DREAD_REACHES` — `Dread Reaches`

### Macro-region

- `NORTH` — `Frostmarch`
- `EAST` — `Greenveil`
- `SOUTH` — `Sunscar`
- `WEST` — `Harvestlands`

### Influence band

- `SHARED_CORE` — `Shared Core`
- `CARDINAL_TRANSITION` — `Cardinal Transition`
- `ESTABLISHED` — `Established Region`

## Rule registry

`ZoneRuleRegistry` publicly exposes:

```java
Optional<StructureRule> structureRule(ResourceLocation id);
Optional<Region> minimumStructureRegion(ResourceLocation id);
boolean structureExplicitlySuppressed(ResourceLocation id);

boolean naturalEntityNamespaceSuppressed(ResourceLocation id);
Optional<NaturalEntityRule> naturalEntityRule(ResourceLocation id);

Map<ResourceLocation, StructureRule> structures();
Map<ResourceLocation, NaturalEntityRule> naturalEntities();
List<PrefixStructureRule> structurePrefixes();
```

This is useful for audits and for rejecting quest candidates that the geography layer itself says cannot exist at a proposed location.

## Quest-side rules

1. Classify a board by the **board block's server position**, never the player's current position.
2. Never reimplement cardinal-sector/noise math.
3. Client region sync may decorate UI, but it is not authoritative for quest issuance.
4. A structure quest must still obtain a real usable generated target. `structureAllowed(...)` only tells us whether a structure is legal at a location.
5. Natural-mob bounty pools should be built from actual zone-registry/entity audit data, not guessed from theme alone.
6. `SHARED_CORE` should use generic Hearthlands work.
7. `CARDINAL_TRANSITION` can mix shared and regional Hearthlands work.
8. `ESTABLISHED` can strongly use the exact macro-region + radial-tier catalog.

## Current boundary

This API is enough to proceed with:

- regional board classification
- region/tier content catalogs
- board UI labels
- filtering quest candidates by geography
- joining developer structure/mob exports to quest design

It does **not** itself solve:

- nearest useful generated structure selection
- map creation
- proof-item insertion into an exact structure instance
- same-board redemption
- authored one-time story state
