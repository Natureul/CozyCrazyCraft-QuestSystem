# CozyCrazyCraft Quest / Information Framework

## Core rule

The world should not behave like a giant quest-book checklist.

Different systems answer different questions:

- **Starter House** — first piece of knowledge; almost no free equipment.
- **Atlas** — what the player knows.
- **Bounty Board** — what local people want done.
- **Cartographer** — places local people know about.
- **Found maps / journals / letters** — what previous explorers knew.
- **Structures** — where events, danger, evidence, and native loot physically exist.
- **Advancements** — significant accomplishments, not errands.

Not every structure should have a bounty. Not every bounty should have a map. Accidental discovery remains important.

---

# Geography

The quest layer will eventually consume two world classifications from the zoning project.

## Radial tier

- Hearthlands: 0–2500
- Frontier: 2500–5500
- Wildlands: 5500–9000
- Dread Reaches: 9000+

## Cardinal macro-region

- North — Frozen / Alpine
- East — Jungle / Lush / Overgrown
- South — Desert / Savanna / Hot
- West — Autumn / Redwood / Temperate Old Forest

Thus a village/target can be classified as:

- Northern Hearthlands
- Southern Frontier
- Western Wildlands
- Eastern Dread Reaches

Radial tier controls how deep/dangerous the content is. Cardinal region controls ecological and cultural identity.

The quest system must **query** this geography later rather than reimplementing it.

---

# Opening vertical slice

The intended first playable information chain is:

```text
Starter House
  ↓
map to nearest useful village (~900–1200 blocks)
  ↓
first village
  ├─ bounty board = local needs
  ├─ cartographer = known places
  └─ rumors / local information
  ↓
first mapped or locally-known Hearthlands adventure
  ↓
reward / proof / new information
  ↓
first outward lead toward Frontier
```

The first ~0–700 blocks are shared/ordinary Inner Hearthlands. Regional identity grows through ~700–1200 and becomes clear beyond the first-village band.

---

# Bounty roles

## 1. Supply / gathering

Reliable Bountiful mechanics:

- exact item objectives
- item-tag objectives

Examples:

- food for hunters
- wool/leather for a northern outfitter
- acacia materials for a southern naturalist
- logs for a western settlement
- regional plants for an eastern herbalist

Use small quantities. The goal is to create a reason to interact with the local ecology, not turn Minecraft into warehouse labor.

## 2. Extermination

Reliable native entity-kill objective.

Examples:

- ordinary local zombies
- region-specific hostile mobs once IDs are audited
- dangerous wildlife when appropriate

Do not pretend an entity-type objective proves the mob was killed at one exact mapped site.

## 3. Great Hunt

A serious named entity-kill contract.

Examples:

- Frostmaw
- Umvuthi
- Sir Pumpkinhead
- Wroughtnaut when appropriate

The bounty gives the **reason** to hunt. The map/cartographer/rumor system gives the **place**.

## 4. Recovery Contract

Preferred robust structure-adventure pattern.

A meaningful target structure contains a unique CozyCrazyCraft item.

Example:

```text
Restless Stables
Objective: recover cozycrazycraft:stablemasters_seal
```

This proves the player physically reached a meaningful interior point without fragile "clear every original mob" logic.

## 5. Investigate / survey

Potentially useful later through criteria, but every exact trigger must first pass the reliability test plan.

Do not ship unsupported assumptions.

## 6. Companion / rescue

Rare authored content, not routine random-board filler.

Preferred forms:

- reward the **means to tame** a regional animal, then let the player choose/find one
- occasionally rescue a specific named animal and transfer ownership through future tested glue
- reward pet-specialization enchantments/items

## 7. Civilian / profession work

Important for making villages feel inhabited.

Examples:

- outfitter supplies
- farmer needs
- cooking/provisions
- archaeology support
- animal keeping
- caravan preparation

These should coexist with adventure contracts so every board is not "EPIC DUNGEON QUEST #6."

---

# Maps and cartographers

## A bounty does not automatically equal a waypoint

Three common cases:

