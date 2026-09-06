# CozyCrazyCraft Regional Reward Ladders

This page is the human-readable reward companion to [`QUESTS.md`](QUESTS.md).

The goal is not to make every quest pay in stronger armor. The best regional rewards should make the player **better at living in, traveling through, or understanding that region**.

Status language:

- **Safe vanilla** — ordinary item reward is mechanically straightforward now.
- **Source-checked** — exact item path/mechanic is supported by upstream source; still needs one full-pack Bountiful delivery/use test.
- **Candidate** — strong design fit; exact installed registry ID / behavior still needs audit.
- **Hold** — known compatibility/behavior concern; do not make progression depend on it yet.
- **Curated stack** — weapon/armor reward needs exact Spartan Weaponry + Quality Equipment + enchantment stack validation.
- **World/system reward** — map, reputation, settlement change, pet ownership, etc.; requires the corresponding integration layer.
- **Native destination loot** — should stay in the dungeon/boss rather than become a bounty-board vending reward.

> Regional association is not global exclusivity. A reward can be especially natural in one region without becoming unusable or impossible elsewhere.

For the exact source findings and caution notes, see [`docs/REWARD_SOURCE_AUDIT.md`](docs/REWARD_SOURCE_AUDIT.md).

---

# Frostmarch — North

**Reward fantasy:** warmth, footing, reach, hunting, mountain travel, frozen-water travel, expedition readiness.

## Hearthlands

| Reward | Why it belongs here | Status |
|---|---|---|
| Thermometer (`cold_sweat:thermometer`) | Teaches Cold Sweat through play instead of JEI | Source-checked |
| Wool / early insulation bundle | Makes first cold preparation understandable | Candidate |
| **Trailwarden** iron spear/pike | Reach is useful against open-country and large northern threats | Curated stack |
| Tall-quality boots | Step-height utility quietly improves snowy/hilly travel | Curated stack |
| Quiver / hunter supplies | Makes northern hunting feel like its own kit | Candidate |

## Frontier

| Reward | Why it belongs here | Status |
|---|---|---|
| Sewing Table (`cold_sweat:sewing_table`) | Turns insulation into deliberate progression | Source-checked |
| Better cold-insulation materials | The environment now expects actual preparation | Candidate |
| Horse Frost Walker | A wonderful region-specific mount upgrade | Candidate |
| Backpack Unlock Token (`backpacked:unlock_token`) | Long expeditions justify more carrying capacity | Source-checked |
| Stronger hunting bow/crossbow | Fits mountain/watch contracts | Curated stack / Candidate |

## Wildlands

| Reward | Why it belongs here | Status |
|---|---|---|
| **White Reach** diamond pike | Signature T3 northern expedition weapon | Curated stack |
| Serious pet/hunter enchant | Companion can develop alongside the player | Candidate |
| Swimmer's Guide | Tempting frozen-coast utility, but an upstream Aquamirae compatibility report makes this a bad progression dependency right now | **Hold** |
| Angler / simpler coast utility | Frozen coast preparation without making the Aquamirae path depend on Swimmer's Guide | Candidate |
| Icebreaker / serious ship progression | Earned water traversal without skipping the whole world | World/system reward |
| Deep-North maps/journals | Information itself becomes premium loot | World/system reward |

## Dread Reaches

Primary reward should be **Aquamirae's own dungeon/boss loot**, plus:

- northern regional relic / story recognition — World/system reward
- major advancement — World/system reward
- possibly one post-expedition Masterful-quality crafted piece — Curated stack, only if balance still warrants it

Do not dilute Cornelia by making the board pay out a whole second boss chest.

---

# Greenveil — East

**Reward fantasy:** agility, botany, adaptive survival, animal/pet utility, breeding, jungle traversal, naturalist knowledge.

## Hearthlands

| Reward | Why it belongs here | Status |
|---|---|---|
| Chameleon/adaptive insulation material | Greenveil teaches flexible survival that later helps North and South too | Candidate |
| **Greenhand** iron saber | Quick, practical dense-terrain weapon | Curated stack |
| Springy-quality leggings | Better jumping without trivializing terrain like flight would | Curated stack |
| Herbal/tea starter goods | Peaceful regional identity and Farmer's Respite exposure | Candidate |
| Naturalist tools | Gives non-combat quests worthwhile utility | Candidate |

