# Story Board Runtime Plan

Goal: make Bountiful feel authored without replacing its reliable quest mechanics.

## Lowest-complexity implementation

The story layer should be a small client-facing companion, not a second quest engine.

### Source of truth

Bountiful remains responsible for:

- objective progress
- completion
- item consumption
- entity-kill counting
- reward payout
- timers
- physical bounty items
- board inventory

The story addon is responsible only for:

- title
- issuer
- short narrative body
- notice-class label
- regional/tier decoration
- clearer presentation of Bountiful's real objective and reward

If the story addon ever disagrees with Bountiful, **Bountiful wins mechanically**.

## Stable story key

Each Bountiful `BountyDataEntry` already carries its pool-entry `id` into the generated bounty.

Use the **primary objective entry id** as the story lookup key.

For broad repeatable work:

```text
objective id -> title / issuer / body
reward remains whatever safe reward the decree generator paired to it
```

Example:

```text
ccc:west_h1_woodmans_order
    -> The Woodman's Order
    -> Lumberer
    -> "The sawyard is behind..."
```

For signature quests, narrow authored issuance guarantees the intended objective/reward pair, so the same id can safely identify the whole posting.

Do not infer story from item names or translated tooltip text.

## Board screen

Preferred implementation once a client addon scaffold exists:

1. Compile against the installed Bountiful 6.0.4 API/classes as an optional-but-required pack dependency.
2. Add a **minimal client injection/render hook** to the Bountiful `BoardScreen` rather than replacing the screen handler/container.
3. Read the currently selected Bounty stack from the existing board inventory.
4. Decode `BountyData` exactly as Bountiful already does.
5. Read the primary objective entry id.
6. Look up story metadata from our own client resource JSON.
7. Render a parchment/detail card in the free/details area.
8. Render Bountiful's true objective summaries and true reward summaries underneath the prose.
9. If metadata is missing or malformed, render stock Bountiful behavior with no gameplay impact.

### Why not replace Bountiful's screen handler?

Replacing the menu/container would create unnecessary sync and compatibility risk. The current Bountiful board already synchronizes its inventory and selected stack. We only need to render more information.

### Why not add a custom network packet for every story card?

The card text is static content keyed by the already-synchronized objective id. Shipping it as client resources is sufficient. No per-bounty story packet is needed.

## Carried bounty tooltip

Use a normal Forge client tooltip event for `bountiful:bounty`:

1. Decode the bounty's `BountyData`.
2. Determine story id from its primary objective.
3. Prepend/append:
   - authored title
   - issuer
   - one short sentence if space permits
   - region/tier classification if stored on the bounty later
4. Preserve Bountiful's normal Required/Rewards lines.

This avoids changing item identity, stack NBT semantics, or redemption.

## Region decoration

CozyCrazyZones already syncs client region state. This can decorate the open board with a small label such as:

```text
HARVESTLANDS NOTICE BOARD
Hearthlands
```

This is cosmetic only.

For **issuance**, always classify the board by its server-side block position through CozyCrazyZones. Never use client state to decide what quests are legal.

If exact board-cell display ever matters at a warped boundary, add one tiny board-open sync later. It is not needed for the first UI pass.

## Visual hierarchy

Board screen:

```text
+--------------------------------------------------------------+
| HARVESTLANDS NOTICE BOARD                         Hearthlands |
+-------------------------+------------------------------------+
| RECOVERY CONTRACT       | [wax seal] RECOVERY CONTRACT       |
| The Empty Stalls        |                                    |
| Stablemaster            | THE EMPTY STALLS                   |
|                         | Posted by the Stablemaster         |
| FARM NOTICE             |                                    |
| A Pest Worth Keeping    | The old roadside stable went      |
| Village Farmer          | silent three nights ago...         |
|                         |                                    |
| WATCH NOTICE            | OBJECTIVE                          |
| Chime at Dusk           | Stablemaster's Seal          0 / 1 |
| Innkeeper               |                                    |
|                         | PAYMENT                            |
| ...                     | Saddle x1 · Emeralds x3-5          |
|                         |                                    |
+-------------------------+------------------------------------+
| Board trust: Familiar                    Expires in 2d 11h    |
+--------------------------------------------------------------+
```

The exact mechanical line should always be more legible than the flavor prose.

## Regional presentation

Use restrained accents from `data/ui/region_profiles.json`:

- Frostmarch: cool blue-grey stamp
- Greenveil: moss/leaf stamp
- Sunscar: ochre road/sun stamp
- Harvestlands: russet harvest stamp
- Shared Core: neutral village seal

Do **not** make four completely different GUI systems. It should visibly be the same network of village notice boards across one world.

## Tier presentation

Tier changes vocabulary more than layout:

- Hearthlands: `Local Notice`, `Watch Notice`, `Recovery Contract`
- Frontier: `Frontier Contract`, `Survey`, `Recovery`
- Wildlands: `Expedition`, `Great Hunt`
- Dread Reaches: `Deep Country`, `Major Contract`

Avoid exposing dev language like `T1`, `T2`, `T3`, `T4` in normal player-facing UI.

## Story-length limit

Normal posting body:

- ideal: 1-2 sentences
- maximum: 3 short sentences
- no dialogue trees
- no fake NPC conversation
- no lore dump

The board is a notice board. Journals, maps, found books, and advancements can carry longer information.

## Failure-safe behavior

The UI addon must fail soft.

- missing story id -> stock Bountiful row/tooltip
- malformed story resource -> log warning, skip custom card
- missing region sync -> neutral board header
- unknown objective type -> show Bountiful's own summary
- missing optional icon -> text still renders

A presentation bug must never stop accepting/completing/redeeming a bounty.

## Not in first implementation

Do not add yet:

- dialogue system
- NPC quest markers
- custom photo verification
- live weather-event objectives
- escort AI
- bespoke per-quest event listeners
- animated quest cutscenes
- custom quest navigation arrows

Maps/cartographers remain the navigation layer.
