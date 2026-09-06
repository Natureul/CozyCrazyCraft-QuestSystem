# Structure Recovery Contracts — Playtest V1

Recovery Contracts are the first bridge between the village notice board and real places in the world.

The loop is deliberately simple:

1. help a settlement enough to build Village Trust;
2. a recovery job can begin appearing on that settlement's regional board;
3. travel to the named kind of structure;
4. find a unique proof item in the structure's distinctive chest;
5. carry that item back while holding the accepted Bountiful bounty;
6. Bountiful's normal item objective consumes the proof during turn-in and awards the bounty.

The proof is a **real custom item ID**, not a renamed vanilla item or an NBT-only token. That matters because Bountiful 6.0.4 reliably matches item objectives by registry ID and consumes the matching inventory stack on completion.

## First four contracts

| Region | Board job | Target | Proof | Trust |
|---|---|---|---|---:|
| Harvestwood | The Empty Stables | `dungeons_enhanced:stables` | Stablemaster's Seal | 2 |
| Sunscar | The Buried Record | `dungeons_enhanced:desert_tomb` | Sunscar Tomb Tablet | 2 |
| Greenveil | Notes from the Green | `dungeons_enhanced:jungle_monument` | Greenveil Survey Notes | 3 |
| Frostmarch | Dispatch from the Ice | `dungeons_enhanced:ice_pit` | Frostmarch Dispatch | 3 |

The Greenveil and Frostmarch jobs intentionally point outward into Frontier content. They are not Newcomer chores; they are the kind of information a settlement starts sharing after the player is already Respected.

## Why loot-table injection is the V1 mechanism

The runtime adds the proof item only when a chosen Dungeons Enhanced chest loot table is loaded:

- `dungeons_enhanced:chests/stables`
- `dungeons_enhanced:chests/desert_tomb`
- `dungeons_enhanced:chests/jungle_monument/treasure`
- `dungeons_enhanced:chests/ice_pit/armory`

This has several useful properties for CozyCrazyCraft:

- no global structure scans;
- no repeating server tick work;
- no fake chest spawned beside the real structure;
- no NBT matching dependency;
- naturally discovered proof can still become useful later;
- a structure remains valuable even if the player reached it without a quest marker.

V1 guarantees the proof **per invocation of the selected loot table**, not mathematically one-per-structure. If a structure uses its selected table more than once, duplicate proofs can exist. They have no crafting recipe, stack to one, and have no direct economic value, so this is safe for the first playtest. If duplicates feel silly in practice, the next implementation can stamp one structure instance after generation instead.

## Fast smoke test

Use a test world or chunks where the target chest has not been opened before installing the runtime. Loot tables are rolled when the container first generates its contents; an already-opened chest will not magically gain the proof.

Useful commands:

```text
/locate structure dungeons_enhanced:stables
/locate structure dungeons_enhanced:desert_tomb
/locate structure dungeons_enhanced:jungle_monument
/locate structure dungeons_enhanced:ice_pit
```

For a pure Bountiful turn-in test without travel, the corresponding items can be given manually:

```text
/give @s cozycrazyquests:stablemasters_seal
/give @s cozycrazyquests:sunscar_tomb_tablet
/give @s cozycrazyquests:greenveil_survey_notes
/give @s cozycrazyquests:frostmarch_dispatch
```

The travel test is the important one: locate a fresh structure, open its designated chest, verify the named glowing proof exists, and return it through the board bounty.

## What this is not yet

The recovery job currently names a *type* of place; it is not yet permanently bound to one exact structure instance. Cartographers and the useful-target locator are still the intended location layer. Eventually a board can issue the reason for the expedition while a cartographer/found map points to an actual nearby target.

Same-issuing-board turn-in is also still a separate runtime refinement. Stock Bountiful accepts a completed bounty at any board; CozyCrazyCraft's preferred behavior remains returning civic contracts to the settlement that issued them.
