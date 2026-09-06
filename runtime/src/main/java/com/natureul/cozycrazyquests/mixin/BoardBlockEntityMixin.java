package com.natureul.cozycrazyquests.mixin;

import com.natureul.cozycrazyquests.BountifulBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.ejekta.bountiful.content.board.BoardBlockEntity", remap = false)
public abstract class BoardBlockEntityMixin {

    @Inject(method = "tryInitialPopulation", at = @At("HEAD"), remap = false, require = 0)
    private void cozycrazyquests$stampRegionalDecree(CallbackInfo ci) {
        BountifulBridge.stampRegionalDecreeIfEmpty(this);
    }

    @Inject(method = "randomlyUpdateBoard", at = @At("RETURN"), remap = false, require = 0)
    private void cozycrazyquests$boundBoardPopulation(CallbackInfo ci) {
        BountifulBridge.maintainNoticeCount(this);
    }
}
