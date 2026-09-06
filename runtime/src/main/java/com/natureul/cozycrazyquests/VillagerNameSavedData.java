package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/** Persistent family/name memory for settlements encountered by the player. */
final class VillagerNameSavedData extends SavedData {
    private static final String DATA_NAME = "cozycrazyquests_villager_names";
    private final Map<String, VillageNames> villages = new HashMap<>();

    static VillagerNameSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                VillagerNameSavedData::load,
                VillagerNameSavedData::new,
                DATA_NAME
        );
    }

    VillageNames village(String key, Supplier<List<String>> familyFactory) {
        VillageNames existing = villages.get(key);
        if (existing != null) return existing;
        VillageNames created = new VillageNames(new ArrayList<>(familyFactory.get()), new HashSet<>());
        villages.put(key, created);
        setDirty();
        return created;
    }

    boolean claim(String villageKey, String fullName) {
        VillageNames names = villages.get(villageKey);
        if (names == null) return true;
        if (!names.usedFullNames.add(fullName)) return false;
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        ListTag villagesTag = new ListTag();
        for (Map.Entry<String, VillageNames> entry : villages.entrySet()) {
            CompoundTag village = new CompoundTag();
            village.putString("key", entry.getKey());

            ListTag families = new ListTag();
            for (String surname : entry.getValue().coreSurnames) families.add(StringTag.valueOf(surname));
            village.put("families", families);

            ListTag used = new ListTag();
            for (String name : entry.getValue().usedFullNames) used.add(StringTag.valueOf(name));
            village.put("used", used);
            villagesTag.add(village);
        }
        root.put("villages", villagesTag);
        return root;
    }

    static VillagerNameSavedData load(CompoundTag root) {
        VillagerNameSavedData data = new VillagerNameSavedData();
        ListTag villages = root.getList("villages", Tag.TAG_COMPOUND);
        for (Tag raw : villages) {
            if (!(raw instanceof CompoundTag tag)) continue;
            String key = tag.getString("key");
            if (key.isBlank()) continue;

            List<String> families = new ArrayList<>();
            for (Tag family : tag.getList("families", Tag.TAG_STRING)) families.add(family.getAsString());
            Set<String> used = new HashSet<>();
            for (Tag name : tag.getList("used", Tag.TAG_STRING)) used.add(name.getAsString());
            data.villages.put(key, new VillageNames(families, used));
        }
        return data;
    }

    static final class VillageNames {
        private final List<String> coreSurnames;
        private final Set<String> usedFullNames;

        VillageNames(List<String> coreSurnames, Set<String> usedFullNames) {
            this.coreSurnames = new ArrayList<>(coreSurnames);
            this.usedFullNames = new HashSet<>(usedFullNames);
        }

        List<String> coreSurnames() {
            return coreSurnames;
        }
    }
}