## Frontier

| Reward | Why it belongs here | Status |
|---|---|---|
| Nature Rune (`majruszsaccessories:nature_rune`) | Strong farming/animal identity, but may skip an intended accessory-combination loop if awarded too cheaply | Source-checked; placement review |
| Pet poison resistance | Makes bringing companions into dangerous jungle viable | Candidate |
| Pet flute / herding utility | Animal-management progression | Candidate |
| Farmer's Respite kettle/crop starter | Makes herbalist quests mechanically useful | Candidate |
| Backpack Unlock Token / gathering augment | Dense-country expeditions benefit from storage utility | Source-checked token / Candidate augment |

## Wildlands

| Reward | Why it belongs here | Status |
|---|---|---|
| **Greenwake** diamond glaive | Signature T3 Greenveil weapon | Curated stack |
| Capuchin companion upgrade path | Earlier regional friend remains relevant later | Candidate / Native interaction |
| Advanced pet enchantments | East specializes companions instead of only the player | Candidate |
| Toxic Cave / major-ruin information | Discovery lead is the reward; native cave loot remains native | World/system reward |
| Better backpack/gathering utility | Expedition quality-of-life without raw combat inflation | Candidate |

## Dread Reaches

Primary reward should be Jungle Abomination native/adapted encounter loot once finalized, plus:

- eastern regional relic / recognition — World/system reward
- major advancement — World/system reward
- optional high-end mobility/adaptive armor piece after the arc — Curated stack

---

# Sunscar — South

**Reward fantasy:** heat survival, speed, mounts, caravans, elephants, archaeology, open-country weapons, logistics.

## Hearthlands

| Reward | Why it belongs here | Status |
|---|---|---|
| **Acacia Blossoms** (`alexsmobs:acacia_blossom`) | Benchmark reward: quest exposes the real Alex's Mobs elephant-taming mechanic | **Source-checked, high priority** |
| Saddle | Natural civilization reward; supports removal of convenience saddle recipe | Safe vanilla |
| Empty Waterskin (`cold_sweat:waterskin`) | Teaches Cold Sweat's hot-weather side without guessing filled-item metadata | Source-checked |
| Special cooled/filled Waterskin | Excellent thematic idea, but exact stack state must be captured first | Candidate / stack test |
| Heat-insulation starter material | Early preparation before severe desert | Candidate |
| Road javelin / spear | Open-country weapon identity | Curated stack |

## Frontier

| Reward | Why it belongs here | Status |
|---|---|---|
| Certificate of Taming (`majruszsaccessories:certificate_of_taming`) | Fits the animal theme, but an open Forge 1.20.1 bug report says it may not work | **Hold** |
| Ancient Scarab (`majruszsaccessories:ancient_scarab`) | Archaeology quest that makes later archaeology better | Source-checked, high priority |
| Speedy-quality boots | Open terrain makes movement speed meaningful | Curated stack |
| Supply Cart | Uses the land instead of skipping it | Candidate, high priority |
| Animal Cart | Regional livestock/passenger logistics | Candidate |
| Icebox (`cold_sweat:icebox`) / cooling infrastructure | Settlement/base progression for serious heat | Source-checked ID / World infrastructure |
| Better waterskin/cooling kit | Practical desert expedition reward | Candidate |

## Wildlands

| Reward | Why it belongs here | Status |
|---|---|---|
| **Sunspike** diamond lance | Signature T3 Sunscar weapon | Curated stack |
| Advanced caravan/cart utility | Long-range land logistics become a regional strength | Candidate |
| Backpack Unlock Token / hazard-resistant augment | Deep-desert expedition utility | Source-checked token / Candidate augment |
| Archaeology maps/tablets | Knowledge progressively reveals the Cursed Pyramid | World/system reward |
| Strong mount-related enchantment | Rewards using the region's travel identity | Candidate |

## Dread Reaches

Primary reward should be **Cursed Pyramid / Ancient Remnant native loot**, plus:

- southern regional relic / final tablet recognition — World/system reward
- major advancement — World/system reward
- possibly one ceremonial high-end weapon/armor reward after returning — Curated stack

