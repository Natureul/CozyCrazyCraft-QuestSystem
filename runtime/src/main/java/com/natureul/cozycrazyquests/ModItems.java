package com.natureul.cozycrazyquests;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CozyCrazyQuests.MOD_ID);

    public static final RegistryObject<Item> STABLEMASTERS_SEAL = proof("stablemasters_seal");
    public static final RegistryObject<Item> SUNSCAR_TOMB_TABLET = proof("sunscar_tomb_tablet");
    public static final RegistryObject<Item> GREENVEIL_SURVEY_NOTES = proof("greenveil_survey_notes");
    public static final RegistryObject<Item> FROSTMARCH_DISPATCH = proof("frostmarch_dispatch");

    public static final RegistryObject<Item> CONVERSATION_TOKEN = ITEMS.register(
            "conversation_token",
            () -> new ConversationTokenItem(new Item.Properties().stacksTo(1))
    );
    public static final RegistryObject<Item> VILLAGE_CONTRACT = ITEMS.register(
            "village_contract",
            () -> new VillageContractItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    private ModItems() {}

    private static RegistryObject<Item> proof(String id) {
        return ITEMS.register(id, () -> new QuestProofItem(
                new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
        ));
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