### Known local problem

The settlement knows exactly where the target is.

A map may come directly with the job or be cheaply available.

### Known type, unknown exact location

Example:

> A Wroughtnaut is said to guard an old chamber. A cartographer may know of one.

The board provides motivation. The cartographer provides geographical intelligence.

### Unknown / lost knowledge

Deep structures are learned through:

- expedition journals
- found maps
- letters
- recovered records

Civilization should not magically know every legendary destination.

---

# Atlas

The Atlas is the player's accumulated geographical knowledge, not a radar.

Desired long-term behavior:

- starts early
- effectively expandable without a tedious empty-map tax
- unexplored terrain stays hidden
- no mob radar
- likely coordinates disabled
- starter/home marker retained
- structure maps integrate into the player's knowledge network

Implementation remains deferred until the map/navigation layer is ready.

---

# Useful target selection

When the runtime eventually issues a destination-specific quest/map, it should not blindly call "nearest structure".

Future selector requirements:

- correct macro-region
- correct radial tier / minimum radial tier
- sensible distance from issuing settlement
- outward progression when the job is meant to lead outward
- preferably unexplored
- avoid repeatedly targeting the same location
- if no sensible target exists, do not issue that destination-specific quest

Conceptual API:

```text
findNearestUsefulStructure(
    structureFamily,
    source,
    allowedRegion,
    allowedRadialTier,
    minDistance,
    preferredDistance,
    maxDistance,
    unexploredPreference,
    repeatGuard
)
```

This is deliberately deferred until the zoning project's structure/geography APIs are known.

---

# Same-board redemption

Desired CozyCrazyCraft behavior:

> A bounty taken from Village A is returned to Village A.

Why:

- board reputation becomes local relationship rather than global XP
- villages become distinct places
- returning home after an expedition matters
- prevents every board from becoming an interchangeable kiosk

Future bounty source stamp:

- dimension
- board position
- stable board identity / UUID if practical

Implementation deferred.

---

# Rewards

## Ordinary repeatable contracts

Usually:

- modest emeralds
- food
- torches
- arrows
- ordinary useful materials

## Authored regional contracts

Prefer a memorable deterministic reward:

- Acacia Blossoms that enable elephant taming
- Cold Sweat survival equipment
- Horse Frost Walker
- Ancient Scarab
- Nature Rune
- backpack Unlock Tokens
- saddle / cart progression
- pet training item/enchantment
- curated regional Spartan weapon

## Major native dungeons

Do not steal their native reward ecology.

Alex's Caves, Aquamirae, major boss dungeons, etc. should still reward the player primarily through the content inside them.

Our layer gives:

- reason
- preparation
- map/knowledge
- occasional proof/reputation
- regional/main-story recognition

---

# Regional signature weapon guideline

Curated quest weapon formula:

```text
appropriate Spartan Weaponry base
+ one meaningful Quality Equipment property
+ one or two restrained compatible enchantments
+ authored name
```

Avoid dumping endgame enchantment stacks onto midgame weapons.

Current T3 concepts:

- North: **White Reach** — diamond Pike, Sweeping quality
- East: **Greenwake** — diamond Glaive, Sweeping quality
- South: **Sunspike** — diamond Lance, Keen quality
- West: **Harvest Moon** — diamond Scythe, Sweeping quality

Exact registry IDs, Quality Equipment NBT, and enchant compatibility must be captured/tested before implementation.

---

# Main-story relationship

The regional main quest should use the same information ecosystem as ordinary play.

Do not create a separate giant quest-book UI that bypasses boards/maps/cartographers/journals.

Current major arcs:

- North: Frostmaw → Aquamirae/Ice Maze → Captain Cornelia
- East: jungle/temples/overgrowth → Jungle Abomination
- South: Umvuthi → archaeology/deep desert → Cursed Pyramid → Ancient Remnant
- West: old forest/pumpkin escalation → Sir Pumpkinhead → Lord Pumpkinhead

Regional final destinations belong in their respective Dread Reaches.

The End remains the ultimate Minecraft endgame later.
