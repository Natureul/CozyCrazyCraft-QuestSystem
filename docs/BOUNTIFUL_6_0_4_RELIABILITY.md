# Bountiful 6.0.4 Reliability Matrix

Target: `Bountiful-6.0.4+1.20.1-forge.jar` on Minecraft 1.20.1.

This document is based on the **exact `1.20.1` branch** of `ejektaflex/Bountiful`, not newer 1.20.4+/7.x/8.x documentation. Newer Bountiful features must not be assumed to exist here.

## Reliability policy

- **GREEN** — native behavior is simple enough and verified in source. Safe for shipped repeatable bounties.
- **YELLOW** — Bountiful supports it, but a specific trigger/NBT/command interaction must pass an in-game test before content depends on it.
- **RED / DEFERRED** — not a native Bountiful guarantee, instance-specific behavior is impossible natively, or it depends on custom CozyCrazyCraft integration that does not exist yet.

---

## GREEN: exact item objectives

Pool type:

```json
"type": "item"
```

Use for:

- supply jobs
- cooked-food deliveries
- regional-material deliveries
- unique proof/recovery items

Verified behavior in `BountyTypeItem.kt`:

- scans the player's **main inventory**
- matches the registry item ID
- counts matching stacks
- removes the required quantity when the bounty is cashed in
- if used as a reward, gives the item and drops overflow at the player's feet when inventory is full

### Critical limitation: objective NBT is not a discriminator

The objective progress code compares the **item registry ID**, not the stack's NBT.

Therefore this is unsafe:

```text
cozycrazycraft:quest_token + NBT Stablemaster
cozycrazycraft:quest_token + NBT Gravekeeper
```

Bountiful would treat both as the same objective item.

Use distinct registered item IDs instead:

```text
cozycrazycraft:stablemasters_seal
cozycrazycraft:gravekeepers_bell
cozycrazycraft:surveyors_tag
```

This is the required design for structure proof items.

### Inventory caveat

Because the exact source scans `player.inventory.main`, do not design a delivery objective around an item that only exists in armor/offhand slots unless tested and deliberately moved into the main inventory for redemption.

Source:
`ejektaflex/Bountiful` → branch `1.20.1` → `BountyTypeItem.kt`

---

## GREEN: item-tag objectives

Pool type:

```json
"type": "item_tag"
```

Use for flexible requests such as:

- any logs
- any wool
- other genuinely interchangeable material families

Verified behavior:

- scans the player's main inventory
- matches members of the requested item tag
- removes matching items on redemption
- is objective-only; `item_tag` does not provide a native reward implementation

### Design caution

The objective consumes whichever matching tag items are found. Do not use broad tags when the player may accidentally surrender valuable variants.

Source:
`BountyTypeItemTag.kt` on the exact `1.20.1` branch.

---

## GREEN: entity-kill objectives

Pool type:

```json
"type": "entity"
```

Use for:

- ordinary extermination jobs
- named miniboss/boss Great Hunts
- regional monster-control contracts

Verified behavior in the exact 1.20.1 source:

- matches the entity type registry ID
- increments matching objectives on carried bounty items
- pet kills were explicitly supported by the 6.x line
- kill credit is deliberately generous around the killer/victim and related player/owner references

### Important limitation: entity type is not entity instance

Native Bountiful can reliably express:

> Kill one Frostmaw.

It cannot natively express:

> Kill **the particular Frostmaw at the mapped cave 2,000 blocks north of this village**.

If the player carries that bounty and kills another entity of the same type, the native entity objective can count it.

Therefore:

- generic or rare Great Hunts = GREEN
- exact mapped-instance hunts = require future CozyCrazyCraft quest-state/target glue if exact-instance enforcement is important

Source:
`BountyTypeEntity.kt` and `BountifulSharedApi.kt`, exact `1.20.1` branch.

---

## GREEN: ordinary item rewards

Pool type:

```json
"type": "item"
```

Good for:

- emeralds
- food
- arrows
- utility items
- verified modded item IDs
- Acacia Blossoms, saddles, insulation materials, backpack unlock items, accessories, etc., once their IDs are verified

Bountiful can attach NBT to an item reward. However, any **mod-specific complex NBT format** (Quality Equipment qualities, specialized enchanted modded weapons, custom component-like data) remains YELLOW until the exact output stack is captured and tested.

---

## YELLOW: criteria objectives

Pool type:

```json
"type": "criteria"
```

Bountiful 6.0.4 can listen to Minecraft advancement-style criterion triggers while a bounty is carried.

This is real functionality, but it is deliberately **not part of the first CozyCrazyCraft baseline**.

Reasons:

1. `BountyTypeCriteria.isValid()` in this version effectively accepts the entry without deeply validating whether the configured criterion/conditions are correct.
2. Bountiful explicitly skips at least `minecraft:tick` and `minecraft:enter_block` style criteria in its hook path.
3. Advancement triggers can have nuanced condition JSON.
4. Bountiful criteria do not provide arbitrary advancement-style memory/state machines.
5. A malformed or misunderstood criterion can create a bounty that appears valid but never progresses.

Promising low-complexity candidates to test later include:

