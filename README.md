# CozyCrazyCraft Quest System

Player-facing quest, bounty, map, cartographer, reward, and information-layer design for the CozyCrazyCraft Forge 1.20.1 modpack.

## Status

This repository is intentionally split into two layers:

1. **Safe content/data work now** — Bountiful 6.0.4 configuration, verified objective/reward patterns, quest catalogs, proof-item specifications, reward balancing, and test plans.
2. **Deferred integration work** — regional board assignment, useful-structure locating, same-board redemption, structure proof placement, and main-story state. These wait for the separate zoning/world-substrate project to expose its final APIs.

The current rule is simple: **do not invent a complicated event system when Bountiful or vanilla criteria can already do the job reliably.** Anything not proven reliable is marked `YELLOW` or `DEFERRED`, not silently treated as implemented.

## Current baseline

- Target: Minecraft Forge 1.20.1
- Bountiful: `6.0.4+1.20.1-forge`
- Default Bountiful bounty pools/decrees: excluded in the deployment baseline
- Only CozyCrazyCraft custom pool/decree content should load after the baseline is installed
- Initial smoke-test decree: `Local Notices`

See:

- `docs/BOUNTIFUL_6_0_4_RELIABILITY.md`
- `docs/QUEST_FRAMEWORK.md`
- `docs/INTEGRATION_BOUNDARY.md`
- `docs/TEST_PLAN.md`
- `deployment/config/bountiful/`

## Geographic model

The final quest layer will consume two authoritative world classifications from the zoning project:

- radial tier: Hearthlands / Frontier / Wildlands / Dread Reaches
- macro-region: North / East / South / West

This repository does **not** duplicate that geographic logic.