The board should not replace the pyramid's treasure ecology.

---

# Harvestlands — West

**Reward fantasy:** farming, home utility, defensive equipment, axes/scythes, animal keeping, settlement improvement, old-country tools.

## Hearthlands

| Reward | Why it belongs here | Status |
|---|---|---|
| Saddle / stable reward | Strong fit for **The Empty Stalls** and ordinary village life | Safe vanilla |
| Tamed Potato Beetle (`majruszsaccessories:tamed_potato_beetle`) | Memorable agricultural utility instead of raw stats | Source-checked; effect test required |
| Wind Chime | Home/night utility; makes a small quest feel worthwhile | Candidate |
| **Woodman's Friend** iron battleaxe | Practical woodsman identity | Curated stack |
| Farming supplies / food | Keeps the early West genuinely habitable and lived-in | Safe vanilla |

## Frontier

| Reward | Why it belongs here | Status |
|---|---|---|
| Solid-quality shield | Defensive reward as the old woods become more dangerous | Curated stack |
| Plow | A quest reward that changes everyday settlement gameplay | Candidate, high priority |
| Household Rune | Home/civilization utility rather than another combat stat | Candidate |
| Dream Catcher | Home/night utility | Candidate |
| Pet flute / herding / collar utility | Animal-keeper progression | Candidate |
| Farming backpack augment | Region-specific work utility | Candidate |

## Wildlands

| Reward | Why it belongs here | Status |
|---|---|---|
| **Harvest Moon** diamond scythe | Signature T3 western weapon | Curated stack |
| Better farming/harvest enchantment | Deepens region identity without generic damage inflation | Candidate |
| Mansion/ruin correspondence | Information lead toward stranger western content | World/system reward |
| Backpack Unlock Token / survey utility | Long old-forest expeditions | Source-checked token / Candidate survey utility |
| Advanced defensive/pet reward | Fits dangerous woodland travel | Candidate |

## Dread Reaches

Primary reward should be Lord Pumpkinhead's own encounter loot, plus:

- western regional relic / recognition — World/system reward
- major advancement — World/system reward
- **Quiet Fields** epilogue reward should be intentionally modest/home-oriented rather than another boss-tier weapon

That last contrast is deliberate: after the nightmare is over, the reward can be that ordinary life is possible again.

---

# Cross-regional rewards worth protecting

These are valuable precisely because they remain useful after leaving the region where they were earned:

| Reward | Best first home | Later value |
|---|---|---|
| Chameleon/adaptive insulation | Greenveil | Helps Frostmarch and Sunscar |
| Elephant taming access | Sunscar | Mount/companion utility anywhere |
| Falconry/hunter utility | Frostmarch | Scouting/hunting elsewhere |
| Farming/home rewards | Harvestlands | Improve whichever base becomes home |
| Backpack Unlock Token | Any expedition tier | Universal and explicitly intended by Backpacked as a modpack reward item |
| Pet enchantments | Region-themed first source | Companion develops across the whole world |
| Maps/journals | Wherever discovered | Connect regions and destinations |

# Reward balance rules

1. **T1 should be memorable, not dominant.** A saddle, Thermometer, Acacia Blossoms, strange farm accessory, useful boots, or modest curated iron weapon can be more exciting than raw diamonds.
2. **T2 should unlock a capability.** Insulation, carts, archaeology, pet utility, better traversal, farming machinery.
3. **T3 earns signature expedition gear.** This is where White Reach, Greenwake, Sunspike, and Harvest Moon belong if exact stack testing supports them.
4. **T4 lets the destination pay.** Main bosses/dungeons should retain their own exciting native loot; the quest layer adds recognition, story relics, maps, and occasional ceremonial rewards.
5. **No random reward soup for named quests.** If a quest exists to teach elephant taming, its special reward should actually help tame an elephant.
6. **Knowledge counts as a premium reward.** A map to a legendary place can be more valuable than another ingot.
7. **Companions are valid rewards, but direct ownership transfer is special-case integration.** Prefer giving taming means/knowledge unless the quest is explicitly a one-off rescue.
8. **Legendary/Masterful qualities remain rare.** Do not spend the strongest Quality Equipment modifiers on ordinary village jobs.
