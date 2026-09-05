# Regional Quest Catalog — Implementability-First Draft

This document turns the regional brainstorm into quest skeletons that respect Bountiful 6.0.4's real capabilities.

Legend:

- **GREEN** — objective can be represented today with exact-item, item-tag, or entity-kill Bountiful logic.
- **GREEN+WORLD** — Bountiful side is simple/reliable, but the world project must provide a structure/proof/map hook.
- **YELLOW** — potentially criteria-based; do not ship until tested.
- **DEFERRED** — special runtime state/ownership/issuance required.

Reward entries labeled `VALIDATE` are design candidates whose exact installed registry ID/NBT still needs certification before implementation.

The structure IDs will be filled in after the developer thread exports the authoritative structure audit.

---

# NORTH — Frozen / Alpine

## Hearthlands

### Outfitter's Order

**Purpose:** introduce cold preparation before the North becomes punishing.

Objective options:

- bring wool — `item_tag` — **GREEN**
- bring leather — exact item — **GREEN**
- bring coal — exact item — **GREEN**

Reward candidates:

- Thermometer — `VALIDATE`
- small insulation material bundle — `VALIDATE`
- modest emeralds

No temperature-event detection is required. The quest simply makes the survival mechanic discoverable.

### Hunter's String

Objective:

- bring string — **GREEN**
- optionally kill spiders — **GREEN**

Reward candidates:

- arrows
- Quiver — `VALIDATE`
- modest food

This is intentionally mundane and useful.

### Trailwarden

Objective:

- kill an audited low-tier northern hostile — **GREEN** once entity ID is known

Reward:

- curated iron spear/pike with a modest quality — `VALIDATE NBT`

Do not implement the curated stack until exact Spartan Weaponry ID + Quality Equipment NBT are captured from the running pack.

### Lost Survey Tag

Objective:

- recover `cozycrazycraft:surveyors_tag` — exact item — **GREEN+WORLD**

World need:

- place the proof item in an appropriate low-tier northern watch/survey structure

Reward candidates:

- Tall-quality boots — `VALIDATE NBT`
- map/lead farther north — **DEFERRED MAP LAYER**

---

## Frontier

### Icebound Tack

Objective:

- recover a unique tack/ledger proof item from a northern structure — **GREEN+WORLD**

Reward:

- horse armor/enchantment using Horse Frost Walker — `VALIDATE`

No need to detect the horse actually crossing ice.

### The Insulator's Kit

Objective options:

- exact Cold Sweat insulation materials — **GREEN after registry audit**
- wool/leather combination — **GREEN**

Reward:

- Sewing Table or curated insulation equipment — `VALIDATE`

### Mountain Extermination

Objective:

- kill audited northern Frontier enemies — **GREEN**

Reward:

- hunter supplies
- backpack Unlock Token — `VALIDATE`
- better ranged weapon — `VALIDATE`

### Missing Expedition Journal

Objective:

- recover unique journal proof from a mountain structure — **GREEN+WORLD**

Reward:

- useful item + information lead toward deeper North

The journal itself may be the important reward/foreshadowing rather than raw loot.

---

## Wildlands

### Great Hunt: Frostmaw

Objective:

- kill `mowziesmobs:frostmaw` (verify exact installed ID) — **GREEN entity-type logic**

Important limitation:

- native Bountiful cannot prove it was a particular mapped Frostmaw instance

Location/map:

- future cartographer/useful-target layer

Reward candidate:

- **White Reach**, curated diamond pike — `DEFERRED AUTHORED STACK`

### Frozen-Coast Supplies

Objective:

- deliver/recover Cold Sweat/water-travel supplies — **GREEN** once IDs known

Reward candidates:

- swimmer/angler accessory — `VALIDATE`
- sea-travel item

### Lost Ice-Maze Expedition

Objective:

- recover a unique expedition record from an outer-northern structure — **GREEN+WORLD**

Reward:

- knowledge/map leading toward Aquamirae/Ice Maze — `DEFERRED MAP LAYER`

This should not hand out Aquamirae's own signature loot.

---

## Dread Reaches

Main destination:

- Aquamirae / Ice Maze / Captain Cornelia

The quest layer should mostly provide:

- final information/map/access
- regional recognition/relic
- advancement

Do not replace native Aquamirae dungeon/boss rewards with board payouts.

---

# EAST — Jungle / Lush / Overgrown

## Hearthlands

### Herbalist's Basket

Objective:

- exact regional plants/foods — **GREEN** once IDs are audited
- safe fallback can use vanilla cocoa/bamboo/melon items

Reward candidates:

- useful regional food/drink
- adaptive-insulation starter material — `VALIDATE`
- Graceful-quality saber — `VALIDATE NBT`

### Garden Pest

Objective:

- kill audited low-tier plant/jungle hostile entities — **GREEN**

Reward candidates:

- Springy-quality leggings — `VALIDATE NBT`
- gardening utility

