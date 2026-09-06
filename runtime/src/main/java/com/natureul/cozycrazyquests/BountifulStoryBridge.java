package com.natureul.cozycrazyquests;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reflection-only presentation bridge to Bountiful 6.0.4.
 * Gameplay remains owned by Bountiful; if this bridge ever fails, the only
 * thing lost is our richer story presentation.
 */
public final class BountifulStoryBridge {
    private static Object bountyDataCompanion;
    private static Method bountyDataGet;
    private static Field bountyObjectives;
    private static boolean warned;

    private BountifulStoryBridge() {}

    public static String entryId(Object entry) {
        if (entry == null) return null;
        try {
            try {
                Method getter = entry.getClass().getMethod("getId");
                Object value = getter.invoke(entry);
                return value == null ? null : value.toString();
            } catch (NoSuchMethodException ignored) {
                Field id = findField(entry.getClass(), "id");
                id.setAccessible(true);
                Object value = id.get(entry);
                return value == null ? null : value.toString();
            }
        } catch (Throwable error) {
            warnOnce("Could not read Bountiful entry id", error);
            return null;
        }
    }

    public static List<String> bountyObjectiveIds(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return Collections.emptyList();
        try {
            if (bountyDataCompanion == null || bountyDataGet == null) {
                Class<?> bountyDataClass = Class.forName("io.ejekta.bountiful.bounty.BountyData");
                Field companionField = bountyDataClass.getField("Companion");
                bountyDataCompanion = companionField.get(null);
                bountyDataGet = findSingleArgMethod(bountyDataCompanion.getClass(), "get", ItemStack.class);
                bountyDataGet.setAccessible(true);
            }

            Object data = bountyDataGet.invoke(bountyDataCompanion, stack);
            if (data == null) return Collections.emptyList();
            if (bountyObjectives == null) {
                bountyObjectives = findField(data.getClass(), "objectives");
                bountyObjectives.setAccessible(true);
            }
            Object value = bountyObjectives.get(data);
            if (!(value instanceof List<?> entries)) return Collections.emptyList();

            List<String> ids = new ArrayList<>(entries.size());
            for (Object entry : entries) {
                String id = entryId(entry);
                if (id != null) ids.add(id);
            }
            return ids;
        } catch (Throwable error) {
            warnOnce("Could not read Bountiful bounty objectives for story tooltip", error);
            return Collections.emptyList();
        }
    }

    private static Method findSingleArgMethod(Class<?> type, String name, Class<?> desiredArg) throws NoSuchMethodException {
        Class<?> cursor = type;
        while (cursor != null) {
            for (Method method : cursor.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != 1) continue;
                Class<?> parameter = method.getParameterTypes()[0];
                if (parameter.isAssignableFrom(desiredArg) || desiredArg.isAssignableFrom(parameter)) {
                    return method;
                }
            }
            cursor = cursor.getSuperclass();
        }
        throw new NoSuchMethodException(name + "(ItemStack)");
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void warnOnce(String message, Throwable error) {
        if (!warned) {
            warned = true;
            CozyCrazyQuests.LOGGER.warn(message + "; falling back to stock Bountiful presentation", error);
        }
    }
}
