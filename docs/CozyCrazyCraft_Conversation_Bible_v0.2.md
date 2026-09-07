# COZYCRAZYCRAFT CONVERSATION BIBLE v0.2

**Developer authority for Conversations-mod villager speech, NPC voice, knowledge-state wording, referrals, Atlas knowledge, Village Trust, local memory, and anti-repeat behavior.**

Version 0.2 • September 7, 2026

This document supersedes Conversation Bible v0.1 for 0.4.1 conversation-writing work. It remains companion authority to the Progression, Routing & Dialogue Bible and Villager Name Bible. Quest/reward authorities still decide what work exists and what rewards are legal. This Bible decides **who can plausibly say what, how certain they sound, what conversational surface carries the information, and how repeated interactions remain human rather than mechanical**.

The non-negotiable target remains: **villagers are inhabitants first, quest-givers second.** A useful line should sound like something this particular person in this particular settlement would say even if the reward UI vanished.

---

## 0. Authority and hard rules

1. **Conversations is the NPC voice surface.** Explanations, directions, uncertainty, referrals, warnings, map knowledge, quest clarification, local history, and acknowledgement belong in Conversations.
2. **Action bar is confirmation, not prose.** It may say `Lead noted.`, `Atlas marked.`, `Survey complete: Amber Rest.`, or `Stable Ledger recovered.` It must not contain a villager's paragraph, route explanation, or social referral.
3. **Chat is durable system state only when persistence is genuinely useful.** It is not a second mouth for the NPC.
4. **Knowledge controls wording.** UNKNOWN, RUMOR, LEAD, KNOWN, and CONFIRMED are not five labels for the same answer. They change certainty, naming, precision, available follow-ups, and Atlas behavior.
5. **NPC knowledge is per subject.** A mason may know a ruin and know nothing about the animal inside it. A cartographer may know the road and not the shrine's history.
6. **Local-first information wins.** Useful same-village and same-zone facts are common. Cross-world omniscience is not.
7. **A shared semantic clue must not create a shared voice.** The fact is authoritative; the expression is speaker-specific.
8. **Completed problems stop being current rumors.** Once the player has confirmed or resolved a local fact, hints change to acknowledgement, consequence, or outward routing.
9. **Names are persistent social facts.** Use the persistent NPC name supplied by the naming system; never reroll identity because profession, Trust, chapter, or quest state changes.
10. **Short beats are a mechanical requirement.** The typewriter UI makes prose length a gameplay cost.
11. **Do not invent precision.** If the runtime cannot support a state-aware fact, write the line conservatively or refer the player to someone who can know.
12. **No feature-gated fiction becomes an unfinishable commission.** Foreshadow disabled future content only as rumor/lore until the encounter is enabled.

---

## 1. UI surface hierarchy

### 1.1 Conversations — speech and social meaning

Use Conversations for why the villager cares; what they personally saw, heard, measured, read, repaired, tracked, caught, treated, or were told; confidence and uncertainty; directions and route advice; referrals; danger assessment; what the player should look for; map-marker justification; completed-job acknowledgement; trust-sensitive disclosures; short local history; and player questions such as `Where exactly?`, `Who saw it?`, and `Can you mark it?`.

**Rule:** if information is presented as the villager's knowledge, the player should receive it while still in the villager conversation whenever technically possible.

### 1.2 Action bar — tiny transient confirmations

Good: `Lead noted.`, `Atlas marked.`, `Survey complete: {TARGET}.`, `{PROOF_ITEM} recovered.`, `Village Trust increased.`

Bad: `Ask Mara the mason. She's about 84 blocks northwest beside the forge.` or any route explanation whose meaning is supposed to be spoken by the NPC.

**Hard budget:** target 2–6 words; 45 characters is a useful practical ceiling. A named-place confirmation may exceed that slightly only when the proper name itself is long.

### 1.3 Chat — durable system notices

Use chat only where the player may reasonably need to review the state later and no dedicated journal exists. Do not mirror ordinary conversation content into chat.

### 1.4 One semantic exchange, one primary surface

Avoid: vague speech → button closes → action bar gives the actual clue → chat repeats it. Prefer: NPC explains in short beats → player asks follow-up → final button performs state change → tiny `Lead noted.` / `Atlas marked.` confirmation if needed.

