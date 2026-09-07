package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;

/**
 * Adds deterministic proof items for the small set of Bountiful public recovery notices that still
 * intentionally use physical structure loot.
 *
 * Important 0.4.1 boundary: a loot table used by an authored villager recovery contract must not also
 * inject a different public-notice proof item. Authored recovery has one player-visible truth: the
 * quest-bound object seeded by RecoveryQuestRuntime. Frostmarch Ice Pit and Greenveil Jungle Monument
 * proofs were therefore retired once Last Warm Camp and Temple of Eight Roots became executable.
 */
public final class ProofLootInjector {
    private static final Map<ResourceLocation, RegistryObject<Item>> PROOFS = Map.of(
            id("dungeons_enhanced", "chests/stables"), ModItems.STABLEMASTERS_SEAL,
            id("dungeons_enhanced", "chests/desert_tomb"), ModItems.SUNSCAR_TOMB_TABLET
    );

    private ProofLootInjector() {}

    public static void onLootTableLoad(LootTableLoadEvent event) {
        RegistryObject<Item> proof = PROOFS.get(event.getName());
        if (proof == null) return;

        event.getTable().addPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(proof.get()))
                        .build()
        );
        CozyCrazyQuests.LOGGER.debug("Injected public-notice proof {} into loot table {}", proof.getId(), event.getName());
    }

    private static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
