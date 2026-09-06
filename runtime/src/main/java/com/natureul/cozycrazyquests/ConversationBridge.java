package com.natureul.cozycrazyquests;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.lang.reflect.Method;

/**
 * Reflection-only bridge to Lazr Productions' Conversations mod.
 *
 * CozyCrazyQuests remains loadable without Conversations present; when it is installed, the mod's
 * LivingEntity mixin makes ordinary villagers (and other living entities such as Guard Villagers
 * guards) implement ICanDialogue. We deliberately touch only that small public surface instead of
 * compiling against the third-party jar.
 */
final class ConversationBridge {
    private static final String INTERFACE = "com.lazrproductions.conversations.entity.base.ICanDialogue";
    private static final String OWN_PREFIX = CozyCrazyQuests.MOD_ID + ":";

    private static boolean resolved;
    private static boolean available;
    private static Class<?> dialogueType;
    private static Method setDialogue;
    private static Method getDialogue;
    private static boolean warned;

    private ConversationBridge() {}

    static boolean available() {
        resolve();
        return available;
    }

    static boolean supports(LivingEntity entity) {
        resolve();
        return available && dialogueType.isInstance(entity);
    }

    static boolean setDialogue(LivingEntity entity, ResourceLocation dialogue) {
        resolve();
        if (!available || !dialogueType.isInstance(entity)) return false;
        try {
            setDialogue.invoke(entity, dialogue.toString());
            return true;
        } catch (Throwable error) {
            warnOnce("Could not assign Conversations dialogue", error);
            return false;
        }
    }

    static boolean hasOwnDialogue(LivingEntity entity) {
        resolve();
        if (!available || !dialogueType.isInstance(entity)) return false;
        try {
            Object current = getDialogue.invoke(entity);
            return current instanceof String id && id.startsWith(OWN_PREFIX);
        } catch (Throwable error) {
            warnOnce("Could not inspect Conversations dialogue on entity", error);
            return false;
        }
    }

    static void clearOwnDialogue(LivingEntity entity) {
        resolve();
        if (!available || !dialogueType.isInstance(entity)) return;
        try {
            Object current = getDialogue.invoke(entity);
            if (current instanceof String id && id.startsWith(OWN_PREFIX)) {
                setDialogue.invoke(entity, "null");
            }
        } catch (Throwable error) {
            warnOnce("Could not clear CozyCrazyQuests dialogue from entity", error);
        }
    }

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            dialogueType = Class.forName(INTERFACE);
            setDialogue = dialogueType.getMethod("setDialogue", String.class);
            getDialogue = dialogueType.getMethod("getDialogue");
            available = true;
            CozyCrazyQuests.LOGGER.info("Conversations integration detected; villager-authored quests enabled");
        } catch (Throwable ignored) {
            available = false;
            CozyCrazyQuests.LOGGER.info("Conversations mod not present; villager-authored dialogue quests disabled");
        }
    }

    private static void warnOnce(String message, Throwable error) {
        if (warned) return;
        warned = true;
        CozyCrazyQuests.LOGGER.warn(message, error);
    }
}