---

## 2. Typewriter writing standard

| Beat type | Preferred | Hard guidance |
| --- | ---: | ---: |
| greeting / reaction | 3–14 words | rarely over 18 |
| normal information | 5–22 words | split before 30 |
| warning | 5–18 words | one danger idea per beat |
| directions | 7–24 words | route first, qualifier second |
| Atlas reveal | 4–16 words | name + marking intent |
| lore / old record | 12–30 words | rare; split over 35 |
| player reply | 2–10 words | normally under 12 |

Prefer two short pages to one long page. Put useful nouns early. One beat carries one main claim. Avoid interface language such as `objective`, `quest state`, `target`, and `knowledge level` in spoken dialogue. Hesitation must communicate uncertainty rather than fill space.

---

## 3. The speaker-context vector

Every authored selection should be explainable as a composition of:

`region × radial tier × profession/civic role × personality × Village Trust × local fact × knowledge state × provenance × recent history × player familiarity`

Not every line must visibly mention all ten dimensions. The point is that **no dimension is allowed to be erased by a giant generic pool**.

### 3.1 Region

- Inner Hearthlands: roads, fields, creeks, mills, orchards, bridges, ordinary woods; regional labels are uncommon.
- Frostmarch: pass, drift, treeline, frozen river, shelter, ridge, visibility; no cartoon Viking register.
- Greenveil: canopy, wet trail, river bend, roots, bank, floodwater, bamboo, mangrove; no caricatured mysticism.
- Sunscar: shade, water, road, mesa, oasis, cliff, caravan, heat; no fake merchant accent.
- Harvestwood: orchard, old road, hedgerow, pasture, fog, timber, hollow, woodline; Zone 1 is not Halloween-land.

### 3.2 Radial tier

Tier changes stakes and lived vocabulary, not intelligence. Hearthlands is local routine and familiar landmarks. Frontier emphasizes route planning and preparation. Wildlands uses experienced warnings, compound threats, specialist evidence, and regional patterns. Dread Reaches has sparse certainty, old records, last reliable routes, and earned legendary names. A Hearthlands farmer should not casually narrate Dread boss doctrine; a Dread farmer may still complain about a fence.

### 3.3 Profession / civic role

Profession determines **evidence and metaphor**, not merely noun substitution.

- Farmer: fields, animal routines, fences, irrigation, soil, weather, losses.
- Mason: stone courses, foundations, cracks, mortar, age, structural reuse.
- Cartographer: routes, bearings, map age, crossings, named places, approaches.
- Librarian: records, names, contradictions, provenance, old accounts.
- Cleric: shrines, graves, undead, ritual history, feared objects; not universal mysticism.
- Fletcher: tracks, sightlines, nests, ranged threats, fresh/old sign.
- Fisherman: water level, current, cut nets, wrecks, crossings, aquatic threats.
- Toolsmith: mines, access, cave work, samples, tool damage.
- Weaponsmith: weapon marks, combat roles, enemy arms, field tests.
- Armorer: damage patterns, survivability, shields, protection needs.
- Leatherworker: tack, straps, hides, insulation, travel wear.
- Shepherd: flock behavior, pasture routes, herd movement, weather exposure.
- Butcher: livestock condition, carcass evidence, food stores, rations.
- Guard / roadwarden: patrols, attacks, casualties, entrances, defensible routes.
- Steward / civic contact: village-wide needs, responsibility, referrals, chapter-level requests.
- Unemployed / citizen: household observation, gossip, missing people, lived routine; less specialist certainty.
- Nitwit: odd observations and rumors may be useful, but never make the role the optimal mandatory knowledge farm.

### 3.4 Personality

Personality changes syntax, emotional distance, and what the speaker foregrounds. It may never alter the canonical fact. Recommended stable palette: warm, terse, cautious, practical, curious, scholarly, weary, proud, suspicious, dry, kind, nervous, veteran, young.

One LEAD fact can sound like: cautious `I can get you near it. I won't pretend I know the door.`; terse `West road. Past the orchard. Look for broken stone.`; curious `If you find the entrance, note what grows around it. No one agrees.`; veteran `Use the west approach. Better sightlines, fewer blind corners.`

### 3.5 Village Trust