- `minecraft:fishing_rod_hooked`
- `minecraft:tame_animal`
- `minecraft:breed_animals`
- `minecraft:item_used_on_block`
- `minecraft:placed_block`

The Bountiful 1.20.1 built-in fisherman pool itself uses `minecraft:fishing_rod_hooked`, which is a useful proof that criterion objectives are intended functionality. Still, every specific CozyCrazyCraft criterion must pass the test matrix before being marked GREEN.

### Rule

**Do not ship a criterion-based quest merely because vanilla has an advancement trigger with the same name. Test the exact JSON conditions in this pack.**

---

## YELLOW: command rewards

Pool type:

```json
"type": "command"
```

The exact 1.20.1 implementation can execute a server command on redemption and supports substitutions including player name/position and bounty amount.

This is potentially useful later for:

- issuing an authored named reward through a function
- setting one-time quest state
- handing ownership of a rescued animal to the player
- triggering a controlled CozyCrazyCraft function

But it should not be our default solution.

### Rule

Use ordinary item/entity mechanics whenever possible. A command reward becomes GREEN only when the exact command/function has a deterministic test and no fragile environmental assumptions.

---

## RED / DEFERRED: exact structure completion without proof

Bountiful does not natively know that a particular generated structure instance has been "cleared."

Avoid quests such as:

> Clear this exact Stable.

unless completion is represented by something Bountiful can reliably observe.

Preferred robust pattern:

1. target structure contains a unique CozyCrazyCraft proof item
2. player enters and reaches that item
3. bounty has an ordinary `item` objective
4. item is consumed on redemption

Example:

```text
Dungeons Enhanced Stable
→ cozycrazycraft:stablemasters_seal
→ return seal to board
```

This converts an ambiguous structure-clear problem into a GREEN item-delivery objective.

Proof-item **placement into the correct generated structure** is still deferred until the zoning/structure project exposes its final integration path.

---

## RED / DEFERRED: same-board redemption

Bountiful tracks reputation per board, but native bounty completion should not yet be assumed to enforce our desired rule that a physical bounty must be redeemed only at the exact board that issued it.

Desired future CozyCrazyCraft behavior:

```text
bounty source = dimension + board position + board UUID/identity
```

Redemption checks the source before accepting it.

Do not implement this until the world/quest companion architecture is settled.

---

## RED / DEFERRED: dynamically locating a useful exact structure

Bountiful itself is not our structure-navigation engine.

The future quest layer needs a shared locator such as:

```text
findNearestUsefulStructure(
  structureFamily,
  issuingBoard,
  macroRegion,
  radialTier,
  minDistance,
  preferredDistance,
  maxDistance,
  unexploredPreference,
  repeatGuard
)
```

This waits for the developer thread's authoritative geography and structure registry.

---

## RED / DEFERRED: complicated bespoke event objectives

Do not design current content around custom event detectors such as:

- survive one exact tornado
- escort a moving caravan NPC for ten minutes
- photograph a particular target and parse the resulting image
- rebuild an arbitrary village building and detect its exact final shape
- protect a specific NPC through a scripted wave

These may be fun future features, but they are not justified until we deliberately build and test the supporting system.

Prefer simple reliable verbs now:

- bring
- kill
- recover
- fish (after criteria test)
- tame (after criteria test)
- breed (after criteria test)

---

# Bountiful generation behavior that affects quest design

Bountiful's random board generator does **not** pair an objective and reward because their narrative names match.

At a high level it:

1. selects one or more reward entries from the active decree's reward pools
2. calculates total reward worth
3. selects one or two objective entries with a comparable worth range

Therefore a broad pool containing:

- "recover botanist satchel"
- "kill zombies"
- "bring wheat"

and rewards containing:

- Acacia Blossoms
- saddle
- arrows

can produce combinations we never intended.

### Consequence

Use Bountiful in two modes:

### A. Broad repeatable local pools

Good for interchangeable jobs/rewards:

- supplies
- basic mob control
- modest emeralds
- food
- arrows
- ordinary utility

### B. Narrow authored/special decrees or future direct issuance

Use when an objective needs a particular signature reward:

> Elephant Keeper → Acacia Blossoms

> Stablemaster's Seal → specific stable reward

> Frostmaw Great Hunt → White Reach

Do **not** put signature rewards into a broad combinatorial reward pool and hope Bountiful chooses the right story pairing.

---

# Source-of-truth links

Exact branch:

- https://github.com/ejektaflex/Bountiful/tree/1.20.1

Key files:

- `common/src/main/java/io/ejekta/bountiful/bounty/types/builtin/BountyTypeItem.kt`
- `.../BountyTypeItemTag.kt`
- `.../BountyTypeEntity.kt`
- `.../BountyTypeCriteria.kt`
- `.../BountyTypeCommand.kt`
- `common/src/main/java/io/ejekta/bountiful/bridge/BountifulSharedApi.kt`
- `common/src/main/java/io/ejekta/bountiful/content/BountyCreator.kt`
- `common/src/main/java/io/ejekta/bountiful/config/ResourceLoadStrategy.kt`

Any design assumption that conflicts with the exact 1.20.1 source loses.
