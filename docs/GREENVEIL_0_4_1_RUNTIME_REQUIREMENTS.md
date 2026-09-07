# Greenveil / EAST 0.4.1 Runtime Requirements

This branch deliberately does **not** change shared quest-runtime, navigation, structure-completion, hint, or HabitationContext Java. It owns regional data and dialogue and records the hooks integration still needs.

## 1. Frontier/Wildlands Bountiful board selection

`ccc_frontier_east` and `ccc_wildlands_east` now have complete objective/reward pools and are marked `LIVE_DATA` in `data/board_decree_assignment.json`. The JSON is valid Bountiful content. If the current integration runtime does not yet consume `board_decree_assignment.json` for tiered pristine-board stamping, that shared board/decree hook remains the only activation requirement. Do not recreate that hook in regional Java.

## 2. Dungeons Enhanced Jungle Monument prisoner

`dungeons_enhanced:jungle_monument` contains a real caged jungle villager. The existing `frontier_overgrown_route` may secure the approach, but a true rescue needs shared support for: captive protection before intervention; detecting cage/rescue transition; post-rescue Conversation routing; and a redundant knowledge route if the prisoner dies. The rewritten live dialogue no longer pretends a kill count equals a rescue.

## 3. Dungeons Enhanced Tree House resident

`dungeons_enhanced:tree_house` has one real jungle villager and should become a lightweight personal/HabitationContext node, not a miniature village. Staged Conversation JSON lives under `data/greenveil/staged_conversations/`. Runtime needs a verified-resident context selector before those files should be copied into the live Conversations resource path.

## 4. Ecology objectives that require location awareness

Carniflore and Foliaath are E5 stationary territorial encounters. They should not become global Bountiful kill quotas. To make the staged boundary/route quests live, shared objective support must bind completion to a known patch, named place, or encounter anchor. The same rule applies to a crocodile crossing: observation, photography, safe-route mapping, or handling one proven dangerous individual is preferred over species culling.

## 5. Photography / observation / protection

Greenveil needs noncombat ecology verbs. The current generic quest runtime does not prove observation, photograph, protect, relocate, or route-around completion. Regional content therefore stages those objectives instead of faking them with kill counts. Shared runtime should expose reusable evidence types for photographed entity/place, observed entity without kill, protected/escorted entity, and surveyed safe crossing.

## 6. Leapleaf, Whisperer, Mother Spider, Matriarch

Danger authority makes Leapleaf an E4 apex, Whisperer an E3 specialist, and Mother Spider/Matriarch-class content conditional on mechanics testing. This branch intentionally does not author new registry IDs for them and does not put them in generic Bountiful pools. Spawn density and encounter qualification belong to the ecology/spawn-control integration.

## 7. Signature rewards

The desired Greenveil ladder remains Quarterstaff -> River Javelin / Survey Bow / Canopy Glider -> Greenwake Glaive -> deep-canopy mobility/armor. Only already-authoritative or vanilla registry IDs are granted here. `data/greenveil/greenveil_reward_ladder_v0_4_1.json` lists every semantic signature reward still waiting on installed-registry and Quality/enchantment verification. Do not guess IDs in regional content.

## 8. Jungle Abomination

There is no mandatory EAST Wildlands boss. Jungle Abomination remains `FUTURE_FEATURE_GATE_DISABLED`, with no registry ID, live quest, structure target, completion trigger, or reward dependency in this branch. When the encounter is genuinely production-ready, enable it through a shared feature gate and add Dread content then.
