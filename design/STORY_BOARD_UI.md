# Story Board UI

## Goal

Make Bountiful feel like a local notice board in a lived-in world without replacing its objective/reward engine or inventing fragile quest events.

The rule is:

> **Bountiful owns truth and completion. CozyCrazyCraft owns presentation and context.**

A posting can sound authored and atmospheric while still resolving to one of the proven objective primitives: exact item, item tag, entity kill, or later a unique proof item.

## Why this is feasible

Bountiful 6.0.4 already stores each generated objective/reward as a `BountyDataEntry`, including its stable pool-entry `id`.

That means a presentation layer can key narrative text to the exact objective entry that actually generated without changing completion logic.

Example:

```text
Bountiful entry id: ccc_hearth_missing_wool
Mechanic: item_tag -> minecraft:wool
Story card:
  title: Winter Stores
  issuer: Village Weaver
  text: The last caravan never made it in. The loom-house is buying clean wool before the nights turn colder.
```

The objective remains ordinary Bountiful item-tag collection. The story layer merely knows what that objective means in-world.

## Recommended screen layout

Do **not** turn the board into a giant quest-menu mod.

Keep Bountiful's tactile board: several postings visible at once, click a posting to select/take it.

Add one readable detail panel for the selected posting.

### Board header

Small contextual header, e.g.:

```text
LOCAL NOTICES
Hearthlands • Frostmarch
```

or:

```text
FRONTIER CONTRACTS
Sunscar
```

Use the current CozyCrazyZones client region only for the display label. Actual board assignment is server-side from board position.

### Posting list

Keep Bountiful's existing compact icon rows.

Optionally add a very short title beside/over the selected row later, but do not require replacing the list widget for v1.

### Selected-posting detail panel

When a bounty is selected, show:

```text
[rarity / category kicker]

Winter Stores
Village Weaver

The last caravan never made it in. The loom-house
is buying clean wool before the nights turn colder.

OBJECTIVE
Bring 8 Wool

REWARD
4 Emeralds + 8 Bread

[time remaining, if timers stay enabled]
```

For a hunt:

```text
GREAT HUNT

Teeth in the Treeline
Village Watch

Something has been taking livestock beyond the western
fence. Tracks point toward the old woods.

OBJECTIVE
Kill 1 Dire Hound Leader

REWARD
...
```

For a structure-proof quest:

```text
RECOVERY CONTRACT

The Empty Stalls
Stablemaster

The old roadside stable went silent three nights ago.
Bring back the Stablemaster's Seal if you find it.

OBJECTIVE
Recover Stablemaster's Seal

REWARD
...
```

The prose implies a place or event; the actual objective remains the unique proof item.

## Carried bounty tooltip

The story should not disappear after the player takes the paper.

When hovering a `bountiful:bounty`, prepend a compact story header derived from its objective entry id:

```text
The Empty Stalls
Stablemaster • Hearthlands Contract

The old roadside stable went silent three nights ago.

Required:
  Stablemaster's Seal 0/1
Rewards:
  ...
```

This can be implemented as a client tooltip augmentation. It does not alter cash-in logic.

## Data-driven story cards

Narrative should live in our own resource JSON, not hard-coded Java conditionals.

Proposed resource:

```text
data/cozycrazyquests/story_cards/*.json
```

Example:

```json
{
  "entry": "ccc_hearth_missing_wool",
  "title": "Winter Stores",
  "issuer": "Village Weaver",
  "category": "LOCAL NOTICE",
  "body": [
    "The last caravan never made it in.",
    "The loom-house is buying clean wool before the nights turn colder."
  ]
}
```

The lookup key is the Bountiful `BountyDataEntry.id`, not entity NBT, coordinates, or inferred text.

Fallback behavior is mandatory:

- if no story card exists, render ordinary Bountiful data
- never hide an objective/reward because a story card failed
- never make completion depend on story metadata

## Multiple-objective bounties

Bountiful can generate more than one objective. Narrative must not lie about the actual paper.

For broad repeatable pools:

- use a generic category/issuer story if multiple objectives appear
- list all actual objectives underneath
- do not force a single dramatic title that describes only one of them

For signature authored postings:

- use a narrow decree/pool setup so only the intended objective/reward pair can generate
- then a specific story card is safe

## Regional flavor without mechanical risk

Regional wording can vary by the CozyCrazyZones client state while the objective stays identical.

Example for a generic livestock request:

```text
Frostmarch:
  "The cold has kept the flocks close to the village..."

Greenveil:
  "The wet season spoiled more stores than expected..."

Sunscar:
  "The caravan came in light after crossing the dry road..."

Harvestlands:
  "The autumn markets emptied the storehouse faster than expected..."
```

This variation is cosmetic. The bounty remains the same validated Bountiful objective.

## Implementation risk levels

### GREEN — do first

- Client-only selected-bounty story panel layered onto the existing board screen.
- Client tooltip augmentation for carried Bountiful bounties.
- Data-driven lookup by `BountyDataEntry.id`.
- Display current CozyCrazyZones radial/macro region as context.
- Fallback to stock Bountiful presentation on any lookup failure.

These do not alter objective tracking, generation, redemption, reputation, or rewards.

### YELLOW — after smoke test

- Replace Bountiful bounty item display names with story titles.
- Replace/reshape the existing board texture/layout substantially.
- Show exact issuing-board region on carried papers; this requires persisting source metadata rather than merely using the player's current region.

### DEFERRED

- UI buttons that cause custom quest-state transitions.
- Dialog trees.
- bespoke event objectives.
- exact structure-instance completion without proof-item/locator infrastructure.

## Visual direction

The desired feeling is **paperwork from a dangerous local world**, not a polished MMO quest log.

- parchment / pinned-note visual language
- quiet serif-like Minecraft-compatible title treatment if feasible; otherwise vanilla font with hierarchy
- small regional stamp or heading rather than giant colored panels
- objective/reward icons remain visible and useful
- short prose: generally 1–3 sentences
- no NPC portrait system required
- no giant quest markers
- no giant animated UI

The atmosphere should come from wording, hierarchy, regional context, maps, and physical proof items—not from complexity.

## First vertical slice

Implement the story layer against the current safe `Local Notices` smoke-test decree before regional authored quests.

This proves:

1. selected Bountiful bounty can be read safely on the client;
2. `BountyDataEntry.id` maps to our narrative data;
3. objective/reward rendering still matches Bountiful truth;
4. carried-bounty tooltip retains the story;
5. CozyCrazyZones region labels can decorate the UI without becoming quest logic.

Only after that works should we dress the real Hearthlands/Frontier/Wildlands/Dread content.
