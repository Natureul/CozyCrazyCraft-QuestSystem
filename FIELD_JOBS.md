# CozyCrazyCraft Regional Field Jobs

These are the **repeatable/local ecology layer**, separate from the 108 authored quest concepts in [`QUESTS.md`](QUESTS.md).

Current expansion: **16 field-job concepts — one new job in every cardinal region × radial tier cell.**

They are intentionally simple under the hood: ordinary Bountiful item deliveries or exact entity kills. Their job is to make a Frostmarch board feel different from a Greenveil board even when no major story contract is active.

> A field job is regional flavor, not necessarily an exclusive spawn claim. Some creatures such as grizzlies or Dread Hounds are legal in more than one place; a local posting can still make sense where that creature is genuinely present.

## At a glance

| Region | Hearthlands | Frontier | Wildlands | Dread Reaches |
|---|---|---|---|---|
| **Frostmarch / North** | Leather for the Snowline | Cats Above the Pass | Bears Beyond the Pines | Lanterns for Black Ice |
| **Greenveil / East** | Moss for the Nursery | Mouths in the Ferns | The Carniflore Line | The Matriarch in the Mangroves |
| **Sunscar / South** | Cactus for the Packers | Rattles in the Brush | Quakes on the Red Road | Guides in the Dust |
| **Harvestwood / West** | Apples for Pressing | Bears at the Orchard | Hounds on the Old Road | Bones at the Churchyard |

---

# Frostmarch — North

### Hearthlands — Leather for the Snowline
**Village Outfitter · Local Notice**

Boots, straps, and pack harness wear out quickly once the roads start freezing. The outfitter is buying sound leather before the next trail party leaves.

- Objective: bring leather
- Mechanical type: exact item
- Play role: ordinary cold-country preparation

### Frontier — Cats Above the Pass
**Mountain Watch · Hunt Notice**

Snow leopards have been stalking the high trail where pack animals cannot see them coming.

- Objective: kill `alexsmobs:snow_leopard`
- Mechanical type: exact entity kill
- CZ 0.3.6 check: Frostmarch Frontier+

### Wildlands — Bears Beyond the Pines
**Expedition Quartermaster · Hunt Notice**

A supply cache was torn open twice in one week. The tracks belong to grizzlies large enough that the quartermaster has stopped sending cooks to chase them off.

- Objective: kill `alexsmobs:grizzly_bear`
- Mechanical type: exact entity kill
- Note: grizzlies can also occur in Harvestwood; this notice does not claim exclusivity

### Dread Reaches — Lanterns for Black Ice
**Frozen-Coast Quartermaster · Supply Contract**

The last shelters before the black water are burning fuel faster than expected.

- Objective: bring coal
- Mechanical type: exact item
- Play role: severe expedition logistics without making every Dread posting another boss hunt

---

# Greenveil — East

### Hearthlands — Moss for the Nursery
**Village Naturalist · Naturalist Notice**

The nursery is trying to keep young shade plants alive through a dry spell. Fresh moss will hold the damp better than another bucket carried by hand.

- Objective: bring moss blocks
- Mechanical type: exact item

### Frontier — Mouths in the Ferns
**Trail Warden · Hunt Notice**

Foliaaths have settled too close to a path used by gatherers.

- Objective: kill `mowziesmobs:foliaath`
- Mechanical type: exact entity kill
- CZ 0.3.6 check: Greenveil Frontier+

### Wildlands — The Carniflore Line
**Deep-Green Watch · Hunt Notice**

Carniflores are appearing along the route between two camps. Nobody is asking you to clear the jungle—only to make one narrow road survivable again.

- Objective: kill `skarrier_mobs:carniflore`
- Mechanical type: exact entity kill
- CZ 0.3.6 check: Greenveil Wildlands+

### Dread Reaches — The Matriarch in the Mangroves
**Blackwater Watch · Hunt Notice**

A Slither Matriarch has claimed the flooded route and smaller creatures have gathered around it.

