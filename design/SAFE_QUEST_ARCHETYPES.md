# Mechanically Safe Quest Archetypes

This catalog is intentionally constrained by what Bountiful 6.0.4 can observe reliably **without a new custom event system**.

## Tier A — can be done with stock Bountiful now

### Supply request

Mechanic:

```text
item objective
```

Examples:

- bring wheat
- bring string
- bring regional food/material
- bring exact crafted food

Reliability: GREEN.

### Flexible material request

Mechanic:

```text
item_tag objective
```

Examples:

- any logs
- any wool

Reliability: GREEN, with the caveat that matching items are consumed on redemption.

### Extermination

Mechanic:

```text
entity objective
```

Examples:

- kill zombies
- kill spiders
- later: kill audited regional entity IDs

Reliability: GREEN.

### Great Hunt

Mechanic:

```text
entity objective, amount 1
```

Examples:

- Frostmaw
- Umvuthi
- Sir Pumpkinhead
- Wroughtnaut

Reliability of **entity-type kill tracking**: GREEN.

Exact-instance/location enforcement: not native; do not claim it.

### Ordinary payment

Mechanic:

```text
item reward
```

Examples:

- emeralds
- food
- arrows
- saddle
- verified mod utility item

Reliability: GREEN once registry ID exists and the reward does not require untested custom NBT.

---

# Tier B — simple future glue, no exotic event detector

These are high-priority because the supporting custom logic is small and deterministic, but they should wait until the runtime/world integration is ready.

### Recovery Contract

Mechanic:

```text
structure contains unique proof item
Bountiful objective = exact item
```

Runtime need:

- deterministic proof-item placement in the correct structure family

Bountiful side remains GREEN.

Examples:

- Stablemaster's Seal
- Gravekeeper's Bell
- Watchman's Ledger
- Sealed Illager Orders
- Expedition Notebook

This should be the default way to represent "go into this structure and accomplish something" when exact clear detection is unnecessary.

### Mapped Recovery Contract

Same as Recovery Contract, plus a real structure map chosen by the future useful-target locator.

Runtime need:

- choose actual useful target
- issue/sell map

No new event detector.

### Authored one-to-one bounty

Mechanic:

- generate a bounty from a deliberately narrow one-objective/one-reward content set
- or directly construct/issue the intended Bountiful bounty using the final companion runtime

Runtime need:

- controlled issuance so special decrees do not randomly appear on wrong boards

Useful for:

- Acacia Blossom regional quest
- named signature weapon quests
- special reputation milestones

### Same-board redemption

Mechanic:

- stamp source board identity on bounty
- reject redemption elsewhere

No complicated world event detection is required, but redemption interception/storage architecture must be chosen first.

---

# Tier C — supported by Bountiful criteria, but test before use

Potentially elegant, but not GREEN merely because the trigger exists.

### Fishing job

Potential trigger:

```text
minecraft:fishing_rod_hooked
```

Strong candidate because built-in 1.20.1 Bountiful content uses it.

### Tame-an-animal job

Potential trigger:

```text
minecraft:tame_animal
```

Could support quests where the reward teaches a regional taming system.

### Breeding job

Potential trigger:

```text
minecraft:breed_animals
```

Could support livestock/farming work.

### Place/use a block

Potential triggers:

```text
minecraft:placed_block
minecraft:item_used_on_block
```

Could eventually support tiny restoration/infrastructure jobs.

All remain YELLOW until the exact full-pack criterion JSON passes `docs/TEST_PLAN.md`.

---

# Tier D — do not build around this yet

### Exact escort mission

Too many AI/pathing/failure-state dependencies.

### "Survive this exact storm"

Requires custom Weather2 integration and robust event attribution.

### Photograph a particular entity/place

Fun concept, but image/camera-state validation is not currently a proven quest primitive.

### Detect that an arbitrary structure is fully repaired

Requires a bespoke block-state comparison/state machine.

### Detect that every original mob in a generated dungeon is dead

Fragile and unnecessary when a proof item solves the actual gameplay requirement.

### Timed defense script around a quest NPC

Could be authored later, but this is a custom event system, not ordinary Bountiful content.

---

# Design rule

When two quest ideas feel equally fun, prefer the one whose completion state can be represented as:

```text
I have the item
I killed the entity type
I performed a tested criterion
```

That gives us authored flavor without fragile machinery.
