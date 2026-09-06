# Quest Targeting & Distance Policy

This document answers one deceptively important question:

> **How do we stop a village from asking the player to do a supposedly local job 7,000 blocks away?**

The answer is not a single maximum-distance number. The target selector should understand **what kind of trip the posting is supposed to be**, where the issuing settlement is, and what CozyCrazyZones says is actually legal at each candidate position.

The machine-readable policy lives in [`data/targeting_policy.json`](../data/targeting_policy.json).

---

## Rule zero: a board is allowed to have nothing suitable to say

The system must never manufacture a bad quest just to fill a slot.

If a board cannot find a real target that is:

- in the correct dimension,
- legal according to `CozyZonesApi.structureAllowed(...)`,
- in the correct radial tier,
- in the correct macro-region when the quest is regional,
- within the quest's travel-distance envelope,
- and not an obviously bad duplicate,

then the structure/recovery posting simply **does not appear**.

The board can fill the space with ordinary gathering, ecology, or community work instead.

This is preferable to every bad fallback:

- no 6,000-block Hearthlands stable errand,
- no Frostmarch village pointing to a jungle temple because it happened to be nearest,
- no Wildlands story lead that points inward toward spawn,
- no duplicate map to the exact same ruin the player was just assigned.

---

## Four travel scopes

### 1. Local Site

This is ordinary settlement business: a missing courier, nearby stable, watchtower, small ruin, graveyard, local infestation, or recovery contract.

It should feel like **leaving town for an adventure and coming back**, not starting an expedition.

| Issuing tier | Minimum | Ideal | Soft max | Hard max |
|---|---:|---:|---:|---:|
| Hearthlands | 250 | 750 | 1,200 | 1,800 |
| Frontier | 350 | 950 | 1,500 | 2,200 |
| Wildlands | 450 | 1,200 | 1,900 | 2,800 |
| Dread Reaches | 550 | 1,500 | 2,400 | 3,400 |

A structure closer than the minimum is often too trivial or may be practically inside town. A structure beyond the hard maximum is not a local job anymore.

### 2. Regional Expedition

This is a named dungeon, major recovery, Great Hunt location, or substantial regional outing in the **same radial tier** as the issuing settlement.

| Issuing tier | Minimum | Ideal | Soft max | Hard max |
|---|---:|---:|---:|---:|
| Hearthlands | 600 | 1,200 | 1,750 | 2,300 |
| Frontier | 700 | 1,500 | 2,200 | 3,000 |
| Wildlands | 850 | 1,900 | 2,800 | 3,800 |
| Dread Reaches | 1,000 | 2,300 | 3,400 | 4,600 |

These can be memorable journeys, but the target still belongs to the settlement's current part of the world.

### 3. Outward Lead

A deliberately rare clue into the **next radial tier**. This is not phrased as "the farmer down the road needs help." It is information: an expedition journal, a rumor, a cartographer's lead, an old road, a trade route, or a known distant landmark.

| Issuing tier | Minimum | Ideal | Soft max | Hard max |
|---|---:|---:|---:|---:|
| Hearthlands | 900 | 1,700 | 2,300 | 3,000 |
| Frontier | 1,100 | 2,100 | 2,900 | 3,700 |
| Wildlands | 1,300 | 2,600 | 3,600 | 4,600 |

An outward lead may jump **one tier only** and must move the target at least ~350 blocks farther from the canonical world anchor than the issuing board.

That prevents a Frontier settlement from giving a supposedly outward expedition that happens to curl back toward the Hearthlands.

### 4. Legendary Destination

This is reserved for final-region knowledge and equivalent major story destinations. It is never drawn as ordinary repeatable board work.

For Dread Reaches, the starting envelope is roughly 1,200 minimum / 2,800 ideal / 4,200 soft max / 5,600 hard max.

The larger range is intentional: a legendary place can be a real journey. The important distinction is that **the game tells the player it is a serious journey** rather than pretending it is a local errand.

---

## Do not blindly choose the nearest structure

"Nearest legal structure" is better than random, but it still creates bad behavior.

Imagine three valid stables at 280, 760, and 1,450 blocks from town. For a Hearthlands Local Site, 760 is probably the best adventure. The 280-block stable may be visible from the village; the 1,450-block stable is unnecessarily long.

So the selector scores candidates around an **ideal distance**.

A simple first scoring model can be:

```text
score = distance_fit
      + region_fit
      + influence_fit
      + unexplored_bonus
      + outward_fit
      - duplicate_penalty
      - boundary_penalty
```

