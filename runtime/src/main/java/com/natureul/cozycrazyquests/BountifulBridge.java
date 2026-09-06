package com.natureul.cozycrazyquests;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class BountifulBridge {
    private static final int MIN_NOTICES = 5;
    private static final int MAX_NOTICES = 7;
    private static final ThreadLocal<Boolean> MAINTAINING = ThreadLocal.withInitial(() -> false);

    private static Field decreesField;
    private static Field bountiesField;
    private static Field creatorRepField;
    private static Method removeBountyMethod;
    private static Method randomUpdateMethod;
    private static Method decreeCreateMethod;
    private static Object decreeCompanion;
    private static boolean warnedBoard;
    private static boolean warnedCreator;

    private BountifulBridge() {}

    public static void stampRegionalDecreeIfEmpty(Object boardObject) {
        try {
            if (!(boardObject instanceof BlockEntity blockEntity)) return;
            if (!(blockEntity.getLevel() instanceof ServerLevel level)) return;

            Container decrees = decrees(boardObject);
            if (!decrees.isEmpty()) return;

            String decreeId = ZoneBridge.decreeFor(level, blockEntity.getBlockPos());
            ItemStack decree = createDecree(decreeId);
            if (decree.isEmpty()) {
                CozyCrazyQuests.LOGGER.warn("Bountiful returned an empty decree for {} at {}", decreeId, blockEntity.getBlockPos());
                return;
            }

            int slot = Math.min(1, decrees.getContainerSize() - 1);
            decrees.setItem(slot, decree);
            blockEntity.setChanged();
            CozyCrazyQuests.LOGGER.debug("Stamped bounty board at {} with decree {}", blockEntity.getBlockPos(), decreeId);
        } catch (Throwable error) {
            warnBoardOnce("Could not stamp regional Bountiful decree", error);
        }
    }

    public static void maintainNoticeCount(Object boardObject) {
        if (MAINTAINING.get()) return;
        MAINTAINING.set(true);
        try {
            Container bounties = bounties(boardObject);
            int count = occupiedSlots(bounties).size();

            if (count > MAX_NOTICES) {
                List<Integer> occupied = occupiedSlots(bounties);
                for (int i = occupied.size() - 1; i >= 0 && count > MAX_NOTICES; i--) {
                    removeBounty(boardObject, occupied.get(i));
                    count--;
                }
            }

            int attempts = 0;
            while (count < MIN_NOTICES && attempts++ < 12) {
                randomUpdate(boardObject);
                count = occupiedSlots(bounties).size();
            }
        } catch (Throwable error) {
            warnBoardOnce("Could not maintain CozyCrazyCraft bounty-board population", error);
        } finally {
            MAINTAINING.set(false);
        }
    }

    public static int creatorReputation(Object creator) {
        try {
            if (creatorRepField == null) {
                creatorRepField = findField(creator.getClass(), "rep");
                creatorRepField.setAccessible(true);
            }
            return creatorRepField.getInt(creator);
        } catch (Throwable error) {
            if (!warnedCreator) {
                warnedCreator = true;
                CozyCrazyQuests.LOGGER.warn("Could not read Bountiful creator reputation; trust-gated objectives will fail open", error);
            }
            return Integer.MAX_VALUE;
        }
    }

    public static double objectiveRepRequired(Object poolEntry) {
        try {
            Method getter = poolEntry.getClass().getMethod("getRepRequired");
            Object value = getter.invoke(poolEntry);
            return value instanceof Number number ? number.doubleValue() : 0.0D;
        } catch (NoSuchMethodException ignored) {
            try {
                Field field = findField(poolEntry.getClass(), "repRequired");
                field.setAccessible(true);
                Object value = field.get(poolEntry);
                return value instanceof Number number ? number.doubleValue() : 0.0D;
            } catch (Throwable error) {
                return 0.0D;
            }
        } catch (Throwable error) {
            return 0.0D;
        }
    }

    private static Container decrees(Object board) throws Exception {
        if (decreesField == null) {
            decreesField = findField(board.getClass(), "decrees");
            decreesField.setAccessible(true);
        }
        return (Container) decreesField.get(board);
    }

    private static Container bounties(Object board) throws Exception {
        if (bountiesField == null) {
            bountiesField = findField(board.getClass(), "bounties");
            bountiesField.setAccessible(true);
        }
        return (Container) bountiesField.get(board);
    }

    private static List<Integer> occupiedSlots(Container inventory) {
        List<Integer> occupied = new ArrayList<>();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (!inventory.getItem(i).isEmpty()) occupied.add(i);
        }
        return occupied;
    }

    private static void removeBounty(Object board, int slot) throws Exception {
        if (removeBountyMethod == null) {
            removeBountyMethod = board.getClass().getMethod("removeBounty", int.class);
            removeBountyMethod.setAccessible(true);
        }
        removeBountyMethod.invoke(board, slot);
    }

    private static void randomUpdate(Object board) throws Exception {
        if (randomUpdateMethod == null) {
            randomUpdateMethod = findMethod(board.getClass(), "randomlyUpdateBoard");
            randomUpdateMethod.setAccessible(true);
        }
        randomUpdateMethod.invoke(board);
    }

    private static ItemStack createDecree(String decreeId) throws Exception {
        if (decreeCreateMethod == null || decreeCompanion == null) {
            Class<?> decreeItemClass = Class.forName("io.ejekta.bountiful.content.DecreeItem");
            Field companion = decreeItemClass.getField("Companion");
            decreeCompanion = companion.get(null);
            decreeCreateMethod = decreeCompanion.getClass().getMethod("create", String.class);
            decreeCreateMethod.setAccessible(true);
        }
        Object value = decreeCreateMethod.invoke(decreeCompanion, decreeId);
        return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
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

    private static Method findMethod(Class<?> type, String name, Class<?>... args) throws NoSuchMethodException {
        Class<?> cursor = type;
        while (cursor != null) {
            try {
                return cursor.getDeclaredMethod(name, args);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static void warnBoardOnce(String message, Throwable error) {
        if (!warnedBoard) {
            warnedBoard = true;
            CozyCrazyQuests.LOGGER.warn(message, error);
        }
    }
}