### A Chameleon's Lesson

Objective:

- deliver a verified chameleon-related item/material — **GREEN after ID audit**

Reward:

- Chameleon Molt/adaptive-insulation supply — `VALIDATE`

This teaches adaptive survival without detecting a temperature event.

### Jungle Provisions

Objective:

- deliver food/crops — **GREEN**

Reward:

- Farmer's Respite starter item/crop/kettle component — `VALIDATE`

A peaceful village quest is desirable here.

---

## Frontier

### The Garden That Bites

Objective:

- kill stronger audited overgrowth creatures — **GREEN**

Reward candidate:

- Nature Rune — `VALIDATE`

### Venom Supplies

Objective:

- bring exact poison/antidote-related ingredients — **GREEN**

Reward candidate:

- Poison Resistance pet enchant/item — `VALIDATE`

No need to detect the player being poisoned.

### Animal Keeper

Objective options:

- bring animal supplies — **GREEN**
- tame animal — **YELLOW criteria; test first**
- breed animals — **YELLOW criteria; test first**

Reward candidates:

- pet-management item
- Certificate of Taming — `VALIDATE`
- Domestication Innovation utility — `VALIDATE`

Ship the item-delivery version first if taming criteria is not certified.

### Overgrown Records

Objective:

- recover a unique journal/reliquary from an eastern structure — **GREEN+WORLD**

Reward:

- regional map/information

---

## Wildlands

### Temple Reliquary

Objective:

- recover a unique proof item from a major jungle temple/ruin — **GREEN+WORLD**

Reward candidate:

- **Greenwake**, curated diamond glaive — `DEFERRED AUTHORED STACK`

This provides East's serious T3 reward without forcing a fake symmetrical miniboss.

### Old Dart

Objective:

- recover an archaeology/temple proof item — **GREEN+WORLD**

Reward/lead:

- capuchin/Ancient Dart progression if the exact Alex's Mobs mechanic validates

### Toxic Trail

Objective:

- recover expedition notes — **GREEN+WORLD**

Reward:

- information about Toxic Cave / major eastern destination

Knowledge is the premium reward; do not give Alex's Caves signature gear from the board.

---

## Dread Reaches

Future final destination:

- Jungle Abomination arena

Do not implement the boss extraction here.

Quest layer later supplies:

- indirect clues
- final map/location
- regional relic/recognition

---

# SOUTH — Desert / Savanna / Hot

## Hearthlands

### Flowers of the Savanna

**Priority authored concept.**

Objective options that stay simple:

- bring acacia saplings/logs/leaves-related exact items/tags — **GREEN**
- recover a botanist's unique satchel from an appropriate structure — **GREEN+WORLD**

Signature reward:

- Acacia Blossoms — `VALIDATE EXACT ID/COUNT/TAMING MECHANIC`

This is the benchmark regional reward: the quest teaches a regional ecology mechanic and then gives the player the means to engage with it.

Because Bountiful random rewards are combinatorial, do not put Acacia Blossoms in a broad Southern reward pool. This must be a narrow/authored pairing later.

### Broken Wagon

Objective:

- bring logs/leather/iron — **GREEN**

Reward:

- Saddle — **GREEN item reward**

This is one reason the convenience saddle recipe can be removed without making saddles obnoxiously rare.

### Caravan Rations

Objective:

- bring prepared food — **GREEN**

Reward:

- emeralds
- travel food
- modest utility

### Desert Vermin

Objective:

- kill audited low-tier southern hostiles — **GREEN**

Reward:

- javelins/arrows/utility — `VALIDATE modded IDs where applicable`

---

## Frontier

### The Elephant Keeper

Safe first implementation:

- exact item/material objective — **GREEN**
- Acacia Blossom / Certificate of Taming reward — `VALIDATE`

Optional later criterion version:

- tame an animal — **YELLOW until tested**

Do not require a bespoke elephant-rescue event merely to make this quest feel special.

### The First Dig

Objective:

- recover a unique archaeology proof item from a desert ruin — **GREEN+WORLD**

Reward candidate:

- Ancient Scarab — `VALIDATE`

This is another benchmark reward: archaeology work unlocks better archaeology interaction.

### The Long Road

Objective:

- recover a courier parcel/proof item from a remote structure — **GREEN+WORLD**

Reward candidate:

- Speedy-quality boots — `VALIDATE NBT`

### Caravan Equipment

Objective:

- supply wood/leather/iron or recover wagon components — **GREEN**

Reward candidate:

- Supply Cart / Animal Cart access — `VALIDATE AstikorCarts award flow`

Land transport is preferred because it engages with geography instead of skipping it.

### Heat Preparation

Objective:

- gather exact Cold Sweat cooling materials — **GREEN after ID audit**

Reward:

- Waterskin/cooling utility — `VALIDATE`

No need to detect that the player reached a particular body temperature.

---

## Wildlands

### Great Hunt: Umvuthi

Objective:

