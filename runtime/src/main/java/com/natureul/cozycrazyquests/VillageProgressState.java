package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Player-specific village/chapter progression derived from semantic accomplishments rather than a
 * raw quest-completion counter.
 *
 * This is the first runtime implementation of the Progression Bible's social ratchet. Bountiful
 * reputation may still be synchronized for compatibility, but it is no longer the authority that
 * decides whether a village can speak to the player or whether an authored chapter advances.
 */
final class VillageProgressState {
    private static final String ROOT = "CozyCrazyVillageProgress";
    private static final String VILLAGES = "villages";
    private static final String CATEGORIES = "categories";
    private static final String ACCOMPLISHMENTS = "accomplishments";
    private static final String MILESTONES = "milestones";

    private VillageProgressState() {}

    static Snapshot snapshot(ServerPlayer player, String villageKey) {
        CompoundTag state = village(player, villageKey, false);
        EnumSet<AccomplishmentCategory> categories = EnumSet.noneOf(AccomplishmentCategory.class);
        CompoundTag categoryTag = state.getCompound(CATEGORIES);
        for (AccomplishmentCategory category : AccomplishmentCategory.values()) {
            if (categoryTag.getBoolean(category.name())) categories.add(category);
        }

        CompoundTag milestones = state.getCompound(MILESTONES);
        boolean capstoneComplete = milestones.getBoolean("Z1_CAPSTONE_COMPLETE");
        boolean proven = milestones.getBoolean("REGIONAL_PROVEN");

        Trust trust;
        if (proven) trust = Trust.PROVEN;
        else if (capstoneComplete) trust = Trust.TRUSTED;
        else if (categories.size() >= 2) trust = Trust.RELIABLE;
        else if (!categories.isEmpty()) trust = Trust.RECOGNIZED;
        else trust = Trust.STRANGER;

        boolean capstoneEligible = categories.contains(AccomplishmentCategory.COMMUNITY)
                && categories.contains(AccomplishmentCategory.EXPLORATION)
                && (categories.contains(AccomplishmentCategory.PROFESSION)
                    || categories.contains(AccomplishmentCategory.DANGER));

        Chapter chapter;
        if (capstoneComplete) chapter = Chapter.Z1_COMPLETE;
        else if (capstoneEligible) chapter = Chapter.Z1_CAPSTONE;
        else if (!categories.isEmpty()) chapter = Chapter.Z1_BRANCHING;
        else chapter = Chapter.Z1_ARRIVAL;

        return new Snapshot(chapter, trust, Set.copyOf(categories), capstoneEligible, capstoneComplete, proven);
    }

    static boolean recordAccomplishment(
            ServerPlayer player,
            String villageKey,
            AccomplishmentCategory category,
            String accomplishmentId
    ) {
        if (villageKey == null || villageKey.isBlank() || accomplishmentId == null || accomplishmentId.isBlank()) return false;
        CompoundTag state = village(player, villageKey, true);
        Set<String> existing = readStrings(state.getList(ACCOMPLISHMENTS, Tag.TAG_STRING));
        String key = category.name().toLowerCase(Locale.ROOT) + ":" + accomplishmentId;
        if (!existing.add(key)) return false;

        ListTag list = new ListTag();
        existing.stream().sorted().forEach(value -> list.add(StringTag.valueOf(value)));
        state.put(ACCOMPLISHMENTS, list);

        CompoundTag categories = state.getCompound(CATEGORIES);
        categories.putBoolean(category.name(), true);
        state.put(CATEGORIES, categories);
        saveVillage(player, villageKey, state);
        return true;
    }

    static void markZoneOneCapstoneComplete(ServerPlayer player, String villageKey) {
        markMilestone(player, villageKey, "Z1_CAPSTONE_COMPLETE");
    }

    static void markRegionalProven(ServerPlayer player, String villageKey) {
        markMilestone(player, villageKey, "REGIONAL_PROVEN");
    }

    static void markMilestone(ServerPlayer player, String villageKey, String milestone) {
        if (villageKey == null || villageKey.isBlank() || milestone == null || milestone.isBlank()) return;
        CompoundTag state = village(player, villageKey, true);
        CompoundTag milestones = state.getCompound(MILESTONES);
        milestones.putBoolean(milestone, true);
        state.put(MILESTONES, milestones);
        saveVillage(player, villageKey, state);
    }

    static boolean hasAccomplishment(ServerPlayer player, String villageKey, String accomplishmentId) {
        CompoundTag state = village(player, villageKey, false);
        if (state.isEmpty()) return false;
        for (Tag raw : state.getList(ACCOMPLISHMENTS, Tag.TAG_STRING)) {
            String value = raw.getAsString();
            if (value.endsWith(":" + accomplishmentId)) return true;
        }
        return false;
    }

    static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag oldData = event.getOriginal().getPersistentData();
        if (oldData.contains(ROOT, Tag.TAG_COMPOUND)) {
            event.getEntity().getPersistentData().put(ROOT, oldData.getCompound(ROOT).copy());
        }
    }

    private static CompoundTag village(ServerPlayer player, String villageKey, boolean create) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound(ROOT);
        CompoundTag villages = root.getCompound(VILLAGES);
        if (villages.contains(villageKey, Tag.TAG_COMPOUND)) return villages.getCompound(villageKey);
        return create ? new CompoundTag() : new CompoundTag();
    }

    private static void saveVillage(ServerPlayer player, String villageKey, CompoundTag state) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound(ROOT);
        CompoundTag villages = root.getCompound(VILLAGES);
        villages.put(villageKey, state);
        root.put(VILLAGES, villages);
        persistent.put(ROOT, root);
    }

    private static Set<String> readStrings(ListTag list) {
        Set<String> values = new HashSet<>();
        for (Tag raw : list) values.add(raw.getAsString());
        return values;
    }

    enum AccomplishmentCategory {
        COMMUNITY,
        EXPLORATION,
        PROFESSION,
        DANGER
    }

    enum Trust {
        STRANGER,
        RECOGNIZED,
        RELIABLE,
        TRUSTED,
        PROVEN
    }

    enum Chapter {
        Z1_ARRIVAL,
        Z1_BRANCHING,
        Z1_CAPSTONE,
        Z1_COMPLETE
    }

    record Snapshot(
            Chapter chapter,
            Trust trust,
            Set<AccomplishmentCategory> categories,
            boolean capstoneEligible,
            boolean capstoneComplete,
            boolean proven
    ) {}
}
