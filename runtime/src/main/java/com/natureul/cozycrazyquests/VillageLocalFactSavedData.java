package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * World-owned memory of concrete local facts attached to settlements.
 *
 * The Quest Bible asks villages to remember a small number of local facts so authored dialogue is
 * about this village rather than a global random pool. Facts survive the individual NPC that first
 * mentioned them, which also gives succession/recovery logic something stable to inherit.
 */
final class VillageLocalFactSavedData extends SavedData {
    private static final String DATA_NAME = "cozycrazyquests_village_local_facts";
    static final int RECOMMENDED_ACTIVE_FACTS = 3;

    private final List<Fact> facts = new ArrayList<>();

    static VillageLocalFactSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                VillageLocalFactSavedData::load,
                VillageLocalFactSavedData::new,
                DATA_NAME
        );
    }

    Fact rememberStructureSurvey(
            VillageContext village,
            ResourceLocation structureId,
            BlockPos target,
            String displayName,
            long gameTime
    ) {
        String factId = structureFactId(structureId, target);
        for (Fact fact : facts) {
            if (fact.villageKey.equals(village.key()) && fact.factId.equals(factId)) return fact;
        }

        Fact fact = new Fact(
                village.key(),
                factId,
                FactKind.STRUCTURE_SURVEY,
                structureId.toString(),
                displayName,
                target.immutable(),
                FactState.OPEN,
                gameTime,
                0L
        );
        facts.add(fact);
        setDirty();
        return fact;
    }

    void resolve(String villageKey, String factId, long gameTime) {
        for (int i = 0; i < facts.size(); i++) {
            Fact fact = facts.get(i);
            if (!fact.villageKey.equals(villageKey) || !fact.factId.equals(factId)) continue;
            if (fact.state == FactState.RESOLVED) return;
            facts.set(i, fact.withState(FactState.RESOLVED, gameTime));
            setDirty();
            return;
        }
    }

    List<Fact> activeFacts(String villageKey) {
        return facts.stream()
                .filter(fact -> fact.villageKey.equals(villageKey) && fact.state == FactState.OPEN)
                .sorted(Comparator.comparingLong(Fact::createdGameTime))
                .limit(RECOMMENDED_ACTIVE_FACTS)
                .toList();
    }

    List<Fact> history(String villageKey) {
        return facts.stream()
                .filter(fact -> fact.villageKey.equals(villageKey))
                .sorted(Comparator.comparingLong(Fact::createdGameTime))
                .toList();
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        ListTag list = new ListTag();
        for (Fact fact : facts) {
            CompoundTag tag = new CompoundTag();
            tag.putString("village_key", fact.villageKey);
            tag.putString("fact_id", fact.factId);
            tag.putString("kind", fact.kind.name());
            tag.putString("subject_id", fact.subjectId);
            tag.putString("display_name", fact.displayName);
            tag.putInt("x", fact.pos.getX());
            tag.putInt("y", fact.pos.getY());
            tag.putInt("z", fact.pos.getZ());
            tag.putString("state", fact.state.name());
            tag.putLong("created", fact.createdGameTime);
            tag.putLong("resolved", fact.resolvedGameTime);
            list.add(tag);
        }
        root.put("facts", list);
        return root;
    }

    static VillageLocalFactSavedData load(CompoundTag root) {
        VillageLocalFactSavedData data = new VillageLocalFactSavedData();
        for (Tag raw : root.getList("facts", Tag.TAG_COMPOUND)) {
            if (!(raw instanceof CompoundTag tag)) continue;
            try {
                data.facts.add(new Fact(
                        tag.getString("village_key"),
                        tag.getString("fact_id"),
                        FactKind.valueOf(tag.getString("kind")),
                        tag.getString("subject_id"),
                        tag.getString("display_name"),
                        new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
                        FactState.valueOf(tag.getString("state")),
                        tag.getLong("created"),
                        tag.getLong("resolved")
                ));
            } catch (IllegalArgumentException ignored) {
                // Ignore a future/invalid fact enum rather than corrupting the entire village ledger.
            }
        }
        return data;
    }

    static String structureFactId(ResourceLocation structureId, BlockPos target) {
        return "structure_survey:" + structureId + "@" + Math.floorDiv(target.getX(), 16) + "," + Math.floorDiv(target.getZ(), 16);
    }

    enum FactKind {
        STRUCTURE_SURVEY,
        COMMUNITY_INCIDENT,
        ECOLOGY,
        ROAD,
        WATER,
        CAVE,
        RECENT_EVENT
    }

    enum FactState {
        OPEN,
        RESOLVED
    }

    record Fact(
            String villageKey,
            String factId,
            FactKind kind,
            String subjectId,
            String displayName,
            BlockPos pos,
            FactState state,
            long createdGameTime,
            long resolvedGameTime
    ) {
        Fact withState(FactState newState, long gameTime) {
            return new Fact(villageKey, factId, kind, subjectId, displayName, pos, newState, createdGameTime, gameTime);
        }
    }
}
