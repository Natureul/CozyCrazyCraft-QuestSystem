# Reward Validation Backlog

This is a design/audit queue, **not an implementation claim**.

A candidate moves to `VERIFIED` only when we know:

1. exact installed registry ID
2. exact installed version behavior
3. whether it is naturally/craftably obtainable already
4. progression power
5. whether Bountiful can deliver the stack without losing required data
6. if NBT is involved, exact stack NBT captured from the running pack

Status meanings:

- `VERIFIED` — exact ID/mechanic + Bountiful delivery tested
- `SOURCE-CHECKED` — mechanic/source known, full-pack delivery test still needed
- `CANDIDATE` — good design fit; exact implementation still needs audit
- `DEFERRED` — depends on custom runtime/world integration

---

# North

| Candidate | Mod/system | Intended role | Tier idea | Status / next check |
|---|---|---|---|---|
| Thermometer | Cold Sweat | teach temperature instead of relying on JEI | Hearthlands | CANDIDATE — verify exact item ID and whether player already receives one through other progression |
| Wool / goat-fur insulation | Cold Sweat | first cold preparation | Hearthlands | CANDIDATE — audit exact insulation item/recipe behavior |
| Sewing Table | Cold Sweat | unlock deliberate insulation progression | Frontier | CANDIDATE — exact ID + power/recipe audit |
| Waterskin / warm-water preparation | Cold Sweat | expedition utility | Frontier | CANDIDATE — verify exact current version behavior |
| Hearth / Boiler | Cold Sweat | settlement/base infrastructure | Frontier | CANDIDATE — probably better as infrastructure than random reward |
| Tall-quality boots | Quality Equipment | +step-height traversal identity | Hearthlands/Frontier | CANDIDATE — capture exact Quality Equipment NBT with `/bo hand` before authoring |
| Long-quality spear/pike | Spartan Weaponry + Quality Equipment | keep large northern threats at reach | Hearthlands/Frontier | CANDIDATE — exact weapon ID + quality NBT + compatibility |
| Horse Frost Walker | Majrusz's Enchantments | frozen mounted travel | Frontier | CANDIDATE — exact enchant/item application test |
| Quiver | Supplementaries | hunter utility | Hearthlands/Frontier | CANDIDATE — exact item ID + inventory behavior |
| Falconry equipment / eagle taming aid | Alex's Mobs | regional companion knowledge | Hearthlands/Frontier | CANDIDATE — verify installed eagle-taming loop and exact items |
| White Reach | custom curated stack | T3 signature diamond pike | Wildlands | DEFERRED — needs exact stack NBT and authored issuance path |
| swimmer/angler accessory | Majrusz's Accessories | frozen-coast preparation | Wildlands | CANDIDATE — verify exact accessory IDs and effects |

---

# East

| Candidate | Mod/system | Intended role | Tier idea | Status / next check |
|---|---|---|---|---|
| Chameleon Molt/adaptive insulation | Cold Sweat | adaptive survival; useful later North/South too | Hearthlands/Frontier | CANDIDATE — verify item ID, source, insulation behavior |
| Springy-quality leggings | Quality Equipment | jungle mobility without flight | Hearthlands/Frontier | CANDIDATE — capture exact NBT |
| Graceful-quality saber | Spartan Weaponry + Quality Equipment | fast dense-terrain weapon | Hearthlands | CANDIDATE — IDs/NBT |
| Nature Rune | Majrusz's Accessories | crops/animals green-region utility | Frontier | CANDIDATE — verify exact current accessory behavior/ID |
| Poison-resistance pet enchant | Domestication Innovation | bring pets into hostile jungle | Frontier | CANDIDATE — exact enchant ID + applicability |
| pet flute | Supplementaries / pet systems | animal-management utility | Frontier | CANDIDATE — exact item behavior |
| tea/coffee/kettle starter | Farmer's Respite | peaceful herbalist progression | Hearthlands/Frontier | CANDIDATE — exact IDs and balance |
| capuchin taming aid | Alex's Mobs | regional companion | Hearthlands/Frontier | CANDIDATE — verify taming item and behavior |
| Ancient Dart path | Alex's Mobs | upgrade an existing capuchin companion | Wildlands | CANDIDATE — verify installed mechanic; reward should likely be native discovery, not random board loot |
| backpack Unlock Token | Backpacked | expedition inventory growth | Frontier/Wildlands | CANDIDATE — verify exact item ID/current config |
| Greenwake | custom curated stack | T3 signature diamond glaive | Wildlands | DEFERRED — authored issuance + exact NBT |

---

# South

