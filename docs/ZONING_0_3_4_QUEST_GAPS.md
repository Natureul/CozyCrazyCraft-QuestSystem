# CozyCrazyZones 0.3.4 — Quest-Relevant Gaps / Follow-Up

This is **not** a criticism of the zoning build. It is a narrow integration audit: what can the quest layer safely infer from `CozyCrazyZones 0.3.4` today, and what should it avoid assuming until the developer thread expands the world rules?

## Important default behavior

Direct bytecode inspection shows:

```java
CozyZonesApi.structureAllowed(...)
```

returns `true` when:

- the structure's namespace is not globally suppressed; and
- no explicit/prefix structure rule exists.

Likewise:

```java
CozyZonesApi.naturalEntityAllowed(...)
```

returns `true` when:

- the entity's namespace is not globally suppressed; and
- no explicit natural-entity rule exists.

Therefore:

> **absence from ZoneRuleRegistry does not mean “not part of this region.” It currently means “the zoning API does not constrain it.”**

The quest layer must not mistake `allowed == true` for a positive thematic assignment.

---

# Already strong enough for quest binding

Current 0.3.4 rules give excellent explicit anchors for:

## Sunscar

- `dungeons_enhanced:desert_tomb` — Hearthlands, SOUTH, Cardinal Transition+
- `dungeons_enhanced:desert_temple` — Frontier, SOUTH, Established
- `betterdeserttemples:desert_temple` — Frontier, SOUTH, Established
- `mowziesmobs:umvuthana_grove` — Wildlands, SOUTH, Established
- `cataclysm:cursed_pyramid` — Dread Reaches, SOUTH, Established

Natural ecology includes explicit Sunscar Frontier candidates:

- `skarrier_mobs:snap`
- `skarrier_mobs:quake`
- `born_in_chaos_v1:spirit_guide`

This is currently the cleanest region for fully bound quest progression.

## Greenveil

- `dungeons_enhanced:jungle_monument` — Frontier, EAST, Established
- `betterjungletemples:jungle_temple` — Frontier, EAST, Established

Natural ecology explicitly includes:

- `mowziesmobs:foliaath` — Frontier
- `skarrier_mobs:dangle` — Frontier
- `skarrier_mobs:slither_spawner_dummy` — Frontier
- `skarrier_mobs:carniflore` — Wildlands
- `skarrier_mobs:slither_matriarch` — Wildlands

That is enough for multiple reliable East Frontier/Wildlands hunt and structure contracts once target locating/proof placement exists.

## Frostmarch

- `dungeons_enhanced:ice_pit` — Wildlands, NORTH, Established
- `mowziesmobs:frostmaw_spawn` — Wildlands, NORTH, Established

This gives the Frostmaw arc an authoritative anchor.

## Harvestlands

- `born_in_chaos_v1:dark_tower_*` prefix — Dread Reaches, WEST, Established
- `born_in_chaos_v1:sir_pumpkinhead` natural rule — Wildlands, WEST, Established

This gives the deep West a real boss/ecology anchor, although several desired pumpkin/grave structures are not yet explicitly regionalized.

---

# Gaps that matter to planned quest content

## 1. Aquamirae / Cornelia is not explicitly bound by ZoneRuleRegistry

The current loaded structure registry contains Aquamirae structures such as:

```text
aquamirae:outpost
aquamirae:shelter
aquamirae:ship
aquamirae:surface/arch
aquamirae:surface/spiral
```

but `ZoneRuleRegistry` currently has no explicit Aquamirae structure rule.

As a result, `structureAllowed()` alone cannot prove:

```text
NORTH + DREAD_REACHES
```

for those structures.

The physical biome/worldgen system may already make them effectively northern/frozen in practice, but the quest system should not treat that as an API-level guarantee yet.

**Request for zoning developer:** expose or document the authoritative North-Dread rule/path for the Ice Maze/Cornelia destination once final.

## 2. Lord Pumpkinhead is not directly represented as a final-destination rule

`born_in_chaos_v1:sir_pumpkinhead` is explicitly WEST + WILDLANDS.

`born_in_chaos_v1:dark_tower_*` is explicitly WEST + DREAD.

However, `born_in_chaos_v1:lord_pumpkinhead` is not currently an explicit natural rule and there is no explicit structure→Lord mapping in the zoning registry.

That may be intentional if his encounter is authored/structure-driven rather than natural.

**Quest requirement:** before `The Old Harvest` becomes live, document exactly how the intended Lord Pumpkinhead encounter is selected and how the quest system locates or reveals it.

## 3. Born in Chaos grave/pumpkin structures are not yet regionally constrained in the rule registry

The loaded structure registry contains many `born_in_chaos_v1:grave_*` structures, plus things such as:

```text
born_in_chaos_v1:farm
born_in_chaos_v1:infernal_pumpkin
born_in_chaos_v1:clown_caravan_*
```

The current rule registry does **not** provide a West/Harvestlands rule for these families.

If the developer intends graveyards/pumpkin structures to be an important western ecology—as discussed—this still needs an authoritative world rule or classification export.

Until then, Harvestlands quests such as `The Gravekeeper's Bell` should remain design-ready but not hard-bind to one of those structures.

## 4. Many biome-obvious vanilla/YUNG structures rely on biome worldgen rather than ZoneRuleRegistry

Examples currently loaded but not explicitly rule-bound include:

- vanilla desert pyramid / jungle pyramid / villages
- Better Mineshaft biome variants
- many smaller Dungeons Enhanced structures
- Beautify botanist houses

That may be completely fine because the macro-biome system itself can make the placement ecologically correct.

For quest authoring, though, we need to distinguish:

```text
explicit zoning rule
vs.
implicitly safe because biome placement itself is authoritative
```

A future registry export should ideally include this distinction.

## 5. Natural entity rules are intentionally partial

The current registry explicitly regionalizes several good East/South threats and Sir Pumpkinhead, but many creatures we may want for flavor/hunts are absent from the table.

Because unruled entities default to `allowed`, the quest system should not say:

> “`naturalEntityAllowed == true`, therefore this is a Frostmarch mob.”

Instead, hunt-pool construction should prefer:

1. entities with explicit ZoneRuleRegistry macro assignment; or
2. entities whose biome/spawn placement the developer export explicitly certifies as region-safe.

This is especially important for the future pumpkin-person ladder in Harvestlands and cold-region natural enemies in Frostmarch.

## 6. `fallen_chaos_knight` is explicitly disabled

The rule exists at Wildlands but has:

```text
enabled = false
```

with note that Scarlet Persecutor suppression interaction still needs testing.

Do not make a bounty depend on a naturally spawning Fallen Chaos Knight until that flag changes or an authored encounter is deliberately supplied.

---

# Quest-side policy until these are resolved

The quest project will use three levels of world binding:

### A — EXPLICIT_RULE

Structure/entity has a matching CozyCrazyZones rule whose radial/macro/influence assignment fits the quest.

Safe for authored pool work, subject to target existence.

### B — BIOME_CERTIFIED

No explicit ZoneRuleRegistry rule, but the zoning developer certifies that biome/worldgen placement itself confines it correctly.

Safe after that export/documentation exists.

### C — UNBOUND

Loaded content exists, but neither an explicit rule nor certified regional placement exists.

Do not make it a required regional quest target yet.

This prevents the quest layer from accidentally outrunning the world substrate.