- kill exact Umvuthi entity type — **GREEN once ID verified**

Reward candidate:

- **Sunspike**, curated diamond lance — `DEFERRED AUTHORED STACK`

### Deep Archaeology

Objective:

- recover a unique inscription/journal from a major desert structure — **GREEN+WORLD**

Reward:

- information leading toward the Cursed Pyramid

### Imbued Expedition Pack

Objective:

- supply/recover regional expedition materials — **GREEN**

Reward candidate:

- Backpacked hazard/Imbued-style augment — `VALIDATE exact current augment`

---

## Dread Reaches

Future final destination:

- isolated Cursed Pyramid package / Ancient Remnant

The quest system later provides:

- archaeology chain culmination
- final map/location
- regional relic/recognition

Native dungeon loot remains important.

---

# WEST — Autumn / Redwood / Old Forest

## Hearthlands

### Restless Stables

**Priority first structure-adventure concept.**

Objective:

- recover `cozycrazycraft:stablemasters_seal` — **GREEN+WORLD**

Target:

- Dungeons Enhanced Stable or another approved Hearthlands stable after structure audit

Reward candidates:

- Saddle — **GREEN**
- modest curated iron weapon — `VALIDATE NBT`
- emeralds

This is an excellent tutorial for:

```text
board → map/local knowledge → structure → proof item → return
```

### A Pest Worth Keeping

Objective:

- ordinary crop/material delivery — **GREEN**

Signature reward candidate:

- Tamed Potato Beetle accessory — `VALIDATE`

Keep this as a narrow/authored reward pairing later, not random broad-pool loot.

### Woodman's Order

Objective:

- logs — `item_tag` — **GREEN**

Reward candidate:

- Broad-quality battleaxe — `VALIDATE NBT`

### Night Supplies

Objective:

- string/coal/wood or common night-hostile kills — **GREEN**

Reward candidate:

- Wind Chime — `VALIDATE`

---

## Frontier

### Gravekeeper's Bell

Objective:

- recover `cozycrazycraft:gravekeepers_bell` from an approved western graveyard structure — **GREEN+WORLD**

Reward candidate:

- Solid-quality shield — `VALIDATE NBT`
- undead-appropriate weapon — `VALIDATE`

### Harvest Order

Objective:

- seasonal/farm item delivery — **GREEN**

Reward candidates:

- farming accessory/augment — `VALIDATE`
- useful food

Do not require detecting Serene Seasons calendar state for the first implementation. The regional pool/board can supply the flavor without a bespoke seasonal event engine.

### Old Plow

Objective:

- supply/recover farming materials — **GREEN**

Reward candidate:

- AstikorCarts Plow — `VALIDATE`

### Animal Keeper

Objective:

- pet/farm supplies — **GREEN**

Reward candidates:

- pet bed/collar/flute/training item — `VALIDATE`

Optional tame/breed criteria remain YELLOW until certified.

---

## Wildlands

### Great Hunt: Sir Pumpkinhead

Objective:

- kill Sir Pumpkinhead entity type — **GREEN once exact ID verified**

Reward candidate:

- **Harvest Moon**, curated diamond scythe — `DEFERRED AUTHORED STACK`

### Sealed Illager Orders

Objective:

- recover `cozycrazycraft:sealed_illager_orders` from an approved mansion/Illager destination — **GREEN+WORLD**

Reward:

- utility + information pointing toward deeper western country

### Old Grave Expedition

Objective:

- recover proof from a Born in Chaos/other approved western structure — **GREEN+WORLD**

Reward:

- regional knowledge / useful defensive item

---

## Dread Reaches

Final destination:

- Lord Pumpkinhead encounter

Quest layer later provides:

- accumulated clues
- deliberate final lead/location
- regional relic/recognition

Possible post-boss epilogue design label:

- `Quiet Fields`

If implemented, keep the actual objective simple (e.g. bring seeds/food/building materials) rather than inventing a complex "restore the whole farm" detector.

---

# Cross-regional contracts that are mechanically healthy

## Courier supplies

- exact item delivery — GREEN
- rewards backpack expansion/travel utility after validation

## Cooking/provisions

- exact crafted-food delivery — GREEN
- good use of Farmer's Delight/Respite without needing custom events

## Local mob control

- entity kills — GREEN
- pool changes by region/tier later

## Hunter materials

- exact item or conservative item tags — GREEN

## Proof-item recovery

- Bountiful side GREEN
- world insertion deferred

## Great Hunts

- entity-type objective GREEN
- map/instance specificity deferred

---

# Quest ideas intentionally kept out of the initial implementation

These remain design concepts, not current promises:

- tornado survival
- scripted caravan escort
- photograph-specific-target verification
- arbitrary structure restoration detection
- exact boss-instance kill enforcement
- dynamic named-pet ownership transfer
- timed NPC defense events

They can be revisited if a future companion runtime makes them cheap and reliable. They are not necessary to make the quest system feel authored.