Use backend bands `STRANGER / RECOGNIZED / RELIABLE / TRUSTED / PROVEN` where available. STRANGER gets public facts and polite distance. RECOGNIZED remembers useful work and makes named referrals. RELIABLE volunteers stronger context. TRUSTED shares sensitive local stakes and exact known routes. PROVEN treats the player as a credible regional actor without worship. Trust affects **candor and access**, not truth.

### 3.6 Local fact

A local fact is what makes this village different now: damaged road, missing livestock, surveyed ruin, recent raid, strange tracks, changed river, recovered ledger, cleared tower, dead resident, reopened route. Open facts create concern. Resolved facts create acknowledgement and consequence. Bias dialogue toward a small active set rather than a world-sized random pool.

### 3.7 Knowledge provenance

The source of information should be audible when it changes confidence: `local_observation`, `profession_evidence`, `village_report`, `map_record`, `old_record`, `player_report`, `world_event`, `rumor_network`, `quest_proof`.

Examples: `I saw it from the upper field yesterday.` / `The lower stones are older than the road laid over them.` / `Mara came back talking about lights by the creek.` / `It's on the older chart, not the new one.` / `You were there. I'll take your word over the old copy.`

### 3.8 Recent dialogue history

Selection should remember a small recent set so repeated clicks do not produce the same wording or the same semantic clue from every resident. Stable personality is not one stable sentence forever.

---

## 4. Knowledge-state dialogue contract

Knowledge is per subject and shared facts do not imply shared confidence.

| State | Speaker knows | Proper name | Direction | Atlas | Required sound |
| --- | --- | --- | --- | --- | --- |
| UNKNOWN | no useful fact | no, unless asking about the name itself | none fabricated | no | admits limit; refers |
| RUMOR | hearsay / uncertain sighting | usually descriptive; rumored name may be quoted as rumor | broad area/bearing only | no precise pin | hedged, sourced |
| LEAD | navigable clue / credible approach | intermediate known names allowed | route + landmark / search approach | usually no exact target pin | useful but bounded |
| KNOWN | identifies actual subject/place | yes | direct known route/approach | **yes when speaker plausibly knows it** | confident on known facts, bounded on unknowns |
| CONFIRMED | player personally verified/discovered/resolved | yes | no redundant reveal needed | already known | acknowledgement + consequence |

### 4.1 UNKNOWN

UNKNOWN is an information boundary, not a weak rumor. `No. I don't know that place.` / `Ask the mason. Old stone is their business.` Never pair `I don't know` with a fabricated exact bearing or marker.

### 4.2 RUMOR

RUMOR exposes uncertainty and preferably provenance. `Two traders mentioned ruins beyond the west creek. Neither agreed on the distance.` Do not attach a precise target pin.

### 4.3 LEAD

LEAD answers **how to search**, not necessarily an exact coordinate. `Take the orchard road west. Past the stone culvert, watch the north bank.` For underground work, describe an honest surface approach, entrance clue, mine route, ravine, shaft, or edge; never tell the player to dig straight down because an X/Z center exists.

### 4.4 KNOWN

KNOWN authorizes confident naming and direct Atlas marking when fictionally justified. `That's Redleaf Watch. I surveyed the road last spring.` / `I know it. Give me your atlas.` / `Here. I've marked the north approach, not the cellar.` Cartographers are common markers, not exclusive markers.

### 4.5 CONFIRMED

CONFIRMED means the player has firsthand knowledge. Stop selling the discovery back. `So you found Redleaf Watch.` / `Since you cleared the road, the timber carts are using it again.` Never fall back to `People say there might be a tower...` after confirmation.

---

## 5. Follow-up question contract

These are semantic branches, not cosmetic buttons.

### `Where exactly?`
UNKNOWN cannot answer and refers. RUMOR gives broad area. LEAD gives road + landmark + search instruction. KNOWN gives exact named approach and may offer marking. CONFIRMED discusses changed route/alternate entrance rather than discovery basics.

### `Can you mark it?`
UNKNOWN: `No. I won't put a guess in your atlas.` RUMOR normally cannot. LEAD may mark only an intermediate known landmark. KNOWN may mark when the speaker plausibly knows the place or holds a reliable map/record. CONFIRMED avoids redundant reveal and may only offer a semantically new safer approach.

