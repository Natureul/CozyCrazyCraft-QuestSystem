# Authored Villager Quest Architecture

Status: 0.3.0 foundation

## Roles

CozyCrazyCraft intentionally uses more than one quest surface.

- **Bountiful board — Public Notices.** Small repeatable civic work. Fast supply, hunt, and ordinary local needs. This is background village life, not the main adventure generator.
- **Conversation villagers — Profession Requests.** Authored work whose giver matters. Farmer, Fletcher, Librarian, Cartographer, Cleric, Armorer, Weaponsmith, Toolsmith, Mason, Leatherworker, Butcher, Nitwit, and ordinary citizens should have materially different quest vocabularies.
- **Trust Contracts — deterministic milestone work.** Once prerequisites are met, an important contract becomes available; it is not hidden behind random board rotation.
- **Regional Story — chapter/first-clear commissions.** Frostmaw, Umvuthi, Sir/Lord Pumpkinhead, Cornelia, and future regional finales belong here rather than in the repeatable lottery.

## The Local-Knowledge Rule

A village may only issue a world-specific quest when the runtime can justify why that village knows about the target.

Examples of valid local facts:

- a real nearby generated structure;
- a nearby route/biome/landmark that can be resolved;
- a regionally legal hostile ecology;
- a village event or remembered prior quest outcome;
- a map/record/relic recovered in an earlier chain step;
- a trusted Cartographer deliberately extending the village's knowledge one tier outward.

A quest definition naming a structure family is **not** proof that a suitable instance exists. Runtime must resolve an actual instance or withhold that quest.

## Spatial Guardrails

1. Resolve the issuing village cell through CozyCrazyZones.
2. Ordinary profession requests strongly prefer the current radial tier.
3. A controlled request may target at most one tier outward when its profession/story justifies it.
4. Outside the shared Hearthlands core, ordinary local requests should remain in the issuing macro-region.
5. Cartographers and explicit Trust/Story contracts are the main intentional outward-navigation tools.
6. A resolved target is cached into the accepted contract. It never silently rerolls because the player moved, relogged, or another structure was discovered.

## Performance Rule

Do not continuously scan the world for quest targets.

Structure availability is resolved lazily when an offer needs it, then cached. Negative results are cached briefly as well. Accepted quests retain the exact target in player state. Continuous per-player work is limited to cheap checks for objectives that are actually active.

## Selection Shape

When the authored catalog grows, candidate selection should apply these stages in order:

1. issuing village cell;
2. villager profession;
3. Village Trust and chapter prerequisites;
4. local facts / real target availability;
5. same-tier and outward-bias rules;
6. repeat/first-clear/cooldown state;
7. objective-family diversity;
8. difficulty and reward band.

Do not solve a boring visible pool by increasing the number of weighted entries. Diversity is selected deliberately.

## Objective-Family Guardrails

- Never surface more than two offers with the same core verb in one authored choice set.
- Pure fetch and pure kill jobs are palate cleansers, not the majority of visible content.
- Frontier+ content should usually involve a place, route, recovery object, investigation, trial, or other authored circumstance.
- A structure-clear story does not automatically need an arbitrary kill counter.
- Recovery/proof items exist to make the requested object specific without inventing convenient permanent drops for every modded mob.

## Trust

There is one village trust ledger. Authored villager completions increment the same Bountiful board completion state used by public notices. Do not create a second parallel reputation number unless Bountiful is eventually replaced entirely.

Major contracts are unlocked deterministically by Trust/chapter state. Reaching the requirement means the village can offer the contract; it does not merely increase a random weight.

## 0.3.0 Proof: The First Real Map

The first Conversations lifecycle deliberately tests the architecture with one Cartographer quest:

- Hearthlands Cartographer only;
- requires an existing recorded issuing village/board;
- resolves a real nearby Watch Tower, Tower Ruin, or Stables instance;
- allows current tier or one tier outward;
- requires the same macro-region outside the shared core;
- caches exact target ID and position on acceptance;
- marks the survey complete near the cached target;
- returns to a Cartographer in the issuing village;
- pays a contextual utility reward and increments Village Trust;
- first-clear per player per village.

Only after this lifecycle works in the real modpack should the larger profession and Trust-chain catalog be imported.
