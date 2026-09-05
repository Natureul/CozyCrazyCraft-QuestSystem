# Bountiful 6.0.4 Authoring / UI Limits

This document records limits that materially affect CozyCrazyCraft quest design.

## Stock bounty title is not a quest-title field

In exact Bountiful 1.20.1, `BountyItem.getName()` produces a title based on rarity (and timer when enabled), e.g. conceptually:

```text
Common Bounty (time)
Rare Bounty (time)
```

There is no pool JSON field that makes the randomly generated physical bounty itself become:

```text
The Elephant Keeper
The Sleeping Mountain
The Headless Road
```

Those names can remain useful **design labels**, map/journal titles, advancement names, or future authored-bounty custom names, but stock random Bountiful generation should not be described as supporting bespoke quest titles.

## Pool-entry `name` is not universally displayed

`PoolEntry` has a `name` field, but the exact built-in type renderers do not all honor it.

- `criteria` uses `entry.name` as a literal display override.
- `item` summary uses the actual item's name.
- `entity` summary renders `Kill <entity name>`.

Therefore we should not assume that adding:

```json
"name": "Recover the Stablemaster's Seal"
```

to an ordinary item objective will cause that custom sentence to appear.

For proof-item quests, give the **proof item itself** a good display name in the CozyCrazyCraft companion mod. Then Bountiful's ordinary item objective naturally shows the meaningful item.

Example:

```text
Stablemaster's Seal 0/1
```

This is clean and reliable.

## Random Bountiful is combinatorial by design

`BountyCreator`:

1. chooses reward entries from all reward pools in the active decree(s)
2. chooses a random amount for those rewards
3. totals their worth
4. chooses one or two objectives whose worth can satisfy the reward value

It does **not** preserve authored objective↔reward narrative pairs.

### Good use

Broad local pools where many combinations make sense:

- bring wheat/logs/wool/string
- kill common local hostiles
- receive emeralds/food/arrows/torches

### Bad use

Put these into one broad pool:

- Acacia Blossoms
- Ancient Scarab
- White Reach
- saddle

and assume the elephant-related objective receives Acacia Blossoms.

It may not.

## Narrow decree can mathematically force a pair, but placement is still a problem

If a decree contains exactly:

- one objective entry
- one reward entry

then Bountiful's selection loops cannot select a second distinct entry, so the resulting bounty effectively has the intended one-to-one pair.

This is promising for authored content.

However, **loading many such special decrees globally is unsafe right now** because pristine 1.20.1 boards select a random loaded decree. Without regional/quest-state assignment, the wrong village could receive the wrong special decree.

Therefore special authored decrees are deferred until the quest runtime can deliberately place/issue them.

## Objective count cannot simply be globally set to one

Exact `BountyCreator` randomly chooses one or two objectives. There is no current Bountiful config field equivalent to `maxNumObjectives`.

Broad pools must therefore be designed so plausible two-objective combinations exist.

Example of acceptable local output:

```text
Bring wool
Kill zombies
```

Example of a pool design to avoid:

```text
Recover sacred relic A
Recover unrelated sacred relic B
```

Those should never share a broad random objective pool.

## Timers

The current CozyCrazyCraft Bountiful config retains the existing timer setting.

Long-form mapped expeditions and main-story steps should **not automatically be implemented as ordinary expiring random bounties**.

Before using stock Bountiful for long-distance adventure contracts, test the actual generated completion times at intended reward worths and decide whether:

- timers stay globally enabled for local jobs
- authored expeditions use a separate issuance path
- or the timer policy changes later

No decision is hard-coded in this repository yet.

## Narrative belongs in the wider information ecosystem

Because stock Bountiful's physical item is intentionally compact, CozyCrazyCraft narrative should be distributed across:

- board objective/reward
- named proof item
- map title
- cartographer information
- found journal/letter
- structure itself
- advancement when significant

This is not a workaround. It matches the project's intended design: no single UI knows everything.