### `Who saw it?`
Expose provenance. Prefer persistent names when the speaker knows them: `Mara Vale saw the broken cart.` If not: `A caravan out of the next village reported it. I don't know the driver.`

### `How dangerous is it?`
Answer through evidence, not a hidden difficulty number. Armorer discusses damage/shields; fletcher sightlines/ranged threat; farmer missing animals; guard casualties/patrols; cartographer terrain exposure; librarian admits stale records.

### `Who else would know?`
Give a persistent name when plausible, otherwise profession + landmark context. `Ask Mara Vale, the mason by the south kiln.` Never require bare-name memory.

### `What am I looking for?`
Give a cue the speaker could know: mason `dark foundation blocks under newer cobble`; farmer `the fence line stops where the old road starts`; fletcher `three-toed tracks crossing the mud`; cartographer `a split in the ridge where the old road bends`.

### `Which road should I take?`
Differentiate shortest, safest, and known routes when relevant. If the speaker knows only one, say so.

---

## 6. Same clue, different human voice

**Canonical fact:** `Old ruin west of orchard; masonry is pre-village; road remains usable; occupation unknown.`

- Farmer: `The west orchard road still runs. You'll see old stone where the fence gives out.`
- Mason: `Those footings predate this village. West road reaches them; I can't tell you what's living there.`
- Cartographer: `The old chart calls it Redleaf Watch. The west road still reaches the approach.`
- Fletcher: `Fresh tracks cross the west road near the ruin. Whatever's there is using the orchard edge.`
- Librarian: `The name survives in one ledger. The road is newer than the place.`
- Guard: `Patrols stop at the orchard boundary. Beyond that, you're outside our regular watch.`

Canonical facts may be centralized. Sentences should not be. When authoring a variant: identify what the speaker knows, provenance, profession attention, confidence, Trust, local vocabulary, what the player already confirmed, and recent wording to avoid. If two variants differ only by swapping a profession noun, they are not variants.

---

## 7. Anti-repeat doctrine

0.4.0 is not fixed by adding a thousand greetings. Control exact repetition, near repetition, and semantic repetition.

Recommended runtime windows: per NPC×player last 3 ambient variant/line IDs; per village×player last 5 semantic hint families; per subject the highest knowledge state already communicated; completion acknowledgements strongly down-weighted after 2–3 exposures unless a new consequence appears.

Selection priority: newly changed local fact → newly completed fact acknowledgement → unresolved fact not recently mentioned → useful referral not recently given → region/tier lived context → profession ambient → generic greeting.

Authored-resource rules: no identical NPC beat across audited ordinary/hint resources unless whitelisted; no near-duplicate beat above roughly 0.92 normalized similarity for lines longer than 45 characters; do not turn one distinctive phrase into an unrelated-NPC catchphrase. Standard player replies such as `Show me your trades.` may repeat.

---

## 8. Village identity, names, and family continuity

The Villager Name Bible is binding social context, not cosmetic garnish.

- Resolve the existing persistent NPC name; never reroll on profession, cure, Trust, chapter, chunk reload, or quest completion.
- Same-village family continuity should use real surname/household data. Nearby relatives may share surname or household identity; do not fabricate kinship solely from matching surnames.
- Referrals may use family relations when real data supports them: `My cousin Mara at the north pens.`
- Old local families may connect to farms, roads, graves, records, or local stories only when authored/derived from actual history.
- STRANGER speech can favor full name/title/profession context. RECOGNIZED/RELIABLE may use first names. TRUSTED/PROVEN may use allowed familiar forms when personality supports it. Canonical identity never changes.
- UNKNOWN/RUMOR about a person may say `a hunter from the next village`; LEAD/KNOWN may use the actual persistent name if the speaker knows them.
- Never output `Farmer #3` once a persistent name exists.

---

## 9. Completion memory and local aftermath

A completed problem becomes a new fact, not silence.

For a cleared road: farmer `The grain cart made it through this morning.`; mason `We're finally hauling stone over that road again.`; guard `Patrol's using the west bend again.`; cartographer `I've removed the closure mark from the west road.`

A second quest to the same structure must explain why new work exists. Bad: survey stables → immediately `Go back and find a ledger.` Good: `Your survey settled where the old stalls are.` → `The mason noticed a sealed tack room in your notes.` → `If the ledger survived, it would be in there.` The follow-up has causal continuity and a different reward identity.

