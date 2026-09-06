# First Reward Delivery Smoke Test

This is the shortest useful in-game test for turning source-checked reward candidates into full-pack-verified quest rewards.

Do **not** test every planned reward at once. Prove the simple high-value cases first.

## Test A — Acacia Blossoms

Target:

```text
alexsmobs:acacia_blossom
```

Procedure:

1. Give/obtain the item normally and verify it renders/behaves correctly.
2. Make a temporary Bountiful reward entry for 4–6 blossoms.
3. Complete a trivial safe objective.
4. Cash in.
5. Confirm exact blossom count arrives with no odd metadata loss.
6. Find an elephant and verify the blossoms participate in the installed taming loop.

Pass means `Flowers of the Savanna` can move from source-checked to live-reward candidate.

## Test B — Backpacked Unlock Token

Target:

```text
backpacked:unlock_token
```

Procedure:

1. Confirm the current Backpacked config permits the desired token use.
2. Give one token and verify it can unlock the intended backpack component.
3. Deliver one through Bountiful.
4. Verify the delivered token behaves identically.

If the current config allows a token to unlock something more powerful than intended, tune the config before making tokens common quest rewards.

## Test C — Thermometer

Target:

```text
cold_sweat:thermometer
```

Procedure:

1. Confirm normal item/Curios behavior in the installed Cold Sweat 2.4.2 build.
2. Deliver one through Bountiful.
3. Verify it remains a normal functional Thermometer.
4. Decide whether first-village regional issuance should avoid duplicates once a player already owns one.

## Test D — Empty Waterskin

Target:

```text
cold_sweat:waterskin
```

Procedure:

1. Deliver an empty waterskin through Bountiful.
2. Verify normal fill/use behavior afterward.
3. Do **not** use a special filled/heated/cooled reward until real stack data is captured separately.

## Test E — Sewing Table

Target:

```text
cold_sweat:sewing_table
```

Procedure:

1. Deliver the block item through Bountiful.
2. Place it.
3. Confirm normal insulation workflow.
4. Balance decision: likely authored Frostmarch Frontier reward, not repeatable random payout.

## Test F — Saddle

Target:

```text
minecraft:saddle
```

Procedure:

1. Add to temporary reward pool.
2. Complete/cash in a trivial quest.
3. Confirm ordinary saddle arrives.

This test is trivial mechanically but important to the design decision to remove the convenience saddle recipe and make stable/caravan work a natural acquisition path.

---

# Explicitly do not test as progression rewards yet

- `majruszsaccessories:certificate_of_taming` — upstream Forge 1.20.1 bug report; verify manually before attaching any quest to it.
- Swimmer's Guide — Aquamirae compatibility concern; do not put in North progression until tested with the current pack.
- Quality Equipment curated gear — first capture exact working stack data/NBT from the installed pack.
- filled/warmed/cooled Waterskin — capture real item state first.
- direct tamed-animal rewards — ownership transfer is a separate runtime test.

# Definition of verified

A reward is `FULL-PACK VERIFIED` only when:

1. exact registry ID is known;
2. native behavior works in the current CozyCrazyCraft instance;
3. Bountiful delivers the expected stack;
4. delivered stack behaves identically to a naturally obtained stack;
5. reward power fits its assigned tier;
6. any configuration prerequisite is documented.
