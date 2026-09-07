package com.natureul.cozycrazyquests;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

/**
 * Stable village-facing responsibilities that do not depend on vanilla trade professions.
 *
 * A generated village can legitimately contain unemployed adults for a while. Progression must not
 * therefore wait for the player to manufacture workstations. We assign the first few adults in the
 * settlement deterministic civic/adventure responsibilities while leaving their vanilla profession,
 * workstation, trades and restock behavior completely untouched.
 *
 * Roles are deliberately social rather than occupational: steward, roadwarden, quartermaster and
 * watch contact. They may route work and represent the village, but never pretend an unemployed
 * villager owns a smithing table, loom, lectern, etc.
 */
final class VillageCivicRoleService {
    private static final int ROLE_RADIUS = 176;

    private VillageCivicRoleService() {}

    enum Role {
        STEWARD("steward", "civic_steward"),
        ROADWARDEN("roadwarden", "civic_roadwarden"),
        QUARTERMASTER("quartermaster", "civic_quartermaster"),
        WATCH_CONTACT("watch contact", "civic_watch_contact"),
        RESIDENT("resident", "");

        private final String label;
        private final String dialoguePath;

        Role(String label, String dialoguePath) {
            this.label = label;
            this.dialoguePath = dialoguePath;
        }

        String label() {
            return label;
        }

        String dialoguePath() {
            return dialoguePath;
        }

        boolean isCivicContact() {
            return this != RESIDENT;
        }
    }

    static Role roleFor(ServerLevel level, VillageContext village, Villager speaker) {
        if (village == null || speaker == null || speaker.isBaby()) return Role.RESIDENT;

        AABB area = new AABB(village.anchor()).inflate(ROLE_RADIUS, 64, ROLE_RADIUS);
        List<Villager> adults = level.getEntitiesOfClass(Villager.class, area, villager -> {
                    if (villager.isBaby()) return false;
                    VillageContext theirs = VillageContext.resolve(level, villager.blockPosition());
                    return theirs != null && village.key().equals(theirs.key());
                }).stream()
                .sorted(Comparator.comparing(Villager::getUUID))
                .toList();

        int index = -1;
        for (int i = 0; i < adults.size(); i++) {
            if (adults.get(i).getUUID().equals(speaker.getUUID())) {
                index = i;
                break;
            }
        }
        return switch (index) {
            case 0 -> Role.STEWARD;
            case 1 -> Role.ROADWARDEN;
            case 2 -> Role.QUARTERMASTER;
            case 3 -> Role.WATCH_CONTACT;
            default -> Role.RESIDENT;
        };
    }

    static boolean isCivicContact(ServerLevel level, VillageContext village, Villager villager) {
        return roleFor(level, village, villager).isCivicContact();
    }
}