`distance_fit` should peak near the policy's ideal distance and fall toward both the minimum and hard maximum.

No sophisticated machine learning or pathfinding is required.

---

## Candidate selection pipeline

For a target family such as `dungeons_enhanced:stables`:

1. Search for a small number of plausible structure instances within the scope hard maximum.
2. Ask CozyCrazyZones whether the structure is legal at each actual candidate position.
3. Classify each candidate's `RegionalCell`.
4. Reject wrong-tier candidates for a local/same-tier quest.
5. Reject wrong macro-region candidates for a regional quest.
6. Reject anything below the scope minimum or above its hard maximum.
7. Reject an instance already assigned by another active quest when possible.
8. For outward leads, require one-tier outward movement and meaningful radial gain.
9. Score the survivors around the preferred distance.
10. Prefer established-region positions over awkward macro-border fringe when otherwise similar.
11. Prefer unexplored/unassigned instances when we can determine that cheaply.
12. Pick the highest-scoring candidate.
13. If the candidate list is empty, **do not issue that quest**.

This exact ordering is intentionally conservative. A wrong-region target cannot win because it has a pretty distance score; it is filtered out before scoring.

---

## Performance: never locate the world every board refresh

Structure lookup can be expensive, especially in a large modpack.

The quest layer should cache results by approximately:

```text
board/settlement identity
+ target structure family
+ target policy scope
+ radial tier
+ macro-region
```

Initial policy:

- consider at most ~12 candidate instances per target search,
- positive result cache: about one Minecraft day (24,000 ticks),
- negative result cache: about 5 minutes (6,000 ticks),
- remember roughly the last 8 assigned targets per player for duplicate avoidance.

A board should therefore spend most updates reading cached target availability, not forcing fresh worldgen lookups.

---

## What Supplementaries already gives us

The current low-code map prototype is safer than it first looked.

Supplementaries' `/supplementaries structure_map ...` implementation searches with a radius of **150 chunks**: nominally about 2,400 blocks from the player. Its data-driven cartographer structure-map trade uses the configured adventurer-map search radius; upstream default is **100 chunks**, about 1,600 blocks.

Those are *search radii*, not promises that every map target is exactly that many blocks away, but they give the prototype a natural locality ceiling.

This makes Supplementaries especially attractive for early local maps:

- if there is no nearby matching structure, the map simply cannot be created;
- cartographer map listings return no offer if the lookup returns empty;
- maps target real Minecraft structure instances rather than arbitrary coordinates.

The custom locator becomes necessary when we need stronger semantics than "nearby matching structure": exact radial tier, strict cardinal region, outward direction, duplicate memory, and proof-item assignment.

---

## Example: The Empty Stalls

Issuing board:

```text
Harvestwood Hearthlands village
~1,150 blocks from spawn
```

Quest:

```text
The Empty Stalls
scope = LOCAL_SITE
target family = dungeons_enhanced:stables
required region = WEST / Harvestwood
required tier = HEARTHLANDS
```

Suppose candidate stables are:

```text
A: 410 blocks away, Harvestwood Hearthlands
B: 820 blocks away, Harvestwood Hearthlands
C: 1,300 blocks away, Frostmarch Hearthlands
D: 2,600 blocks away, Harvestwood Frontier
```

C is rejected for region. D is rejected for tier and hard distance. A and B survive. B is closer to the 750-block ideal, so B wins unless duplicate/exploration state gives A a compelling reason.

That is the behavior we want players to feel without ever seeing the scoring math.

---

## Example: a Frontier cartographer teaser

A Greenveil Frontier settlement can occasionally know about something in Greenveil Wildlands.

That is an `OUTWARD_LEAD`, not a local bounty.

The selector therefore requires:

- East/Greenveil,
- target in Wildlands,
- no jump straight to Dread Reaches,
- target farther outward than the village,
- meaningful radial gain,
- distance within the outward-lead envelope.

The result may be sold as a map, found in an old expedition journal, or mentioned by a board posting. The target does not need to be a quest at all.

---

## Player-facing rule

Players should not think about any of these numbers.

They should simply learn the world's language:

- **notice / local contract** = nearby enough to come home afterward,
- **expedition** = pack for a real trip,
- **rumor / old map / outward lead** = this may take you into the next belt,
- **legendary map** = this is a destination, not an errand.

The backend exists to make those words trustworthy.
