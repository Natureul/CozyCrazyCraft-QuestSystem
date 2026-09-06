# Automatic Regional Bounty Boards — Runtime Plan

The current Bountiful JSON proves the quest content can exist. The next runtime problem is making a naturally generated board choose the **right CozyCrazyCraft decree automatically**.

This should be a very small integration layer, not a replacement quest engine.

---

## Why a runtime hook is needed

Bountiful 1.20.1's `BoardBlockEntity` initializes a pristine board by creating a decree with `DecreeItem.create()`. In the stock implementation that call chooses a random loaded decree. The board then generates its initial bounties from whatever decree ended up in its private decree inventory.

That is incompatible with cardinal geography:

```text
Frostmarch village + random decree = could receive Sunscar work
```

Our config can prevent vanilla/default Bountiful content from loading, but config alone cannot tell Bountiful that one physical board is north and another is south.

CozyCrazyZones 0.3.6 already provides exactly the missing fact:

```java
RegionalCell cell = CozyZonesApi.regionalCellAt(level, boardX, boardZ);
```

---

# Minimal behavior

Before a **pristine** Bountiful board performs its first population, the integration layer chooses one CozyCrazyCraft decree from the board's real position.

Initial H1 mapping:

```text
SHARED_CORE + HEARTHLANDS
  -> ccc_local_notices

CARDINAL_TRANSITION/ESTABLISHED + HEARTHLANDS + NORTH
  -> ccc_hearth_north

... + EAST
  -> ccc_hearth_east

... + SOUTH
  -> ccc_hearth_south

... + WEST
  -> ccc_hearth_west
```

The existing regional Hearthlands decrees already contain both their regional pool and the ordinary local pool. That is a good first implementation for villages around the ~1,000-block opening because they feel regionally recognizable without losing mundane village work.

Future mappings add dedicated `frontier_*`, `wildlands_*`, and `dread_*` decree families once those live pools are ready.

---

# Safest integration point

The best first implementation is a tiny Bountiful compatibility hook **before `BoardBlockEntity.tryInitialPopulation()` creates its random decree**.

Conceptually:

```java
if (board is pristine && board decree slots are empty) {
    RegionalCell cell = CozyZonesApi.regionalCellAt(level, pos.getX(), pos.getZ());
    String decreeId = BoardPolicy.decreeFor(cell);
    board decree slot = Bountiful DecreeItem.create(decreeId);
}

// let Bountiful continue doing its own normal initial bounty generation
```

This is deliberately narrow:

- Bountiful still creates bounty items.
- Bountiful still tracks kills/items.
- Bountiful still refreshes board slots.
- Bountiful still handles reputation and redemption.
- We only choose the source decree before the first random choice can happen.

That is much safer than maintaining a parallel board inventory or reimplementing Bountiful's generator.

---

# Implementation technique

Bountiful's 1.20.1 board keeps its decree inventory private. A small Mixin/accessor is likely the cleanest compatibility technique:

1. inject at the head of `tryInitialPopulation()`;
2. access the board's decree inventory through a dedicated accessor/shadow;
3. if it is empty and the board is pristine, insert `DecreeItem.create(exactId)`;
4. return control to Bountiful.

Do **not** cancel Bountiful's method unless testing proves necessary.

Do **not** tick-scan every loaded board and rewrite its NBT every second.

Do **not** modify established player-used boards merely because the zoning config changes.

---

# Existing boards and upgrades

A board that has already generated bounties is not pristine. We should not silently wipe it.

Version-one policy:

```text
new/pristine board -> automatic regional assignment
existing populated board -> leave alone
```

For development worlds we can expose an operator-only reset/reclassify command later.

That avoids breaking accepted bounties or changing a town's board under the player.

---

# Transition handling

The first village is intentionally around the region transition/established boundary. We do **not** need a giant special system for this.

For v1:

- Shared Core board = generic Local Notices.
- Once a board has a real macro-region in the cardinal transition, use the appropriate regional Hearthlands decree.
- Those decrees already mix regional and ordinary local objectives.

If playtesting makes transition villages feel too specialized, we can later create lighter transition pools. Do not preemptively duplicate dozens of JSON entries before we know this is a problem.

---

# Tier progression

A physical board's radial tier is determined by **where the settlement exists**, not by player level.

This produces the intended knowledge gradient:

```text
Hearthlands village
  ordinary/local work + first regional identity

Frontier settlement
  regional Frontier work + some lower-tier daily life

Wildlands settlement/outpost
  expedition work + dangerous ecology + serious leads

Dread settlement/outpost
  scarce severe contracts + old lower-tier life + story knowledge
```

Bountiful's per-board reputation still controls quality/rarity inside that local pool. Geography controls what kind of work can exist at all.

---

# Structure quests are not generated blindly

Automatic regional decree assignment alone is safe for pure item/entity objectives. It is **not enough** for a structure recovery contract.

A structure posting must additionally ask the target service:

```text
is there a valid real target inside this posting's travel envelope?
```

If no:

```text
withhold structure posting
fill board with ordinary/ecology work instead
```

See [`TARGETING_AND_DISTANCE_POLICY.md`](TARGETING_AND_DISTANCE_POLICY.md).

This is why structure contracts should not all be dumped directly into broad Bountiful random pools before the target service exists.

---

# Signature quests

A named signature quest such as:

```text
The Sleeping Mountain -> Frostmaw
The Sunbird -> Umvuthi
The Headless Road -> Sir Pumpkinhead
```

should not become a random every-day posting merely because the board is in the correct cell.

Signature issuance can initially be very simple:

- narrow one-objective/one-reward decree,
- prerequisite/story flag or board reputation,
- explicitly inserted posting when eligible,
- ordinary Bountiful completion primitive underneath.

Again: special **selection**, ordinary **completion**.

---

# Performance rule

Regional board classification is cheap and should occur only when needed.

For a new board:

```text
one RegionalCell lookup
one decree choice
normal Bountiful behavior thereafter
```

There is no reason to recompute the board's cardinal identity every tick. A board is a fixed block position.

Store/stamp the chosen region/tier on our own lightweight board metadata if later UI or migration needs it.

---

# UI consequence

Once the board is stamped, the story UI can display authoritative labels such as:

```text
FROSTMARCH NOTICE BOARD
Hearthlands
```

or:

```text
SUNSCAR FRONTIER CONTRACTS
```

The label is presentation. The server-side decree assignment remains the authority.

---

# Test order

1. Place a new board inside Shared Core and verify only `ccc_local_notices` content.
2. Place/spawn boards in each H1 macro-region and verify the matching regional decree.
3. Reload world and confirm assignment persists.
4. Cross a macro boundary and confirm an already placed board does not change identity.
5. Confirm existing populated boards are not wiped.
6. Confirm normal Bountiful board refresh still works.
7. Confirm `/bo test` and manual decree commands remain functional for debugging.
8. Only then add Frontier/Wildlands/Dread automatic mappings.

This gives us an automatic regional board system with a very small code surface.
