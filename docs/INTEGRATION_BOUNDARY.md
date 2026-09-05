# Integration Boundary

This file exists to prevent the quest project from inventing fake interfaces while the zoning/world-substrate project is still under active development.

## Safe to build now

These artifacts do not depend on the final world API:

- Bountiful 6.0.4 custom-only configuration
- objective/reward reliability documentation
- broad repeatable bounty pools
- quest archetype definitions
- regional quest/reward design catalog
- proof-item registry/specification
- named reward weapon specification
- item/entity/structure registry audits
- balance tables
- static validators
- in-game smoke-test procedures
- neutral data models that describe intended quest content without claiming runtime support

## Must wait for the developer thread

Do not implement these until the final world substrate is known:

### Regional board assignment

Need authoritative answers for:

```text
getMacroRegion(pos)
getRadialZone(pos)
```

or whatever equivalent API the developer thread actually ships.

### Useful structure targeting

Need final structure registry/region rules and a stable method to locate permitted generated instances.

### Regional cartographer maps

Need target selection + map creation integration.

### Same-board redemption

Needs chosen runtime owner for bounty source metadata and redemption interception.

### Structure proof placement

Needs chosen worldgen hook/data path for reliably adding a unique proof item to the intended generated structure.

### Authored named quest issuance

Bountiful's broad random generator is intentionally combinatorial. Signature objective/reward pairs may need a narrow decree or direct custom issuance. Wait until the runtime owner is decided.

### Main-story state

Regional boss completion, final relic requirements, Stronghold/End progression, and one-time story flags are outside the current baseline.

### Rescue-pet ownership transfer

Do not assume a command/NBT trick. Build this only after a deterministic method is selected and tested.

---

# Interface we expect conceptually, not literally

The quest layer will eventually need capabilities equivalent to:

```text
WorldRegion classify(BlockPos)

WorldRegion:
    macroRegion = NORTH | EAST | SOUTH | WEST | CORE/TRANSITION
    radialTier = HEARTHLANDS | FRONTIER | WILDLANDS | DREAD_REACHES
```

and a target-selection service capable of returning a real generated destination under constraints.

These names are documentation placeholders only.

**Do not write production code against them until the developer project hands off its actual API.**

---

# Data we want from the developer thread

When available, request/export:

## Biomes

- registry ID
- assigned macro-region(s)
- transition/common status
- radial/intensity notes if relevant

## Structures

- registry ID
- macro-region assignment(s)
- minimum radial tier
- actual generation mechanism
- contained mobs/bosses
- important loot/value notes
- whether the structure is safe for quest proof insertion

## Natural mobs

- entity registry ID
- macro-region assignment(s)
- minimum radial tier
- daylight/night viability
- spawn mechanism
- authored/raid exemptions

## Existing bosses

- exact entity ID
- containing structure ID if any
- spawn mechanism
- region/tier assignment

Once those exist, the quest catalog can be joined to real world content without guessing.

---

# Principle

The zoning project owns the truth of **where things can exist**.

The quest project owns the truth of **what people know about those things, why the player might care, and what the player receives for engaging with them**.

Neither project should duplicate the other's core logic.
