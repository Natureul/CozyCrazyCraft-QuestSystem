package com.natureul.cozycrazyquests.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The DI 1.20.1 source exposes a petstoreVillageWeight config, but its actual village
 * registration path hard-codes weight 17. Cancel that tiny registration method instead
 * of trying to rewrite generated village structures after the fact.
 */
@Pseudo
@Mixin(targets = "com.github.alexthe668.domesticationinnovation.server.misc.DIVillagePieceRegistry", remap = false)
public abstract class PetshopVillageMixin {

    @Inject(method = "registerHouses", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void cozycrazyquests$disablePetshopVillagePieces(CallbackInfo ci) {
        ci.cancel();
    }
}
