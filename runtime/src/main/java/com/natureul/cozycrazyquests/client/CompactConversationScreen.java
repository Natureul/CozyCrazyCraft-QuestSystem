package com.natureul.cozycrazyquests.client;

import com.google.gson.JsonParseException;
import com.natureul.cozycrazyquests.CozyCrazyQuests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Compact reflection-only presentation for Conversations 1.0.5.
 *
 * The upstream screen hard-codes a 512-GUI-pixel panel and positions all of its reply hitboxes
 * around the same fixed geometry. That makes it consume almost the entire screen at the GUI scale
 * used by CozyCrazyCraft. Replacing the presentation at ScreenEvent.Opening lets us keep the
 * upstream conversation/state/network implementation while rendering a responsive lower-third UI.
 *
 * Conversations' own client close handler only recognizes its stock ConversationScreen. Because
 * this class replaces that screen, a server-side dialogue.close used to leave the compact panel
 * sitting open even though the conversation was already over. After a reply is sent we therefore
 * watch the upstream client conversation state and close this replacement screen when the server
 * confirms that the conversation ended.
 */
public final class CompactConversationScreen extends Screen {
    private static final int MAX_PANEL_WIDTH = 430;
    private static final int MIN_PANEL_WIDTH = 280;
    private static final int OUTER_MARGIN = 18;
    private static final int INNER_PAD = 12;
    private static final int REPLY_HEIGHT = 18;
    private static final int REPLY_GAP = 3;
    private static final int CHARS_PER_TICK = 2;

    private static final String CONVERSATION_CLIENT_API =
            "com.lazrproductions.conversations.api.ConversationClientApi";

    private final Object conversation;
    private int visibleCharacters;
    private boolean closing;
    private boolean awaitingReplySync;
    private final List<ReplyBox> replyBoxes = new ArrayList<>();

    public CompactConversationScreen(Object conversation) {
        super(Component.empty());
        this.conversation = conversation;
    }

