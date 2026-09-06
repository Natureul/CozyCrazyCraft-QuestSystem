# Village Trust + Board Cadence

This document fixes the player-facing cadence of CozyCrazyCraft village work before the full story-board UI exists.

## The short version

A village board should usually show **5–7 notices**, with **6** as the visual target.

The board changes **slowly**: one small refresh opportunity about every **5 real minutes**. It should feel like people post and remove notices over time, not like a slot machine rerolling in front of the player.

Once a player takes a bounty, **the accepted paper does not expire**.

Helping the same settlement builds **Village Trust**. Trust is local to that board/settlement and gradually unlocks better categories of work and utility rewards.

---

## Why accepted bounties should not have timers

Bountiful 6.0.4 stores the start time when the bounty is **generated on the board**. The physical bounty copied into the player's inventory keeps that same timing metadata.

That means a nominal 30-minute bounty which has already sat on a board for 24 minutes may give the player only six minutes after accepting it.

That is tolerable for a lightweight bounty mod. It is a bad fit for CozyCrazyCraft once a notice can mean:

- travel to a mapped structure,
- prepare for Cold Sweat conditions,
- bring a horse/cart,
- penetrate a dungeon,
- recover a proof item,
- return to the issuing settlement.

So the V1 policy is simple:

> **Listings rotate; accepted jobs do not expire.**

A player can discard/compost a bounty they no longer want. A later story UI may add a cleaner `Abandon` affordance.

---

## How many notices?

Stock Bountiful physically supports 21 bounty slots. That is much more than we want to present as the normal village experience.

CozyCrazyCraft target:

| State | Notices |
| --- | ---: |
| Minimum healthy board | 5 |
| Preferred | 6 |
| Maximum | 7 |

Why 5–7:

- enough choice that one bad objective does not make the board useless;
- enough room for mundane work + ecology + one unusual lead;
- small enough that each posting remains legible and worth hovering;
- avoids turning every settlement into a quest-book page.

The runtime V1 maintains this range after Bountiful refreshes the board.

---

# Village Trust

Bountiful already stores completion history on each board and converts total completions into its board reputation/level. That is almost exactly the semantic we want.

We reinterpret the existing idea as **Village Trust**.

This is deliberately **settlement trust, not global player renown**.

If you help one little Harvestwood village for an afternoon, *those people* know you. A Frostmarch village 4,000 blocks away does not magically know that you delivered carrots somewhere else.

In multiplayer, Bountiful's board level is based on summed completion history at the board. That naturally makes trust a community/settlement relationship: multiple players can improve the same village's confidence in outsiders.

## Trust stages

### Newcomer — Rep 0
Immediately available.

Typical work:
- common supplies;
- food/fuel;
- ordinary local hostile mobs;
- basic regional flavor.

Typical rewards:
- emeralds;
- food;
- arrows/torches;
- very small utility.

### Familiar Face — Rep 1 (~2 completed bounties)
The settlement recognizes that you actually come back.

Adds:
- slightly more personal errands;
- animal/farm assistance;
- modest utility rewards.

### Trusted Hand — Rep 2 (~4 completed bounties)
This is the first important threshold.

Adds:
- meaningful local jobs;
- small local-adventure leads when a valid destination exists;
- civilization utility such as a saddle;
- more valuable specialist supplies.

This threshold is intentionally quick. The player should not need twenty wheat contracts before villagers admit an old stable nearby is full of undead.

### Respected — Rep 3 (~6 completed bounties)
Adds:
- unusual ecology work;
- rarer local knowledge;
- better utility rewards;
- more dangerous but still tier-appropriate notices.

### Village Ally — Rep 5 (~10 completed bounties)
This is the practical ceiling for **story-relevant trust gating**.

Adds:
- signature local contracts;
- important regional leads;
- unusual rewards that the settlement would not give a stranger.

CozyCrazyCraft should **not** require rep 15/25/30 to see the area's meaningful content. Bountiful's native rarity progression may continue past this point, but main exploration should not become board grinding.

---

# Hard trust gating

Bountiful 6.0.4 natively checks `repRequired` while selecting **rewards**, but its objective-selection path does not check the same field.

The CozyCrazyQuests runtime V1 adds that missing filter for our custom content:

```text
objective.repRequired <= board reputation
```

This lets the data layer safely say:

```json
{
  "name": "Trusted local job",
  "repRequired": 2
}
```

without writing a unique event handler for the objective.

This is intentionally a narrow behavior change. The deployment excludes Bountiful's stock/compat pools, so the filter applies to the CozyCrazyCraft-owned bounty ecosystem rather than unexpectedly gating third-party content.

---

# What Trust should unlock

Trust should mostly change **what kind of relationship the board represents**, not simply multiply payouts.

Good unlocks:

- a stable owner trusts you with an abandoned-stable problem;
- a hunter tells you where a dangerous animal was last seen;
- a cartographer will sell/show a more useful local map;
- an animal keeper gives you a saddle, collar tool, or taming opportunity;
- an outfitter offers useful Cold Sweat equipment;
- a farmer gives a strange regional agricultural item;
- a village shares a recovered journal instead of selling it as generic loot.

Bad unlocks:

- `Trust 5 = emerald rewards +30%` as the whole system;
- forcing 30+ chores before regional story begins;
- locking accidental world discovery behind trust;
- making every village repeat the same five-stage quest chain.

The world always remains discoverable without permission. Trust changes what **civilization tells and gives you**.

---

# Trust + cartographers

The board and cartographer should cooperate without becoming one UI.

Example:

```text
Newcomer
  board: local supplies / simple hunt
  cartographer: ordinary nearby known places

Trusted Hand
  board: "The old stable has gone quiet."
  cartographer: can provide the actual local Stable map if a sane target exists

Village Ally
  board: serious regional lead
  cartographer: deeper/outward knowledge appropriate to profession level and geography
```

Cartographer profession level and Village Trust are different dimensions:

- **profession level** = how capable/experienced this cartographer is;
- **Village Trust** = how willing the settlement is to share valuable/problem-sensitive knowledge with you.

Do not make both gates mandatory for every map. Ordinary explorer maps remain available normally.

---

# Trust + authored quests

Repeatable Bountiful notices can use `repRequired` directly.

Major one-off authored postings need the companion runtime to track whether the settlement has already offered/completed them. Those should be inserted deliberately rather than dumped into a broad Bountiful pool where objectives and rewards can cross-pair randomly.

The intended board composition at mature implementation is roughly:

- 3–4 ordinary/repeatable local notices;
- 1 regional ecology/community notice;
- 0–1 trusted/special notice;
- 0–1 real destination lead when the target selector finds something sane.

No special target found = no fake special posting.

---

# Current V1 implementation target

The first runnable companion runtime is intentionally modest:

1. classify pristine boards through CozyCrazyZones;
2. stamp the correct Hearthlands regional decree instead of random geography;
3. maintain roughly 5–7 active board notices;
4. relabel Bountiful's reputation concept as **Village Trust** in English UI resources;
5. honor `repRequired` for objective entries as well as rewards;
6. detect an inhabited village near a player and repair a boardless village with one civic board near its meeting point;
7. keep all expensive work event/proximity-driven rather than performing world-wide scans.

Signature proof-item quests, exact useful-structure targeting, and richer story cards remain later runtime layers, but this V1 is enough to test whether the village loop itself feels right.
