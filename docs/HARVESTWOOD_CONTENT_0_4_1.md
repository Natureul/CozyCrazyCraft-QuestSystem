# Harvestwood / WEST executable content — 0.4.1

This branch is a merge-friendly regional content pass built on `2e413cba282af6a11d5372b177d6c51d9df4503b`.
It deliberately does **not** alter quest completion, hint, navigation, structure-survey, or named-place runtime code.

## Executable additions

`HarvestwoodQuestCatalog` adds twelve authored contracts using only the existing runtime primitives.

### Hearthlands
- **A Guardian in the Field** — visit the verified Born in Chaos farm; observe the friendly Pumpkin Spirit context; learn the intentional Pumpkin Spirit creation mechanic in the completion dialogue.
- **The Fog-Watch Ledger** — recover a ledger from a generated forest observation tower and begin the old-road evidence thread.

### Frontier
- **Two Towers, One Story** — recover corroborating road-watch evidence.
- **The Road Around the Mound** — clear a Mound of Hounds as a route-opening job rather than a wilderness extermination bounty.
- **The Old-Road Maker's Stamp** — recover professional evidence from a Valhelsia Forge while treating any actual resident as a workshop inhabitant, not an invented blacksmith.
- **Where the Timber Road Split** — recover a route plate from a verified Frontier ruin.

### Wildlands
- **The Road Goes Quiet** — clear a strategic hound mound so a reliable road exists before deeper investigation.
- **Ash Where No Barn Burned** — recover evidence from an Infernal Pumpkin site; completion dialogue teaches the Pumpkin Spirit → Seared Spirit → Infernal Spirit transformation chain without handing out a Transmuting Elixir.
- **Three Roads, One Rider** — corroborate the headless-road pattern. It explicitly tells the player not to hunt Sir Pumpkinhead from rumor alone.

### Dread Reaches
- **The Last Reliable Road** — recover a route plate from one exact generated Dark Tower instance.
- **What the Old Alchemists Changed** — recover a transmutation folio; completion dialogue teaches the Seared/Infernal and iron→Dark Metal uses while keeping the elixir economically gated.
- **Made for One Journey** — clear the assigned Dark Tower approach and receive high-grade preparation gear. This is readiness content, not the Lord Pumpkinhead final.

## Dialogue additions

Every quest has a WEST-prefixed offer, active, and turn-in Conversations resource: **36 new dialogue JSON files**.
The writing keeps normal beats short, uses old-road / orchard / fog / timber vocabulary, distinguishes friendly Pumpkin Spirits from corruption, and gives post-completion consequences or knowledge instead of generic congratulations.

## Registry assumptions

| Target | Use here | Authority assumption |
| --- | --- | --- |
| `born_in_chaos_v1:farm` | Hearthlands observation/knowledge | Exact registered structure; verified one farmer + friendly Pumpkin Spirit. |
| `born_in_chaos_v1:observation_tower_forest` | Hearthlands, Frontier, Wildlands evidence | Exact registered structure. Higher-tier definitions only become offerable when the existing resolver finds a real same-region instance in the required radial band. |
| `born_in_chaos_v1:mound_of_hounds` | Frontier/Wildlands route clear | Exact registered structure; Frontier+ structure policy. |
| `valhelsia_structures:forge` | Frontier professional recovery | Exact registered structure; verified one resident path. Runtime profession is not assumed. |
| `valhelsia_structures:castle_ruin` / `tower_ruin` | Frontier route recovery | Exact registered structures used as ruins/landmarks, not invented settlements. |
| `born_in_chaos_v1:infernal_pumpkin` | Wildlands evidence | Exact registered structure. The quest is withheld unless the existing resolver finds a real legal Wildlands instance. |
| `born_in_chaos_v1:dark_tower_forest` / `_plain` / `_taiga` | Dread reconnaissance/readiness | Exact Dread-gated registered structures. No normal resident is invented. |

## Protected encounters

- **Sir Pumpkinhead:** no kill objective added. The new Wildlands arc stops at evidence, route preparation, and corroboration.
- **Lord Pumpkinhead:** no kill objective added. Dread content stops at route reconstruction, occult records, and one-journey readiness.
- **Tunnel Gore:** not referenced anywhere in the executable catalog.

## Born in Chaos systems surfaced

- Ethereal Spirit as the key that awakens a built scarecrow into a Pumpkin Spirit.
- Friendly Pumpkin Spirit as a defensive guardian rather than a default enemy.
- Fire as the corruption/transformation step into Seared Spirit.
- Transmuting Elixir as the Seared→Infernal step and, later, an iron→Dark Metal transmutation tool.
- Dark Metal knowledge at Dread tier.
- Transmuting Elixir is never granted as a repeatable quest reward.
- Fel Lamp/Felsteed is deliberately held for post-Sir progression rather than leaked early.

## Runtime mechanics deliberately not added

These ideas are valid but need shared/runtime work and therefore remain documented instead of being smuggled into regional content:

1. **Direct Born in Chaos farm-resident Conversations.** The current authored village-quest manager resolves `VillageContext`; a one-person farm should use a future `HabitationContext`/micro-node path rather than pretending to be a village.
2. **Hard chain prerequisites.** The current `Definition` schema does not encode prerequisite quest IDs. List order creates a useful first-available cadence, but a formal Sir/Lord chain should use explicit shared prerequisite state.
3. **Explicit issuer macro-region.** Definitions have `sameMacroRegion` target semantics but no separate `issuingRegion`. This branch does not change that shared schema.
4. **Nonlethal entity observation / photography.** No dedicated objective primitive exists yet, so this pass uses real structure visits and recovery evidence instead of converting wildlife into kill counters.
5. **Semantic Quality floors / reforge service.** Reward names and enchantments are executable now; positive/strong-positive Quality guarantees should wait for the actual Quality Equipment API/config contract.
6. **Sir/Lord exact encounter completion.** Sir's authoritative encounter/kill-credit path and Lord's final destination path must be solved before those fights are made executable.
7. **Fel Lamp/Felsteed chapter.** Add after the Sir encounter can be completed and its actual drop/progression state can be detected.

## Merge surface

New content is isolated to:
- `HarvestwoodQuestCatalog.java`
- WEST-prefixed Conversations JSON
- this handoff document
- a Harvestwood executable manifest

The only shared-file change is the catalog registration line in `VillageQuestCatalog.ALL`.
No shared completion, hint, navigation, named-place, or structure-survey implementation is changed.
