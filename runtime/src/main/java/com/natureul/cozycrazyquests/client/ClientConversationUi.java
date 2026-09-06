package com.natureul.cozycrazyquests.client;

import com.natureul.cozycrazyquests.CozyCrazyQuests;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

/** Client-only compatibility layer for Conversations 1.0.5's fixed-size screen. */
@Mod.EventBusSubscriber(modid = CozyCrazyQuests.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientConversationUi {
    private static final String CONVERSATION_SCREEN =
            "com.lazrproductions.conversations.client.gui.ConversationScreen";
    private static boolean warned;

    private ClientConversationUi() {}

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen == null || !CONVERSATION_SCREEN.equals(screen.getClass().getName())) return;

        Object conversation = extractConversation(screen);
        if (conversation == null) return;
        event.setNewScreen(new CompactConversationScreen(conversation));
    }

    private static Object extractConversation(Screen screen) {
        try {
            Field field = screen.getClass().getDeclaredField("conversation");
            field.setAccessible(true);
            return field.get(screen);
        } catch (Throwable error) {
            if (!warned) {
                warned = true;
                CozyCrazyQuests.LOGGER.warn(
                        "Could not replace Conversations screen; falling back to the upstream UI",
                        error
                );
            }
            return null;
        }
    }
}
