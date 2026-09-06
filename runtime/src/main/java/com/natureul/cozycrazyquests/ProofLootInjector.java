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
 * Adds one deterministic quest-proof item to a deliberately chosen structure loot table.
 *
 * This is intentionally loot-table scoped rather than structure-ID scoped: it is tiny,
 * data-safe, works when the structure is generated normally, and does not create any
 * ticking/searching workload. The chosen tables are the structure's distinctive chest
 * tables in Dungeons Enhanced 5.4.x.
 */
public final class ProofLootInjector {
    private static final Map<ResourceLocation, RegistryObject<Item>> PROOFS = Map.of(
            id("dungeons_enhanced", "chests/stables"), ModItems.STABLEMASTERS_SEAL,
            id("dungeons_enhanced", "chests/desert_tomb"), ModItems.SUNSCAR_TOMB_TABLET,
            id("dungeons_enhanced", "chests/jungle_monument/treasure"), ModItems.GREENVEIL_SURVEY_NOTES,
            id("dungeons_enhanced", "chests/ice_pit/armory"), ModItems.FROSTMARCH_DISPATCH
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
        CozyCrazyQuests.LOGGER.debug("Injected quest proof {} into loot table {}", proof.getId(), event.getName());
    }

    private static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