    @Override
    public void tick() {
        super.tick();

        if (awaitingReplySync && Minecraft.getInstance().screen == this) {
            Boolean upstreamActive = upstreamConversationActive();
            if (Boolean.FALSE.equals(upstreamActive)) {
                // The server already performed dialogue.close. Do not send a second stop packet
                // from onClose(); simply retire our replacement UI.
                closing = true;
                Minecraft.getInstance().setScreen(null);
                return;
            }
        }

        String text = dialogueText();
        if (visibleCharacters < text.length()) {
            visibleCharacters = Math.min(text.length(), visibleCharacters + CHARS_PER_TICK);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        replyBoxes.clear();

        String fullDialogue = dialogueText();
        String shownDialogue = fullDialogue.substring(0, Math.min(visibleCharacters, fullDialogue.length()));
        String[] replies = replies();

        int panelWidth = Mth.clamp(width - (OUTER_MARGIN * 2), MIN_PANEL_WIDTH, MAX_PANEL_WIDTH);
        int textWidth = panelWidth - (INNER_PAD * 2);
        List<net.minecraft.util.FormattedCharSequence> dialogueLines = font.split(Component.literal(shownDialogue), textWidth);
        List<net.minecraft.util.FormattedCharSequence> fullLines = font.split(Component.literal(fullDialogue), textWidth);
        int lineCount = Math.max(1, fullLines.size());

        int headerHeight = 30;
        int dialogueHeight = Math.max(20, lineCount * 10 + 4);
        int repliesHeight = replies.length == 0 ? 0 : replies.length * REPLY_HEIGHT + Math.max(0, replies.length - 1) * REPLY_GAP + 8;
        int panelHeight = headerHeight + dialogueHeight + repliesHeight + INNER_PAD;
        int x = (width - panelWidth) / 2;
        int y = Math.max(12, height - panelHeight - 18);

        // Intentionally restrained: enough separation from the world to read clearly without the
        // full-screen black slab of the stock Conversations UI.
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0xD914171B);
        graphics.fill(x, y, x + panelWidth, y + 1, 0xFF9A825D);
        graphics.fill(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, 0xFF564B3C);
        graphics.fill(x, y, x + 1, y + panelHeight, 0xFF6C5A43);
        graphics.fill(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, 0xFF6C5A43);

        Component speaker = speakerName();
        Component title = title();
        graphics.drawString(font, speaker, x + INNER_PAD, y + 9, 0xFFF2D59C, false);
        int titleWidth = font.width(title);
        if (titleWidth > 0 && titleWidth < panelWidth / 2) {
            graphics.drawString(font, title, x + panelWidth - INNER_PAD - titleWidth, y + 9, 0xFF9FA4AA, false);
        }

        int dialogueY = y + headerHeight;
        for (int i = 0; i < dialogueLines.size(); i++) {
            graphics.drawString(font, dialogueLines.get(i), x + INNER_PAD, dialogueY + i * 10, 0xFFECECEC, false);
        }

        int repliesY = y + headerHeight + dialogueHeight + 4;
        for (int i = 0; i < replies.length; i++) {
            int ry = repliesY + i * (REPLY_HEIGHT + REPLY_GAP);
            boolean hovered = mouseX >= x + INNER_PAD && mouseX < x + panelWidth - INNER_PAD
                    && mouseY >= ry && mouseY < ry + REPLY_HEIGHT;
            int background = hovered ? 0xD9424650 : 0xA8252930;
            graphics.fill(x + INNER_PAD, ry, x + panelWidth - INNER_PAD, ry + REPLY_HEIGHT, background);
            graphics.drawString(font, Component.literal(replies[i]), x + INNER_PAD + 7, ry + 5,
                    hovered ? 0xFFFFFFFF : 0xFFD7D9DC, false);
            replyBoxes.add(new ReplyBox(x + INNER_PAD, ry, panelWidth - INNER_PAD * 2, REPLY_HEIGHT, i));
        }

        if (visibleCharacters < fullDialogue.length()) {
            graphics.drawString(font, Component.literal("click to finish"), x + panelWidth - INNER_PAD - 72,
                    y + panelHeight - 10, 0xFF747A82, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        String dialogue = dialogueText();
        if (visibleCharacters < dialogue.length()) {
            visibleCharacters = dialogue.length();
            return true;
        }

        for (ReplyBox box : replyBoxes) {
            if (box.contains(mouseX, mouseY)) {
                performReply(box.index());
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Space/Enter first finish the typewriter rather than accidentally selecting an answer.
        if ((keyCode == 32 || keyCode == 257 || keyCode == 335) && visibleCharacters < dialogueText().length()) {
            visibleCharacters = dialogueText().length();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (!closing) {
            closing = true;
            invokeNetworking("sendConversationClosePacketToServer", new Class<?>[0]);
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void performReply(int index) {
        Object conversationId = invokeConversation("getConversationId");
        Object container = invokeConversation("getCurrentDialogueContainerIndex");
        Object option = invokeConversation("getCurrentDialogueOptionIndex");
        if (!(conversationId instanceof ResourceLocation id)
                || !(container instanceof Integer containerIndex)
                || !(option instanceof Integer optionIndex)) {
            CozyCrazyQuests.LOGGER.warn("Could not read Conversations reply coordinates for compact UI");
            return;
        }
        awaitingReplySync = true;
        invokeNetworking(
                "sendPerformReplyActionPacketToServer",
                new Class<?>[]{String.class, int.class, int.class, int.class},
                id.toString(), containerIndex, optionIndex, index
        );
    }

    private String dialogueText() {
        Object raw = invokeConversation("clientSafeGetCurrentDialogue");
        if (!(raw instanceof String json)) return "";
        return componentFromJson(json).getString().stripLeading();
    }

    private String[] replies() {
        Object raw = invokeConversation("clientSafeGetCurrentReplies");
        if (!(raw instanceof String[] jsonReplies)) return new String[0];
        String[] plain = new String[jsonReplies.length];
        for (int i = 0; i < jsonReplies.length; i++) plain[i] = componentFromJson(jsonReplies[i]).getString();
        return plain;
    }

    private Component title() {
        Object raw = invokeConversation("clientSafeGetConversationTitle");
        return raw instanceof Component component ? component : Component.empty();
    }

    private Component speakerName() {
        Object raw = invokeConversation("getSyncedSpeakerId");
        if (raw instanceof Integer entityId) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null) {
                Entity speaker = minecraft.level.getEntity(entityId);
                if (speaker != null) return speaker.getDisplayName();
            }
        }
        return Component.literal("Villager");
    }

    private static Boolean upstreamConversationActive() {
        try {
            Class<?> api = Class.forName(CONVERSATION_CLIENT_API);
            Field field = api.getDeclaredField("currentConversation");
            field.setAccessible(true);
            return field.get(null) != null;
        } catch (Throwable error) {
            // Unknown is deliberately different from false: reflection failure must never close a
            // live conversation on its own.
            return null;
        }
    }

    private Component componentFromJson(String json) {
        try {
            Component parsed = Component.Serializer.fromJson(json);
            return parsed == null ? Component.literal(json) : parsed;
        } catch (JsonParseException | IllegalArgumentException ignored) {
            return Component.literal(json);
        }
    }

    private Object invokeConversation(String methodName) {
        try {
            Method method = conversation.getClass().getMethod(methodName);
            return method.invoke(conversation);
        } catch (Throwable error) {
            CozyCrazyQuests.LOGGER.debug("Compact Conversations UI could not invoke {}", methodName, error);
            return null;
        }
    }

    private static Object invokeNetworking(String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Class<?> networking = Class.forName("com.lazrproductions.conversations.api.ConversationAPI$Networking");
            Method method = networking.getMethod(methodName, parameterTypes);
            return method.invoke(null, args);
        } catch (Throwable error) {
            CozyCrazyQuests.LOGGER.warn("Compact Conversations UI could not invoke networking method {}", methodName, error);
            return null;
        }
    }

    private record ReplyBox(int x, int y, int width, int height, int index) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
