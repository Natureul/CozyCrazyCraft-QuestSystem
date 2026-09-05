# Deployment Baseline

This folder is a **safe Bountiful 6.0.4 smoke-test baseline**, not the finished regional quest system.

Copy the contents of:

```text
deployment/config/bountiful/
```

to the modpack instance's:

```text
config/bountiful/
```

## What this does

`bountiful.json` excludes all datapack-provided Bountiful bounty pools and decrees:

```json
"dataPackExclusions": [
  "bounty_pools/*",
  "bounty_decrees/*"
]
```

Bountiful's exact 1.20.1 `ResourceLoadStrategy` filters datapack resources first, then separately loads JSON files found in the config-pack directories. Therefore the custom files in:

```text
config/bountiful/bounty_pools/
config/bountiful/bounty_decrees/
```

still load after the exclusions.

Result:

- built-in Bountiful bounty pools are excluded
- built-in Bountiful decrees are excluded
- Bountiful datapack compatibility pools from other mods are also excluded
- the Bounty Board block/mechanics remain
- village board generation remains
- only CozyCrazyCraft config-pack pools/decrees should be available

## Why this is deliberately only one decree right now

The current custom decree is:

```text
Local Notices
```

There is only one because the final North/East/South/West board assignment depends on the zoning project.

In Bountiful 6.0.4, a pristine board creates a random decree from the currently loaded decree list. If we loaded four regional decrees before the quest layer can assign the correct one by geography, a western board could randomly become a jungle board.

Until regional assignment exists, one neutral smoke-test decree is safer.

## Current smoke-test content

Objectives use only source-verified GREEN types:

- exact items
- item tags
- entity kills

Rewards use only ordinary items.

No criteria objectives.
No command rewards.
No custom NBT.
No structure-specific proof items yet.
No dynamic locating.
No region-dependent code.

This is intentional.

## Existing-world warning

Bounty boards store their decree items in block-entity NBT.

A board created before this custom-only configuration may still physically contain an old decree ID. Since the underlying decree resource is now excluded, old test-world boards are not a clean validation target.

For the baseline test, use either:

- a new world, or
- a newly placed/fresh board after removing/resetting the old decree contents.

Do not diagnose regional-quest behavior from a stale pre-baseline board.
