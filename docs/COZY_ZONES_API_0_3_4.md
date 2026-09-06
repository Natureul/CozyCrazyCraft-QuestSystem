# CozyCrazyZones 0.3.4 — Quest Integration Contract

This document is based on direct inspection of the current `CozyCrazyZones-0.3.4` bytecode handed to the quest project.

It replaces the earlier placeholder idea of `getMacroRegion()` / `getRadialZone()` with the **actual public API currently shipped by the zoning mod**.

> Do not duplicate this geography logic in the quest mod. Query CozyCrazyZones.

---

# Public geography API

Class:

```java
com.natureul.cozycrazyzones.CozyZonesApi
```

Current public static methods:

```java
double distanceFromSpawn(ServerLevel level, double x, double z)
Region regionAt(ServerLevel level, double x, double z)
Region regionForDistance(double distance)

RegionalInfluenceBand influenceBandAt(ServerLevel level, double x, double z)
RegionalInfluenceBand influenceBandForDistance(double distance)

double regionalStrengthAt(ServerLevel level, double x, double z)
double regionalStrengthForDistance(double distance)

MacroRegion macroRegionAt(ServerLevel level, double x, double z)
MacroRegion macroRegionForOffset(long seed, double dx, double dz)
double macroBoundaryStrengthAt(ServerLevel level, double x, double z)

RegionalCell regionalCellAt(ServerLevel level, double x, double z)

boolean structureAllowed(ServerLevel level, ResourceLocation structureId, double x, double z)
boolean naturalEntityAllowed(ServerLevel level, ResourceLocation entityId, double x, double z)
boolean isDaytimeCandidate(ResourceLocation entityId)
```

For most quest-board decisions, prefer **one call to `regionalCellAt`** instead of independently rebuilding several classifications.

---

# RegionalCell

Current record fields/accessors:

```java
Region radialZone()
MacroRegion macroRegion()
RegionalInfluenceBand influenceBand()
double distanceFromSpawn()
double regionalStrength()
double macroBoundaryStrength()

boolean sharedCore()
String ecologyDisplayName()
String cellDisplayName()
```

This is the natural board-classification object.

Conceptually:

```text
Board at X/Z
  ↓
CozyZonesApi.regionalCellAt(level, x, z)
  ↓
radialZone        = FRONTIER
macroRegion       = EAST
influenceBand     = ESTABLISHED
  ↓
Greenveil Frontier board policy
```

---

# Radial zones

`Region` values:

| Enum | ID | Display | Current subtitle |
|---|---|---|---|
| `HEARTHLANDS` | `hearthlands` | Hearthlands | Home country — lived-in, but never harmless. |
| `FRONTIER` | `frontier` | The Frontier | Beyond the familiar roads. |
| `WILDLANDS` | `wildlands` | Wildlands | Prepare for an expedition, not a stroll. |
| `DREAD_REACHES` | `dread_reaches` | Dread Reaches | The known world has grown very far away. |

Current default boundaries from `CozyZonesConfig`:

```text
Hearthlands → Frontier: 2500
Frontier → Wildlands:   5500
Wildlands → Dread:      9000
```

These are config-backed values. Quest code must not hard-code the numbers as authoritative runtime truth.

---

# Cardinal macro-regions

`MacroRegion` values:

| Enum | ID | Display | Adjective |
|---|---|---|---|
| `NORTH` | `north` | Frostmarch | Frostmarch |
| `EAST` | `east` | Greenveil | Greenveil |
| `SOUTH` | `south` | Sunscar | Sunscar |
| `WEST` | `west` | Harvestlands | Harvest |

Use these enum values as mechanical identities and the display/adjective methods for player-facing copy where appropriate.

---

# Influence bands

`RegionalInfluenceBand` values:

| Enum | ID | Display |
|---|---|---|
| `SHARED_CORE` | `shared_core` | Shared Core |
| `CARDINAL_TRANSITION` | `cardinal_transition` | Cardinal Transition |
| `ESTABLISHED` | `established` | Established Region |

