# Village Bounty Board Runtime

The current playtest exposed an important gap: **stock Bountiful does not guarantee a bounty board in every village.** For CozyCrazyCraft, that is not reliable enough.

## What Bountiful 1.20.1 actually does

On Forge 1.20.1, Bountiful registers its `bountiful:village/common/bounty_gazebo` as another entry in the vanilla village **house jigsaw pools** for:

- plains
- savanna
- snowy
- taiga
- desert

The configured `boardGenFrequency` is the weight used for that pool insertion. It is not a count and it is not a guarantee.

The current CozyCrazyCraft playtest config leaves `boardGenFrequency` at `2`. Therefore a perfectly valid generated village can contain zero Bountiful gazebo pieces, and another village can contain one or more.

Two additional edge cases matter:

1. A village whose chunks were generated before the relevant Bountiful worldgen registration will not retroactively gain a gazebo.
2. Village-overhaul mods may use or replace jigsaw pools in ways that make stock Bountiful's five vanilla-pool injections less dependable.

The correct CozyCrazyCraft solution is therefore **not** to raise the jigsaw weight until boards become common. A high weight can create duplicate gazebos and still cannot express our desired settlement semantics.

---

# Player-facing requirement

For an ordinary inhabited village that participates in the quest system:

> **There should be exactly one obvious civic bounty board, easy to find from the village center.**

The player should not have to search every house to discover whether the settlement happened to roll a board.

A Bountiful-generated gazebo already present in the village satisfies the requirement. The companion runtime only repairs villages that have no usable board.

---

# Detection model

The runtime should work from village semantics rather than from a hardcoded list of structure templates.

Preferred village-center evidence:

1. bell POI / bell block near an active village,
2. village POI/activity center,
3. known village structure center as a fallback when an integration exposes it.

A center candidate must have convincing village evidence; a random bell at a player's base must not cause a bounty kiosk to appear.

Once a village center is recognized, search a configurable radius (initial design: 64 blocks) for `bountiful:bountyboard`.

- If one exists, record the village as satisfied and do nothing.
- If none exists, attempt a small safe placement search around the civic center.

---

# Repair placement

The repair should be deliberately boring and robust rather than build an entire second village structure system.

Preferred V1 placement is a **single board block on a tiny civic notice-post arrangement** adjacent to a path/bell, only where:

- the board position and interaction side are replaceable/air,
- there is stable non-fluid support,
- placement does not overwrite a workstation, container, bed, bell, door, farm block, or other meaningful village block,
- it is not inside a building wall,
- it is reasonably visible from the center.

If no safe position exists, do nothing and retry only at a long cooldown or after the village is encountered again. Never bulldoze terrain to satisfy the guarantee.

This repair mechanism also gives us a useful path for **existing villages**: loading/visiting an old boardless village can make it eligible for a one-time repair.

---

# Duplicate prevention

The runtime needs settlement-level state, not repeated local scans forever.

Suggested saved record:

```text
VillageBoardRecord
  dimension
  center x/y/z
  board x/y/z
  status: FOUND_EXISTING | PLACED_REPAIR | DEFERRED_NO_SAFE_SITE
  firstSeenGameTime
  lastVerifiedGameTime
  regionalCellSnapshot
```

Nearby center detections should coalesce into the same settlement record. A pre-existing Bountiful gazebo and a repair board must never result in two companion-managed boards.

If the recorded board is later broken by the player, **do not immediately respawn it behind their back**. Mark the settlement as player-modified and wait for an explicit design decision on replacement behavior.

---

# Regional initialization

Presence and content are separate concerns.

Once the physical board exists, its initial CozyCrazyCraft identity should come from:

```java
CozyZonesApi.regionalCellAt(level, boardX, boardZ)
```

That gives the macro-region, radial tier, and influence band. The quest layer then chooses the appropriate allowed board profile/decree.

Important: current checked-in `board_decree_assignment.json` is a **design contract**, not evidence that the running playtest is already assigning decrees geographically. A manually placed board can still receive a random available Bountiful decree until this runtime hook exists.

For profiles that are not live yet, fail conservatively to a generic local-notices profile rather than pretending a future Frontier/Wildlands/Dread catalog is implemented.

---

# Stock jigsaw generation during rollout

Do **not** set Bountiful `boardGenFrequency` to zero until the companion guarantee is proven in-game.

Rollout order:

1. Keep stock Bountiful gazebo generation enabled.
2. Add the companion detector/repairer; existing gazebos count as valid boards.
3. Test vanilla plains/savanna/taiga/snowy/desert villages plus modded village variants used by the pack.
4. Verify one-board behavior and safe placement.
5. Only then consider setting `boardGenFrequency = 0` and making the companion the sole placement authority.

There is no benefit to removing the stock fallback before its replacement is reliable.

---

# Performance constraints

This must not become another tick-heavy world scanner.

- Never scan the whole world for villages.
- Trigger from chunk/village/player proximity events at coarse cadence.
- Cache processed settlement centers in saved data.
- Search only a compact radius around a detected civic center.
- Do not repeatedly query structure-location APIs every tick.
- Board content refresh remains Bountiful's job.

The intended runtime cost after a settlement has been processed is effectively zero.

---

# Definition of done

The feature is production-ready when all of these are true:

1. A newly generated supported village reliably ends up with one discoverable bounty board.
2. A stock Bountiful gazebo satisfies the guarantee without duplication.
3. A boardless already-generated village can be safely repaired when encountered.
4. A random bell outside a real village does not create a board.
5. The board's region/tier identity comes from CozyCrazyZones rather than random decree selection.
6. Breaking a board does not cause an immediate intrusive respawn loop.
7. The implementation does not perform broad recurring world scans.

This is a small piece of glue, but it matters enormously to the player experience: **the village quest system has to be physically findable before anything clever about quests or maps matters.**