| Candidate | Mod/system | Intended role | Tier idea | Status / next check |
|---|---|---|---|---|
| Acacia Blossoms | Alex's Mobs | enable elephant taming | Hearthlands/Frontier | CANDIDATE HIGH PRIORITY — user's benchmark reward; verify exact ID/current taming count/mechanic |
| Certificate of Taming | Majrusz's Accessories | animal-focused reward | Frontier | CANDIDATE — exact ID/effect |
| Saddle | vanilla | normal civilization/animal reward instead of convenience recipe | Hearthlands | mechanically GREEN as item reward; balance/quest pairing pending |
| Speedy-quality boots | Quality Equipment | open-country travel | Frontier | CANDIDATE — exact NBT |
| Ancient Scarab | Majrusz's Accessories | archaeology progression | Frontier | CANDIDATE HIGH PRIORITY — exact ID/effect |
| Waterskin / cooled water | Cold Sweat | desert survival | Hearthlands/Frontier | CANDIDATE — exact ID/NBT/state behavior |
| Icebox / cooling infrastructure | Cold Sweat | settlement/base cooling | Frontier | CANDIDATE — likely authored infrastructure rather than random reward |
| Supply Cart | AstikorCarts Redux | land logistics without skipping geography | Frontier | CANDIDATE HIGH PRIORITY — exact item/entity creation path and whether direct reward is appropriate |
| Animal Cart | AstikorCarts Redux | livestock/passenger logistics | Frontier/Wildlands | CANDIDATE |
| Kangaroo companion | Alex's Mobs | useful storage/mobility companion | Frontier | DEFERRED if directly tamed; can instead reward taming means if native loop supports it |
| Sunspike | custom curated stack | T3 signature diamond lance | Wildlands | DEFERRED — exact NBT/authored issuance |

---

# West

| Candidate | Mod/system | Intended role | Tier idea | Status / next check |
|---|---|---|---|---|
| Tamed Potato Beetle | Majrusz's Accessories | memorable agricultural utility | Hearthlands | CANDIDATE HIGH PRIORITY — verify exact ID/effect |
| Wind Chime | Chimes | home/night utility | Hearthlands | CANDIDATE — exact ID + phantom interaction in installed version |
| Broad-quality battleaxe | Spartan Weaponry + Quality Equipment | woodsman identity | Hearthlands | CANDIDATE — IDs/NBT |
| Solid-quality shield | Quality Equipment | defensive graveyard/forest reward | Frontier | CANDIDATE — exact NBT |
| Household Rune | Majrusz's Accessories | home/civilization utility | Frontier | CANDIDATE — exact current effect/ID |
| Dream Catcher | Majrusz's Accessories | home/night utility | Frontier | CANDIDATE — exact current effect/ID |
| Plow | AstikorCarts Redux | agriculture/settlement progression | Frontier | CANDIDATE HIGH PRIORITY — exact item/entity path |
| farming backpack augment | Backpacked | agriculture utility | Frontier | CANDIDATE — exact current augment IDs/effects |
| crow interaction/taming supplies | Alex's Mobs | woodland companion/utility | Hearthlands | CANDIDATE — verify exact taming/use loop |
| Harvest Moon | custom curated stack | T3 signature diamond scythe | Wildlands | DEFERRED — exact NBT/authored issuance |

---

# Cross-regional / global

| Candidate | Mod/system | Intended role | Status |
|---|---|---|---|
| Backpack Unlock Token | Backpacked | highly desirable medium-tier reward | CANDIDATE HIGH PRIORITY |
| pet beds/collars | Domestication Innovation | makes companion rewards sustainable | CANDIDATE |
| specific pet enchants | Domestication Innovation | companion specialization | CANDIDATE — audit each enchant ID/applicability before assignment |
| camera / album | Camera Mod | survey/naturalist flavor | CANDIDATE — do not build photo-verification objective unless proven simple |
| instruments | Immersive Melodies | flavorful non-combat reward | CANDIDATE |
| furniture/decor | Another Furniture / Valhelsia Furniture / Beautify | low-stakes village/home rewards | CANDIDATE — use sparingly, not as core progression |
| Hang Glider | Hang Glider | earned traversal unlock | CANDIDATE — progression placement review |
| Small Ships | Small Ships | earned water traversal | CANDIDATE — exact item/entity award flow |
| Immersive Aircraft | Immersive Aircraft | potentially world-skipping traversal | DESIGN REVIEW — likely remove or strongly gate; no normal early reward |
| weather instruments | Weather2 | civilian/survey flavor | CANDIDATE — objective detection not required; ordinary item reward is easy once IDs verified |

---

# Things that should usually stay native loot

Do not turn boards into vending machines for the signature rewards of major destination mods.

Examples:

- Alex's Caves high-value cave equipment
- Aquamirae major equipment/boss loot
- major boss-native trophies
- Cursed Pyramid/Cataclysm package loot if/when isolated
- special structure rewards whose discovery is the point of the dungeon

Quest rewards should generally provide:

- preparation
- utility
- regional knowledge
- a reason to go
- an occasional carefully authored signature item

not replace the destination's own reward loop.
