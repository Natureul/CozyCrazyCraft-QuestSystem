package com.natureul.cozycrazyquests.mixin;

import com.natureul.cozycrazyquests.BountifulBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "io.ejekta.bountiful.content.BountyCreator", remap = false)
public abstract class BountyCreatorMixin {

    @Inject(method = "getAllPossibleObjectives", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void cozycrazyquests$honorObjectiveTrustRequirements(List<?> rewardPools, CallbackInfoReturnable<List<?>> cir) {
        List<?> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return;

        int reputation = BountifulBridge.creatorReputation(this);
        List<Object> filtered = new ArrayList<>(original.size());
        for (Object entry : original) {
            if (reputation >= BountifulBridge.objectiveRepRequired(entry)) {
                filtered.add(entry);
            }
        }
        cir.setReturnValue(filtered);
    }
}
