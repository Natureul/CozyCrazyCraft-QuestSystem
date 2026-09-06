# Quest & Reward Master Bible v0.2 Integration

Status: semantic/design import for the 0.3.0 authored-dialogue branch.

The v0.2 Bible is now the content/design authority for the next authored-villager expansion. The runtime proof-of-concept is still intentionally small: one full Conversations quest lifecycle is being proven before hundreds of records are allowed to execute in-game.

## What v0.2 changes

v0.2 expands the content bank from 320 regional hooks to 480 by adding 160 Alex's Mobs ecology hooks: 40 per macro-region and 10 per radial tier. It also adds a unique-reward identity layer, native Alex's Mobs rewards, special village gratitude rewards/companions, and explicit ecology-selection rules.

The imported data lives under:

- `data/ecology/alexsmobs_policy_v0_2.json`
- `data/ecology/alexsmobs_hooks_*_v0_2.json`
- `data/rewards/unique_signature_rewards_v0_2.json`
- `data/rewards/alexsmobs_native_rewards_v0_2.json`

These files are semantic source data. They do not mean every hook is already executable by the Java runtime.

## Local ecology is a local fact

The existing local-knowledge rule applies to creatures as strongly as it applies to structures.

A village may only offer a species-specific contract when the runtime can justify that the species is locally relevant. The authored region/tier is a content gate, not evidence that the mob exists near this particular village. Before an offer becomes visible, the selector should prefer evidence in this order:

1. a recent local sighting/event already remembered by the village;
2. an actual nearby loaded/observed population;
3. a nearby biome/spawn-capability match that makes the species reasonably locatable;
4. otherwise, withhold the quest.

Do not continuously scan the world for Alex's Mobs. Resolve ecology facts lazily when a dialogue pool is built, cache the result briefly, and freeze any exact target/route fact when the player accepts.

## Ecology role comes before kill count

Each ecology candidate is classified before the objective is selected:

- observe;
- protect/coexist;
- tame/breed;
- material;
- controlled hunt;
- predator hunt;
- elite hunt.

Peaceful herd/bird species are not members of a generic kill lottery. Herd animals favor census/protection or small justified culls. Rare solitary creatures favor one-off discovery or one-target authored hunts. Pack creatures may use natural pack-sized objectives. Predators can support direct hunts when the story makes them a real road, pasture, river, or expedition problem.

## Native proof beats fake proof when it is fair

Use a real Alex's Mobs material when it is deterministic enough and the item itself tells the story: one antler, horn, scute, rattle, feather, etc. Do not turn a story quest into RNG farming for a rare drop. Use objective state or a custom proof/recovery item when a native drop is unsuitable, random, or would force the player to kill an animal the quest intends to protect.

## Reward ownership

Named rewards have homes. They are not global loot-table decorations.

- Frostmarch: **WHITE REACH**, **PASS-WARDEN**, Froststalker/antler/cold-road equipment.
- Greenveil: **GREENWAKE**, **CANOPY GLIDER**, Vine Lasso, jungle traversal/scouting equipment.
- Sunscar: **SUNSPIKE**, **THE RED RETURN**, Roadrunner/Guster/caravan equipment. Sunscar owns the boomerang identity.
- Harvestwood: **HARVEST MOON**, **LAST HARVEST**, axes/scythes/old-road and eerie-forest equipment.

A reward can be mechanically reused elsewhere only when it does not erase the identity of the named/signature chain.

## Quality policy

Quality Equipment randomness is build variety, not punishment.

- Hearthlands prized: neutral floor or reforge support.
- Frontier named: neutral-to-positive floor.
- Wildlands signature: guaranteed positive.
- Dread first-clear: curated strong-positive or a two/three-item choice.

Names such as Frostforged, Wind-Cut, Canopy-Balanced, and Harvest-Honed are semantic/presentation identities until the live Quality Equipment config is frozen. Do not invent or hardcode stats from those names yet.

## Boss/story separation

Ecology work may establish local knowledge and earn trust toward Frostmaw, Umvuthi, Sir Pumpkinhead, Lord Pumpkinhead, Cornelia, or future regional finales. It does not replace those authored social chains.

The East Jungle Abomination and South Ancient Remnant remain future-story flags until their encounter packages are actually ready. Their existence in design prose must never make an unfinished final commission visible.

## Import sequence

1. Prove `The First Real Map` end-to-end in the real pack.
2. Generalize the runtime quest definition/selector around profession, trust, local facts, objective family, target, proof mode, repeat policy, reward band, and outward bias.
3. Import a small Hearthlands profession slice plus a small ecology slice.
4. Validate visible-pool diversity and local availability in play rather than enabling all 480 hooks at once.
5. Add reward composition with verified registry IDs/config behavior.
6. Only then expand through Frontier/Wildlands trust chains and Dread first-clears.

The point of the 480-hook bank is breadth of authored possibilities. It is not permission to dump 480 weighted records into a random selector.
