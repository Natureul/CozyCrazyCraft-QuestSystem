# First Village Vertical Slice

This is the first quest experience to make playable once the quest runtime begins consuming CozyCrazyZones.

## Observed world behavior

The current zoning build is already reserving first-village candidates around the intended ~1,000-block band and can identify the candidate's macro-region/influence band. A recent test selected a village roughly 1,136 blocks from the shared spawn in **Frostmarch / CARDINAL_TRANSITION**.

That is almost exactly the design target for the opening quest layer.

## Desired first-time flow

```text
Starter house
  ↓
first-settlement map / knowledge
  ↓
~900–1,200 block trip through ordinary → emerging cardinal terrain
  ↓
first village in CARDINAL_TRANSITION
  ↓
notice board + cartographer
  ↓
mostly ordinary Hearthlands work
  + first regional Frostmarch/Greenveil/Sunscar/Harvestlands flavor
  ↓
first mapped/recovery adventure
  ↓
useful local reward + outward lead
```

The first village is not a full RPG quest hub. It is the player's first proof that settlements know things about the surrounding world.

---

# Board composition

Use the current `data/board_policy.json` transition rule:

- ~55% Shared Core Hearthlands work
- ~45% current-region Hearthlands work

Target **4–7 visible postings**, normally something like:

1. two mundane/local jobs
2. one local watch/hunt job
3. one regional ecology/survival job
4. zero or one mapped/recovery contract if a valid nearby structure exists
5. rare authored service/signature posting only when intentionally issued

The board must never invent a recovery contract if no suitable target structure can be resolved.

---

# Example: first village is Frostmarch transition

A healthy first opening might show:

### Wool for the Loom

Simple item-tag objective. Establishes that ordinary village work exists.

### Keep the Road Clear

Simple zombie/entity objective. Establishes that the board can track a carried hunt contract.

### Outfitter's Order

Simple wool/leather/coal objective. Story presentation introduces cold preparation without requiring any custom temperature-event objective.

### A Marker in the Snow

Only appears if a valid nearby low-tier northern watch/survey structure has been resolved and prepared with the unique proof item.

This is the first strong demonstration of:

```text
board = reason to go
map/cartographer = where to go
proof item = evidence you got far enough into the place
```

The player should not be required to complete all four. They are opportunities, not a forced tutorial checklist.

---

# First useful rewards

The first village should mostly pay in things that make the next few hours nicer rather than powerful gear.

Safe/near-safe examples:

- emeralds
- food
- arrows / torches
- a saddle from the authored stable-service request
- regional survival starter once exact item ID/delivery is validated
- a modest curated iron weapon only from a special/reputation posting, not random common jobs

Do not hand out:

- diamonds
- enchanted golden apples
- large iron jackpots
- T3 signature weapons
- Legendary/Masterful quality equipment
- major native dungeon loot

---

# Cartographer role in the first village

The cartographer should not merely duplicate the board.

Initial useful map pool should eventually include:

- one or more nearby Hearthlands structures that are legal in the current regional cell
- perhaps the nearest next settlement
- occasional outward teaser once the player has interacted with the village enough

A bounty can say, in story terms, that a local cartographer knows the place. The bounty itself does not need magic coordinates.

If no sensible nearby target exists, the cartographer simply does not offer that map.

---

# First recovery-contract target selection

The target-selection layer should prefer:

- current macro-region or compatible transition/common structure
- Hearthlands-legal radial tier
- reasonably near the issuing village
- not directly beside the village
- preferably unexplored
- not already overused by another active authored contract
- terrain/worldgen instance that actually exists

The exact distance window should be tuned in play, but the first adventure should feel like a local trip rather than another 3,000-block migration.

---

# UI behavior

This is the ideal place to test the Story Board UI.

The board header can read, for example:

```text
LOCAL NOTICES
Hearthlands · Frostmarch
```

The selected posting shows:

- notice class
- title
- issuer
- one short paragraph of context
- exact Bountiful objective/progress
- exact reward
- timer if timers remain enabled

The current neutral Local Notices already have story-card metadata, including cosmetic regional wording variants. They should be the first in-game UI smoke test because the objective logic underneath is already deliberately boring and safe.

---

# Acceptance criteria

The first-village vertical slice is ready only when all of these are true:

- the board knows its region/tier from the server-side CozyCrazyZones classification
- Shared Core vs regional work is mixed according to influence band
- no wrong-region posting appears
- exact-item, item-tag, and entity objectives cash in correctly
- story text never disagrees with the real objective/reward
- a carried bounty retains enough context to remember why it was accepted
- one structure recovery contract works end-to-end using a unique proof item
- the structure/map target actually exists and is sensible from the village
- the contract can be returned under the chosen issuing-board policy
- no special reward loses required item data/NBT

This slice should be proven before scaling authored structure contracts across all sixteen cardinal/tier cells.
