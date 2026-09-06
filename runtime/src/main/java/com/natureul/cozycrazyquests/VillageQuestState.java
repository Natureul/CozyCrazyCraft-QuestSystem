package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Player-owned authored quest state.
 *
 * 0.3.5 stored exactly one global ACTIVE contract. That made every other village go socially silent
 * while the player was doing work elsewhere. 0.3.6 migrates that legacy slot into a village-keyed
 * contract book: at most one authored contract per village, but contracts from different villages
 * can coexist. The physical contract papers remain the lightweight player-facing reminder.
 */
final class VillageQuestState {
    static final String ROOT = "CozyCrazyVillagerQuests";
    static final String PENDING = "pending";
    static final String LEGACY_ACTIVE = "active";
    static final String ACTIVES = "active_by_village";
    static final String COMPLETED = "completed";
    static final String CONVERSATION_VILLAGE = "conversation_village_key";
    static final String CONVERSATION_SPEAKER = "conversation_speaker_uuid";
    static final String PAID_HINTS = "paid_hints";

    private VillageQuestState() {}

    static CompoundTag root(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) persistent.put(ROOT, new CompoundTag());
        CompoundTag root = persistent.getCompound(ROOT);
        migrateLegacyActive(root);
        persistent.put(ROOT, root);
        return root;
    }

    static void save(ServerPlayer player, CompoundTag root) {
        migrateLegacyActive(root);
        player.getPersistentData().put(ROOT, root);
    }

    static CompoundTag activeForVillage(CompoundTag root, String villageKey) {
        if (villageKey == null || villageKey.isBlank()) return new CompoundTag();
        CompoundTag actives = root.getCompound(ACTIVES);
        CompoundTag active = actives.getCompound(slot(villageKey));
        if (active.isEmpty()) return active;
        String stored = active.getString("village_key");
        return stored.isBlank() || stored.equals(villageKey) ? active : new CompoundTag();
    }

    static void putActive(CompoundTag root, String villageKey, CompoundTag active) {
        CompoundTag actives = root.getCompound(ACTIVES);
        actives.put(slot(villageKey), active);
        root.put(ACTIVES, actives);
    }

    static void removeActive(CompoundTag root, String villageKey) {
        CompoundTag actives = root.getCompound(ACTIVES);
        actives.remove(slot(villageKey));
        root.put(ACTIVES, actives);
    }

    static List<CompoundTag> allActives(CompoundTag root) {
        CompoundTag actives = root.getCompound(ACTIVES);
        List<CompoundTag> result = new ArrayList<>();
        for (String key : actives.getAllKeys()) {
            CompoundTag active = actives.getCompound(key);
            if (!active.isEmpty()) result.add(active);
        }
        return result;
    }

    static void noteConversation(CompoundTag root, String villageKey, String speakerUuid) {
        if (villageKey == null) villageKey = "";
        if (speakerUuid == null) speakerUuid = "";
        root.putString(CONVERSATION_VILLAGE, villageKey);
        root.putString(CONVERSATION_SPEAKER, speakerUuid);
    }

    static String conversationVillage(CompoundTag root) {
        return root.getString(CONVERSATION_VILLAGE);
    }

    static String conversationSpeaker(CompoundTag root) {
        return root.getString(CONVERSATION_SPEAKER);
    }

    static boolean hintPaid(CompoundTag root, String speakerUuid, String questId) {
        if (speakerUuid == null || speakerUuid.isBlank() || questId == null || questId.isBlank()) return false;
        return root.getCompound(PAID_HINTS).getBoolean(hintKey(speakerUuid, questId));
    }

    static void markHintPaid(CompoundTag root, String speakerUuid, String questId) {
        CompoundTag paid = root.getCompound(PAID_HINTS);
        paid.putBoolean(hintKey(speakerUuid, questId), true);
        root.put(PAID_HINTS, paid);
    }

    private static void migrateLegacyActive(CompoundTag root) {
        CompoundTag legacy = root.getCompound(LEGACY_ACTIVE);
        if (legacy.isEmpty()) return;

        String villageKey = legacy.getString("village_key");
        if (villageKey.isBlank()) {
            String dimension = legacy.getString("village_dimension");
            if (dimension.isBlank()) dimension = legacy.getString("board_dimension");
            villageKey = dimension + "|legacy|" + legacy.getInt("villageX") + "," + legacy.getInt("villageZ");
            legacy.putString("village_key", villageKey);
        }

        CompoundTag actives = root.getCompound(ACTIVES);
        if (actives.getCompound(slot(villageKey)).isEmpty()) {
            actives.put(slot(villageKey), legacy.copy());
            root.put(ACTIVES, actives);
        }
        root.remove(LEGACY_ACTIVE);
    }

    private static String slot(String villageKey) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(villageKey.getBytes(StandardCharsets.UTF_8));
    }

    private static String hintKey(String speakerUuid, String questId) {
        return speakerUuid + "|" + questId;
    }
}
