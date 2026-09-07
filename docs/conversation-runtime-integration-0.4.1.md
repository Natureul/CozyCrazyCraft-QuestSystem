# Conversation Voice Pass 0.4.1 — Integration Handoff

Branch authority: conversation-writing / villager-voice pass.

This file records runtime changes deliberately **not** made in the protected integration-core files by this branch.

## Why this handoff exists

The 0.4.0 field findings identify long route/referral prose in the action bar as a P0 UX defect. Static Conversations resources can improve voice and branching, but they cannot interpolate runtime-only target name, distance, direction, approach, speaker name, or local-fact payload with the current `ConversationBridge`, which only assigns a resource ID.

## Protected files that main integration should update

### `QuestHintNetwork.java`

At base commit `2e413cba282af6a11d5372b177d6c51d9df4503b`, `routeForActiveQuest(...)` writes a full named-person referral + distance to `displayClientMessage(..., true)` and `giveLead(...)` writes target/bearing/approach prose there too. Selection is profession-first rather than first branching on the player's existing per-subject `UNKNOWN/RUMOR/LEAD/KNOWN/CONFIRMED` state.

Required integration:

1. Read existing `PlayerKnowledgeState` for the subject before selecting a resource.
2. Use `hint_structure_known` for KNOWN and `hint_structure_confirmed` for CONFIRMED; equivalent underground resources are supplied by this pass.
3. Stop emitting route/referral sentences through the action bar. Keep only a tiny confirmation such as `Lead noted.` or `Atlas marked.` after the spoken exchange.
4. Preserve uncertainty: RUMOR/LEAD actions must not silently promote to an exact pin.
5. KNOWN should offer a direct mark from any plausibly knowledgeable speaker, using safe-approach semantics for underground/submerged targets.

### `VillageConversationQuestManager.java`

Ensure `markCurrentTargetOnAtlas(...)` follows v0.2: KNOWN is sufficient when the active speaker has plausible knowledge or reliable map/record provenance; RUMOR is not; underground markers use safe known approaches rather than chamber/structure centers; CONFIRMED does not create a redundant reveal.

No changes were made here on this branch.

## Non-protected runtime surface worth aligning during merge

### `VillageSocialConversationManager.java`

Base behavior also emits a long referral sentence from `routeToUsefulPerson(...)` into the action bar. This content branch leaves the Java untouched to reduce parallel-branch conflicts. Integration should replace it with Conversation-delivered referral semantics and a tiny confirmation if one is still needed.

Ambient selection currently uses a stable `UUID % 3` profile. The rewritten base / `_v2` / `_v3` resources intentionally treat those slots as stable personality families, so this remains a safe fallback. For the fuller v0.2 model, selection should layer region, tier, Trust, local facts, recent completion, event/weather state, provenance, and recent-history without exploding into a literal cross-product of files.

## Recent-history contract

Recommended minimal runtime state: NPC×player last 3 ambient variant/line IDs; village×player last 5 semantic hint families; subject highest knowledge state already communicated; recent completed fact acknowledgement exposure count, strongly down-weighted after 2–3 repeats. Do not reroll the NPC's stable personality when rotating lines.

## Persistent names / family continuity

This branch assumes `VillagerNameService` remains the canonical source of persistent display names. When dynamic referrals move into Conversations, use the existing persistent name when the speaker plausibly knows the target person. Do not derive kinship from surname equality alone; household/family data must authorize relationship language.

## Merge acceptance

Verify UNKNOWN has no hidden exact action-bar direction; RUMOR sounds uncertain and does not pin; LEAD is navigable but bounded; KNOWN can mark a justified approach; CONFIRMED gets acknowledgement; route/referral action bars are no longer sentences; repeated villagers do not all emit the same semantic hint wording.