Current default distances:

```text
0–700:        Shared Core
700–1200:     Cardinal Transition
1200+:        Established cardinal ecology
```

The current config comments explicitly describe the Shared Core as neutral/shared countryside and the 700→1200 interval as the organic cardinal transition band.

Again, code should use the API/config-backed classification rather than reproducing these numbers.

---

# Macro borders are intentionally not hard quadrants

Current config exposes:

```text
macroBorderBlendDegrees = 11.0
```

The macro-region resolver also applies seed-derived angular warping around the actual Overworld spawn anchor. The quest layer should therefore treat the returned `MacroRegion`/`RegionalCell` as authoritative rather than assuming `+X = East` with a hard geometric seam.

This matters especially for first villages near a regional border.

---

# Structure legality API

Before offering a structure-specific quest/map candidate:

```java
CozyZonesApi.structureAllowed(level, structureId, x, z)
```

should agree that the structure is legal at the candidate position.

For audit/debug tooling, the zoning mod also exposes:

```java
ZoneRuleRegistry.structureRule(ResourceLocation)
ZoneRuleRegistry.minimumStructureRegion(ResourceLocation)
ZoneRuleRegistry.structureExplicitlySuppressed(ResourceLocation)
ZoneRuleRegistry.structures()
ZoneRuleRegistry.structurePrefixes()
```

Do not infer a quest structure's region from its name if the zoning registry already has a rule.

---

# Natural entity legality API

For a quest that asks the player to hunt a naturally occurring regional mob, verify the target is appropriate for that cell:

```java
CozyZonesApi.naturalEntityAllowed(level, entityId, x, z)
```

Audit/debug access:

```java
ZoneRuleRegistry.naturalEntityRule(ResourceLocation)
ZoneRuleRegistry.naturalEntityNamespaceSuppressed(ResourceLocation)
ZoneRuleRegistry.naturalEntities()
```

And:

```java
CozyZonesApi.isDaytimeCandidate(entityId)
```

can inform flavor/design, but **must not be used as a promise that the mob will definitely be standing around during the day**. It is a zoning-side candidate flag, not a spawn guarantee.

Authored raids, structure spawns, summons, commands, and other non-natural creation paths remain a separate concern and should not be rejected merely because the natural-spawn ecology would reject that entity there.

---

# Current default world shape relevant to quests

The quest layer should interpret the geography approximately like this:

```text
0–700
  Shared Core
  ordinary local work

700–1200
  Cardinal Transition
  mixed core + first regional Hearthlands work
  ideal first-village band

1200–2500
  Established regional Hearthlands
  strongly regional T1 work

2500–5500
  Regional Frontier

5500–9000
  Regional Wildlands

9000+
  Regional Dread Reaches
```

This is why `data/board_policy.json` mixes Shared Core and current-region Hearthlands work in transition villages rather than flipping instantly from one theme to another.

---

# Quest integration rules

1. **Classify the board, not merely the player.** Server-side quest availability should be based on the issuing board's actual position.
2. **UI can use synchronized/current regional state cosmetically, but server classification owns truth.**
3. **Use `RegionalCell` for board-pool selection.**
4. **Use `structureAllowed` before considering a structure destination.**
5. **Use `naturalEntityAllowed` before putting a regional natural-mob hunt in a board pool.**
6. **Do not duplicate boundary math.**
7. **Do not turn `isDaytimeCandidate` into a bespoke daytime kill objective.** Ordinary entity-kill tracking remains enough.
8. **Maps/locators still need a real generated-instance search.** The zoning API tells us where a structure is legal, not where a particular generated instance is.
9. **Proof-item placement remains quest-layer/worldgen integration work.** The zoning API does not by itself insert quest proof objects into structures.

---

# Version boundary

This contract is explicitly for:

```text
CozyCrazyZones 0.3.4
Minecraft Forge 1.20.1
```

If the developer thread changes public API signatures or rule semantics in a later CozyCrazyZones build, update this document and the machine-readable world bindings before changing quest runtime code.
