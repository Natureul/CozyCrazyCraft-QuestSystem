package com.natureul.cozycrazyquests;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Lazy, persistent person naming based on the Villager Name Bible.
 *
 * Naming is intentionally tied to the person, not their current profession. A villager promoted
 * from unemployed to cartographer keeps the same name. The first interaction is the low-cost point
 * at which ordinary villagers/guards are named; after that vanilla entity NBT persists the custom
 * name and our structured name metadata.
 */
final class VillagerNameService {
    private static final String NAMED = "ccc_named_person";
    private static final String FIRST = "ccc_first_name";
    private static final String SURNAME = "ccc_surname";
    private static final String VILLAGE_KEY = "ccc_name_village";
    private static final String HOUSEHOLD = "ccc_household_id";

    private VillagerNameService() {}

    static void ensureNamed(ServerLevel level, LivingEntity entity) {
        CompoundTag persistent = entity.getPersistentData();
        if (persistent.getBoolean(NAMED)) return;

        // A pack author/player may have deliberately named this entity already. Respect that name
        // rather than treating CozyCrazyCraft's generator as the highest authority over people.
        if (entity.hasCustomName()) {
            persistent.putBoolean(NAMED, true);
            return;
        }

        ZoneBridge.Cell cell = ZoneBridge.cellAt(level, entity.blockPosition());
        String villageName = NamedPlaceBridge.nearestVillageName(level, entity.blockPosition(), 220);
        String villageKey = villageKey(level, entity, villageName, cell);
        double regionalWeight = regionalWeight(cell);

        VillagerNamePools pools = VillagerNamePools.get();
        VillagerNameSavedData saved = VillagerNameSavedData.get(level);
        VillagerNameSavedData.VillageNames village = saved.village(
                villageKey,
                () -> createCoreFamilies(level, villageKey, cell, regionalWeight, pools)
        );

        long seed = level.getSeed()
                ^ entity.getUUID().getMostSignificantBits()
                ^ Long.rotateLeft(entity.getUUID().getLeastSignificantBits(), 21)
                ^ villageKey.hashCode();
        Random random = new Random(seed);

        String first = "Alden";
        String surname = "Carter";
        String full = first + " " + surname;
        for (int attempt = 0; attempt < 16; attempt++) {
            first = chooseGiven(random, cell, regionalWeight, pools);
            surname = choosePersonSurname(random, cell, regionalWeight, pools, village.coreSurnames());
            full = first + " " + surname;
            if (saved.claim(villageKey, full)) break;
        }

        entity.setCustomName(Component.literal(full));
        persistent.putBoolean(NAMED, true);
        persistent.putString(FIRST, first);
        persistent.putString(SURNAME, surname);
        persistent.putString(VILLAGE_KEY, villageKey);
        persistent.putString(HOUSEHOLD, householdId(villageKey, surname, village.coreSurnames(), entity));
    }

    private static List<String> createCoreFamilies(
            ServerLevel level,
            String villageKey,
            ZoneBridge.Cell cell,
            double regionalWeight,
            VillagerNamePools pools
    ) {
        Random random = new Random(level.getSeed() ^ ((long) villageKey.hashCode() << 32) ^ villageKey.length());
        int targetCount = 5 + Math.floorMod(villageKey.hashCode(), 3); // 5-7 core families for a normal village
        List<String> result = new ArrayList<>();
        int guard = 0;
        while (result.size() < targetCount && guard++ < 100) {
            String candidate = chooseLocalSurname(random, cell, regionalWeight, pools);
            if (!result.contains(candidate)) result.add(candidate);
        }
        if (result.isEmpty()) result.add("Carter");
        return result;
    }

    private static String chooseGiven(Random random, ZoneBridge.Cell cell, double regionalWeight, VillagerNamePools pools) {
        List<String> regional = pools.regionalGiven(cell.macro());
        // The Bible puts most regional identity in surnames. Given names travel much more freely.
        if (!regional.isEmpty() && random.nextDouble() < regionalWeight * 0.45D) {
            return regional.get(random.nextInt(regional.size()));
        }
        List<String> shared = pools.sharedGiven();
        return shared.get(random.nextInt(shared.size()));
    }

    private static String choosePersonSurname(
            Random random,
            ZoneBridge.Cell cell,
            double regionalWeight,
            VillagerNamePools pools,
            List<String> coreFamilies
    ) {
        double roll = random.nextDouble();
        if (!coreFamilies.isEmpty() && roll < 0.74D) {
            return coreFamilies.get(random.nextInt(coreFamilies.size()));
        }
        if (roll < 0.90D) {
            return chooseLocalSurname(random, cell, regionalWeight, pools);
        }
        return chooseMigrantSurname(random, cell, pools);
    }

    private static String chooseLocalSurname(Random random, ZoneBridge.Cell cell, double regionalWeight, VillagerNamePools pools) {
        List<String> regional = pools.regionalSurnames(cell.macro());
        if (!regional.isEmpty() && random.nextDouble() < regionalWeight) {
            return regional.get(random.nextInt(regional.size()));
        }
        List<String> shared = pools.sharedSurnames();
        return shared.get(random.nextInt(shared.size()));
    }

    private static String chooseMigrantSurname(Random random, ZoneBridge.Cell cell, VillagerNamePools pools) {
        List<String> macros = new ArrayList<>(List.of("NORTH", "EAST", "SOUTH", "WEST"));
        macros.remove(cell.macro());
        String macro = macros.get(random.nextInt(macros.size()));
        List<String> migrant = pools.regionalSurnames(macro);
        if (migrant.isEmpty()) return pools.sharedSurnames().get(random.nextInt(pools.sharedSurnames().size()));
        return migrant.get(random.nextInt(migrant.size()));
    }

    private static double regionalWeight(ZoneBridge.Cell cell) {
        if (!cell.known()) return 0.0D;
        if ("SHARED_CORE".equals(cell.band())) return 0.15D;
        return switch (ZoneBridge.tierRank(cell.tier())) {
            case 0 -> 0.50D;
            case 1 -> 0.60D;
            case 2 -> 0.70D;
            case 3 -> 0.76D;
            default -> 0.25D;
        };
    }

    private static String villageKey(ServerLevel level, LivingEntity entity, String villageName, ZoneBridge.Cell cell) {
        String dimension = level.dimension().location().toString();
        if (villageName != null && !villageName.isBlank() && !"the village".equalsIgnoreCase(villageName)) {
            return dimension + "|" + villageName.toLowerCase(Locale.ROOT).replace(' ', '_');
        }
        int areaX = Math.floorDiv(entity.blockPosition().getX(), 128);
        int areaZ = Math.floorDiv(entity.blockPosition().getZ(), 128);
        return dimension + "|" + cell.macro().toLowerCase(Locale.ROOT) + "|" + areaX + "|" + areaZ;
    }

    private static String householdId(String villageKey, String surname, List<String> coreFamilies, LivingEntity entity) {
        int familyIndex = coreFamilies.indexOf(surname);
        if (familyIndex >= 0) return villageKey + "#family-" + familyIndex;
        return villageKey + "#arrival-" + Math.floorMod(entity.getUUID().hashCode(), 10000);
    }
}