Immediate consequences should be common shortly after completion, then decay into concise history unless the event permanently changed the village.

---

## 10. Region × tier voice guidance

Inner Hearthlands / Hearthlands stays ordinary: `Creek's high today. Use the mill bridge.` / `There's old stone beyond the pasture. The mason knows more.`

Frostmarch: Hearthlands `The river froze clean last night. Stay near the bank anyway.`; Frontier `Past the treeline, wind matters more than distance.`; Wildlands `We stopped maintaining that pass. Anyone going farther carries shelter.`; Dread `The old ice chart has one route I still trust. One.`

Greenveil: Hearthlands `Rain put the low path under water again.`; Frontier `Keep the river on your left. The trail disappears under roots.`; Wildlands `We mark routes by crossings now. Paths don't last.`; Dread `Past that bend, even the old survey notes stop agreeing.`

Sunscar: Hearthlands `Cross the open road early. Shade disappears by noon.`; Frontier `The caravan road is longer, but it reaches water.`; Wildlands `Out there, a good well is a landmark and a plan.`; Dread `The old route assumes two wells. One is gone.`

Harvestwood: Hearthlands `Bears are into the lower orchard again.`; Frontier `Fog hides the old road markers before sunrise.`; Wildlands `The strange reports all start beyond the same abandoned lane.`; Dread `People still use the old names. They just don't use the roads.`

---

## 11. Profession voice cards

| Role | Lead with | Avoid |
| --- | --- | --- |
| Farmer | routine, crops, fences, animals, weather | ancient-history certainty |
| Mason | material, age, stability, construction | monster behavior certainty |
| Cartographer | route, named geography, map confidence | unsurveyed interiors |
| Librarian | records, names, contradictions | treating old text as current field report |
| Cleric | shrines, graves, undead, feared relics | calling every strange thing a curse |
| Fletcher | tracks, sightlines, movement, ranged risk | deep historical exposition |
| Fisherman | current, water level, nets, wrecks | inland route certainty |
| Toolsmith | access, shaft, sample, tool wear | spiritual claims |
| Weaponsmith | arms, enemy equipment, field use | harmless ecology as kill counters |
| Armorer | wounds to gear, shields, protection | route omniscience |
| Leatherworker | tack, straps, hide, insulation | ancient ruin interpretation |
| Shepherd | flock behavior, pasture, movement | dungeon architecture |
| Butcher | carcasses, stores, rations, predators | grand lore |
| Guard | patrol, casualty, defense line | hidden-record scholarship |
| Steward | civic consequence, responsibility, trust | unevidenced specialist claims |
| Citizen | lived observation, household gossip | unjustified precision |

---

## 12. Provenance-aware phrasing matrix

For `something large is using the north ridge`: local observation `I saw it cross the ridge at dusk. Too far to name it.`; profession evidence `Tracks are broad and fresh. Whatever made them is heavy.`; village report `Two shepherds came back by the lower road. Both saw movement up there.`; map record `The ridge is marked as a game trail on the old survey.`; old record `A similar report appears in the winter ledger. That doesn't prove it's the same animal.`; player report `You saw it yourself? Then stop calling it a rumor.`; world event `After the storm, the tracks came down toward the road.`; rumor network `People keep saying there's something big up there. No one has shown me proof.`; quest proof `That claw mark settles the size question.`

---

## 13. Map reveal speech

A KNOWN place may be marked directly when the NPC has a plausible geographic basis. Do not force every known target through a cartographer. A mason can mark surveyed foundations, a fisherman a known wreck/shoal, a farmer a nearby pasture, a cleric a known chapel road.

RUMOR does not become a precise pin. LEAD may reveal an intermediate landmark, not fabricate target precision. Underground markers represent a verified entrance, safe surface approach, shaft, ravine, structure edge, or other honest reference—never a structure-center coordinate presented as a doorway. If CONFIRMED and already marked, acknowledge rather than redundantly reveal; only offer a genuinely new approach marker.

---

## 14. Weather and event injection

Use weather/events only when they change lived conditions or reflect recent history: Frostmarch snow changes tracks; Greenveil rain changes crossings; Sunscar heat changes route choice; Harvestwood fog hides road markers; recent raids change repair/store/patrol talk. Do not use weather as a random greeting skin with no consequence.

