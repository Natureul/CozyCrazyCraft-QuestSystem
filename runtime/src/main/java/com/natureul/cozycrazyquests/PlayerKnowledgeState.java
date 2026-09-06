package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Monotonic player knowledge for persistent subjects/places.
 *
 * This directly implements the Conversation Bible's UNKNOWN -> RUMOR -> LEAD -> KNOWN ->
 * CONFIRMED model. Dynamic facts such as road safety should use a separate incident/fact state;
 * the identity of a learned persistent place does not regress merely because its occupancy changes.
 */
final class PlayerKnowledgeState {
    private static final String ROOT = "CozyCrazyKnowledge";
    private static final String SUBJECTS = "subjects";

    private PlayerKnowledgeState() {}

    static Knowledge knowledge(ServerPlayer player, String subjectKey) {
        if (subjectKey == null || subjectKey.isBlank()) return Knowledge.UNKNOWN;
        CompoundTag tag = subject(player, subjectKey);
        String raw = tag.getString("state");
        try {
            return raw.isBlank() ? Knowledge.UNKNOWN : Knowledge.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return Knowledge.UNKNOWN;
        }
    }

    static boolean advance(ServerPlayer player, String subjectKey, Knowledge target, Provenance provenance) {
        if (subjectKey == null || subjectKey.isBlank() || target == null) return false;
        Knowledge current = knowledge(player, subjectKey);
        CompoundTag tag = subject(player, subjectKey);
        boolean changed = false;
        if (target.ordinal() > current.ordinal()) {
            tag.putString("state", target.name());
            changed = true;
        }

        if (provenance != null) {
            Set<String> provenanceValues = new LinkedHashSet<>();
            for (Tag raw : tag.getList("provenance", Tag.TAG_STRING)) provenanceValues.add(raw.getAsString());
            if (provenanceValues.add(provenance.id)) changed = true;
            ListTag list = new ListTag();
            provenanceValues.forEach(value -> list.add(StringTag.valueOf(value)));
            tag.put("provenance", list);
        }

        if (changed) saveSubject(player, subjectKey, tag);
        return changed;
    }

    static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        if (oldData.contains(ROOT, Tag.TAG_COMPOUND)) {
            event.getEntity().getPersistentData().put(ROOT, oldData.getCompound(ROOT).copy());
        }
    }

    private static CompoundTag subject(ServerPlayer player, String subjectKey) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT);
        CompoundTag subjects = root.getCompound(SUBJECTS);
        if (subjects.contains(subjectKey, Tag.TAG_COMPOUND)) return subjects.getCompound(subjectKey);
        return new CompoundTag();
    }

    private static void saveSubject(ServerPlayer player, String subjectKey, CompoundTag subject) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound(ROOT);
        CompoundTag subjects = root.getCompound(SUBJECTS);
        subjects.put(subjectKey, subject);
        root.put(SUBJECTS, subjects);
        persistent.put(ROOT, root);
    }

    enum Knowledge {
        UNKNOWN,
        RUMOR,
        LEAD,
        KNOWN,
        CONFIRMED
    }

    enum Provenance {
        LOCAL_OBSERVATION("local_observation"),
        PROFESSION_EVIDENCE("profession_evidence"),
        VILLAGE_REPORT("village_report"),
        MAP_RECORD("map_record"),
        OLD_RECORD("old_record"),
        PLAYER_REPORT("player_report"),
        WORLD_EVENT("world_event"),
        RUMOR_NETWORK("rumor_network"),
        QUEST_PROOF("quest_proof");

        private final String id;

        Provenance(String id) {
            this.id = id;
        }
    }
}