- Objective: kill `skarrier_mobs:slither_matriarch`
- Mechanical type: exact entity kill
- CZ 0.3.6 check: deep Greenveil threat

---

# Sunscar — South

### Hearthlands — Cactus for the Packers
**Caravan Tanner · Local Notice**

The caravan crews use cactus where greener settlements would waste cloth and timber. The tanner is buying clean cuttings for the next batch of travel gear.

- Objective: bring cactus
- Mechanical type: exact item

### Frontier — Rattles in the Brush
**Road Watch · Hunt Notice**

Rattlesnakes have moved into the scrub around a busy watering place.

- Objective: kill `alexsmobs:rattlesnake`
- Mechanical type: exact entity kill
- CZ 0.3.6 check: Sunscar Frontier+

### Wildlands — Quakes on the Red Road
**Badlands Scout · Hunt Notice**

Quakes have started crossing the old red road in daylight. The scout wants the route reopened before another caravan decides the detour is safer.

- Objective: kill `skarrier_mobs:quake`
- Mechanical type: exact entity kill
- Play role: uses a region-appropriate daytime threat without inventing a special event

### Dread Reaches — Guides in the Dust
**Pyramid-Road Watch · Hunt Notice**

Spirit Guides have been drifting onto the route after sunset, drawing travelers away from the markers.

- Objective: kill `born_in_chaos_v1:spirit_guide`
- Mechanical type: exact entity kill
- Note: the creature begins earlier in Sunscar; this is a deeper-country posting, not an exclusive Dread spawn claim

---

# Harvestwood — West

### Hearthlands — Apples for Pressing
**Orchard Keeper · Farm Notice**

The early wind knocked half the orchard before the pickers were ready. Good apples are still being bought for the village presses and autumn tables.

- Objective: bring apples
- Mechanical type: exact item
- Play role: reinforces that early Harvestwood is pleasant, lived-in country rather than immediate Halloween horror

### Frontier — Bears at the Orchard
**Orchard Keeper · Hunt Notice**

Grizzlies have learned where the fallen fruit piles up. The keeper can tolerate a missing basket; a bear in the workers' path is another matter.

- Objective: kill `alexsmobs:grizzly_bear`
- Mechanical type: exact entity kill
- Note: grizzlies are shared with Frostmarch

### Wildlands — Hounds on the Old Road
**Deep-West Watch · Hunt Notice**

Dread Hounds have started following travelers between abandoned farms. Clear enough of the pack that the watch can use the road again.

- Objective: kill `born_in_chaos_v1:dread_hound`
- Mechanical type: exact entity kill
- Note: Dread Hounds are a general outward threat; the posting is western because the local fiction fits old roads and abandoned farms

### Dread Reaches — Bones at the Churchyard
**Last-Road Watch · Hunt Notice**

A Supreme Bonescaller has been seen beyond the old graves. Nobody is asking for the churchyard back—only for the thing raising the dead around it to stop.

- Objective: kill `born_in_chaos_v1:supreme_bonescaller`
- Mechanical type: exact entity kill
- CZ 0.3.6 check: Dread Reaches-level threat

---

## Current Bountiful playtest decrees

The first implementation pass is deliberately simple. Each regional decree currently contains these four objectives together so we can test the actual entity/item tracking and regional feel before writing automatic board assignment:

```text
/bo decree ccc_field_north
/bo decree ccc_field_east
/bo decree ccc_field_south
/bo decree ccc_field_west
```

These are **playtest decrees**, not the final production tier filter. A Hearthlands board should not eventually offer its region's Dread job. Production assignment will use the board's `CozyZonesApi.regionalCellAt(...)` classification to select the correct region/tier content.

## Design target for a normal board

A healthy board should eventually show roughly:

- 2–3 mundane/local postings
- 1–2 region-specific field jobs
- 0–1 structure/recovery contract if a sensible target exists
- 0–1 signature/story posting only when actually unlocked

The point is that **ordinary life continues at every tier**. A Dread Reaches settlement can still need food, fuel, timber, or help with one road; it just lives beside much worse things.