---

## 15. Resource architecture for 0.4.1

Existing ordinary villagers use a stable per-entity three-way variant. Treat those as durable delivery profiles, not synonymous greetings: base = practical/profession-first; `_v2` = cautious/socially observant; `_v3` = distinctive secondary voice (dry, curious, proud, weary, veteran, kind, etc.) fitted to profession. The same villager should not swap personality every click.

Hint resources are epistemic surfaces. `hint_structure_unknown`, `hint_structure_rumor`, `hint_structure_lead`, `hint_structure_known`, and `hint_structure_confirmed` are canonical state families. Profession-specific hint resources (`mason`, `records`, `water`, `route`, `cartographer`, `guard`) express evidence source and profession rather than cloning the generic lead.

A machine-readable authoring manifest should declare profession, stable personality family, knowledge niche, provenance bias, allowed player intents, and executable resource ID. It is a validation contract until runtime consumes more dimensions directly.

---

## 16. Validation requirements

CI should fail on invalid Conversations 1.0.5 action syntax; missing required knowledge-state resources; overlong beats; excessive word count; unsupported `{TOKEN}` placeholders in executable JSON; exact duplicate NPC beats across audited voice resources; near-duplicate NPC beats above the configured threshold; a profession missing a declared stable profile; duplicate personality IDs within one profession's three profiles; or manifest references to missing resources.

Player replies and interface-standard phrases may be whitelisted. The validator protects NPC voice, not `Show me your trades.`

---

## 17. Runtime boundary and required integration

Static resource writing cannot interpolate runtime-only target name, distance, direction, approach, speaker name, Trust, or local-fact payload with the current bridge. Therefore several v0.2 goals require integration work rather than fake static prose.

**A — dynamic clue payloads stay inside Conversations.** Runtime-generated directions/referrals must stop appearing as long action-bar prose. Present those values within the exchange when technically possible, then use only a tiny confirmation.

**B — select knowledge state before resource.** Read per-subject state before assigning hint dialogue. Once KNOWN, do not assign RUMOR wording. Once CONFIRMED, use acknowledgement/aftermath.

**C — direct KNOWN marking.** `Can you mark it?` should succeed from a plausible knowledgeable NPC at KNOWN, not only a special profession, with safe-approach semantics underground/submerged.

**D — recent history / anti-repeat.** Persist recent variant history per NPC×player and semantic-hint history per village×player. Stable personality remains stable while lines rotate within that profile.

**E — contextual dimensions.** Expose region, tier, Trust, active/recently resolved local facts, provenance, events/weather, civic role, and persistent names at selection time. Layer dimensions rather than materialize an impossible full JSON cross-product.

---

## 18. Acceptance tests

1. Three professions in one village sound recognizably different in evidence, vocabulary, and attitude.
2. Three villagers who know one ruin agree on the fact but do not sound cloned.
3. UNKNOWN never fabricates a bearing or marker.
4. RUMOR hedges and identifies source when possible.
5. LEAD gives a search method without pretending certainty.
6. KNOWN names the place and can offer a justified Atlas mark.
7. CONFIRMED acknowledges discovery and never reverts to rumor wording.
8. `Where exactly?`, `Can you mark it?`, `Who saw it?`, `How dangerous is it?`, `Who else would know?`, and `What am I looking for?` produce different semantic answers.
9. Completing a local problem changes nearby talk from request/hint to acknowledgement/consequence.
10. A second visit to the same structure explains why new work exists.
11. Stable personality does not mean one repeated sentence; recent useful lines rotate once runtime history is wired.
12. Action bars contain confirmations, not speeches.
13. Persistent names survive profession/Trust/quest transitions.
14. Family continuity appears only when relationship data supports it.
15. CI rejects exact/near-duplicate authored NPC beats and missing state resources.

---

## 19. Closing design statement

CozyCrazyCraft does not need every villager to be a bespoke novel character. It needs **coherent differences that arise from what people do, where they live, what they have personally learned, how well they know the player, and what has happened around them**.

The test is not whether the database contains many lines. The test is whether the player can walk through a village, ask the same question three times, and feel that three different people answered it—without any of them contradicting the world.
