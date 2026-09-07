# Greenveil / EAST Content Report — 0.4.1

## Scope

Regional-only content pass from base `2e413cba282af6a11d5372b177d6c51d9df4503b`. Shared quest-runtime/navigation/completion Java is untouched.

## Executable content

- 12 new Bountiful objective entries: 6 Frontier + 6 Wildlands.
- 12 new Bountiful reward entries: 6 Frontier + 6 Wildlands, restricted to vanilla/authoritative IDs.
- 2 new EAST decrees: `ccc_frontier_east` and `ccc_wildlands_east`.
- 6 existing live Conversations quest chains rewritten around Greenveil ecology, routes, floods, caves, territorial-place logic, and short speech beats.
- 18 live Conversation resources rewritten (offer/active/turn-in triplets).
- 5 staged Conversation resources for the Tree House resident, Jungle Monument survivor, and nonlethal crocodile-crossing work.

## Structure use

- `dungeons_enhanced:jungle_monument`: live hostile-approach/major-expedition use; actual prisoner rescue remains shared-runtime gated.
- `dungeons_enhanced:tree_house`: staged lone-resident knowledge node.
- `betterjungletemples:jungle_temple`: live Frontier and Wildlands expedition destination.
- `bettermineshafts:mineshaft_lush`: live recovery/cave route.
- `bettermineshafts:mineshaft_overgrown`: live recovery/side expedition.
- `bettermineshafts:mineshaft_jungle`: verified Frontier cave candidate, staged until a specific objective binding is needed.

## Ecology discipline

Foliaath and Carniflore are kept as E5 territorial/place hazards rather than Bountiful species-cull targets. Zombiflore (E2/E3) is the only new regional hostile added to the repeatable Frontier/Wildlands pools. Crocodile work is staged around crossing ecology. Leapleaf and Whisperer are named only semantically with null registry IDs pending local registry/spawn audit.

## Reward identity

The branch adds practical survey/travel rewards now: maps, compass, spyglass, boots, rations, ender pearls, emerald/diamond pay. It preserves the Greenwake diamond glaive already used by the live Wildlands catalog and records the intended quarterstaff, River Javelin, Survey Bow, Canopy Glider, ecology-derived gear, camera kit, and curated Quality/enchant ladder without inventing unverified IDs.

## Future gated

- Jungle Monument captive rescue state and post-rescue knowledge handoff.
- Tree House HabitationContext.
- Observation/photo/protection objective types.
- Location-aware Carniflore/Foliaath territory completion.
- Leapleaf/Whisperer registry/spawn qualification.
- Jungle Abomination: disabled future Dread feature; no symmetry boss was added.
