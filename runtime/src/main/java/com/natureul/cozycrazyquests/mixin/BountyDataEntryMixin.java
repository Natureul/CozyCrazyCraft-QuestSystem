package com.natureul.cozycrazyquests.mixin;

import com.natureul.cozycrazyquests.BountifulStoryBridge;
import com.natureul.cozycrazyquests.BountyStoryTooltip;
import com.natureul.cozycrazyquests.StoryCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "io.ejekta.bountiful.bounty.BountyDataEntry", remap = false)
public abstract class BountyDataEntryMixin {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Inject(method = "textBoard", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void cozycrazyquests$authorBoardHover(CallbackInfoReturnable cir) {
        String id = BountifulStoryBridge.entryId(this);
        StoryCatalog.Card card = StoryCatalog.get(id);
        if (card == null) return;

        Object result = cir.getReturnValue();
        if (!(result instanceof List<?> original)) return;

        List<Object> lines = new ArrayList<>(original);

        // The stock board hover begins with the raw objective description (for example,
        // "Skeleton"). The accepted bounty already uses our authored title. Replace only that
        // first summary line so the notice is recognizably the same quest before and after pickup;
        // any subsequent Bountiful progress/amount lines remain untouched.
        Component authoredTitle = Component.literal(card.title())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        if (lines.isEmpty()) {
            lines.add(authoredTitle);
        } else {
            lines.set(0, authoredTitle);
        }

        lines.add(Component.empty());
        lines.add(Component.literal(card.noticeClass() + "  •  " + card.issuer())
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        for (String line : BountyStoryTooltip.wrap(card.body(), 46)) {
            lines.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }
        if (card.trust() > 0) {
            lines.add(Component.literal("Village Trust " + card.trust() + "+")
                .withStyle(ChatFormatting.DARK_GREEN));
        }
        cir.setReturnValue(lines);
    }
}
