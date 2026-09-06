# Named Places + Local Knowledge Model

Status: architecture authority for the authored villager quest layer.

## Core rule

CozyCrazyCraft already gives villages and discoverable structures persistent world-global names through CozyCrazyZones. Authored quests must reuse those identities.

A quest should say **Pumpkin Hollow**, **The Amber Watch**, **Redleaf Keep**, etc. whenever the issuing player/NPC knows that identity. It should not regress to generic phrases like `the issuing village`, `watch tower`, or `nearby structure` after a named place is known.

Quest state therefore carries both identity and location:

- issuing settlement board/dimension
- issuing settlement persistent name
- exact target structure ID
- exact frozen target position
- target persistent place-name
- how the player learned the target
- current knowledge state

The name is presentation; the frozen structure ID + position remain the authoritative objective identity.

## Knowledge states

The intended information model is Daggerfall-inspired without random NPC interrogation grind.

### UNKNOWN

The player has no usable lead. No Atlas marker.

### RUMOR

The player knows a broad fact such as direction, road, biome, or local problem, but not the exact destination. This may be enough to begin searching.

Example: `Hunters coming down the western road keep finding frost-bitten carcasses.`

### LEAD

A specific person or clue can resolve the location.

Example: a farmer says the fletcher saw where the raiders came from. The quest points the player to **one sensible profession/person**, not a random-villager spam loop.

### KNOWN

The source genuinely knows the destination. Its persistent named-place marker may be placed on the player's Atlas immediately.

Example: a cartographer already has **The Amber Watch** in local records and marks it for a survey commission.

### CONFIRMED

The player personally reaches/discovers the target. CozyCrazyZones discovery behavior may still fire even if an NPC revealed the marker earlier; `known` and `personally discovered` are intentionally distinct facts.

## Who knows what

Quest knowledge comes from role + local world facts rather than global omniscience.

- **Cartographer:** mapped roads, known landmarks, villages, broad regional leads; primarily a map merchant, not the universal quest funnel.
- **Farmer / Shepherd:** fields, nearby animal populations, crop damage, local trails and ruins used by villagers.
- **Fletcher / Hunter / Guard:** hostile ecology, tracks, camps, towers, road danger and combat incidents.
- **Cleric:** graves, curses, undead, shrines, relics and ominous local reports.
- **Mason / Armorer / Weaponsmith:** mines, ruins, fortifications, material sites, weapon/armor commissions and serious threats.
- **Fisherman:** rivers, coasts, wrecks, aquatic ecology and shoreline structures.
- **Librarian:** records, older place-names, written clues and cross-village knowledge.
- **Named/story NPCs:** exceptional knowledge justified by their role in a regional chain.

An NPC only offers a structure/species quest when the runtime can justify that knowledge from actual nearby world content, remembered village facts, prior quest flags, or an explicit story fact.

## Atlas rule

When a quest source knows an exact place, the quest layer should call CozyCrazyZones' existing Atlas discovery-marker ledger rather than inventing a second waypoint system.

This has several advantages:

1. The quest marker uses the same persistent place-name as normal exploration.
2. The same target cannot acquire competing map identities.
3. A marker may be queued even if the player is not currently holding the Atlas.
4. Later physical discovery can still produce the normal discovery/stinger behavior.
5. Removing/finishing a quest does not erase the geographical knowledge the player legitimately learned.

A quest marker is therefore **knowledge gained**, not a temporary HUD GPS arrow.

## First implementation: The First Real Map

The first 0.3.0 Cartographer proof uses `KNOWN` semantics:

- the cartographer resolves a real nearby legal structure;
- CozyCrazyZones assigns/reuses that structure's persistent name;
- CozyCrazyZones assigns/reuses the issuing village's persistent name;
- accepting the commission queues the named structure into the Atlas marker ledger;
- the Village Contract records both names plus approximate direction/distance;
- the exact structure position is frozen for objective completion;
- arriving at that structure marks the survey complete;
- turn-in must occur back in the named issuing settlement.

## Future handoff pattern

A multi-person quest should use deliberate information handoffs, not arbitrary fetch dialogue.

Good example:

`Pumpkin Hollow farmer -> local problem -> fletcher who saw tracks -> named camp becomes KNOWN -> Atlas marker -> investigate camp -> return to farmer/guard.`

Bad example:

`Ask random villagers until one passes a hidden RNG check.`

The information chain itself should reveal character, profession, settlement history or the nature of the problem. If an intermediary adds no information or choice, skip the intermediary.

## Naming invariants

- Never generate a quest-only alternate name for a structure already named by CozyCrazyZones.
- Never regenerate a target name after acceptance.
- Never use a generic structure label when the stored named-place identity is available.
- Never make the Atlas label and contract label disagree.
- Never identify a place more precisely than the quest's current knowledge state permits.
- Village names are part of quest identity and should appear in contracts, return instructions and regional story text whenever known.
