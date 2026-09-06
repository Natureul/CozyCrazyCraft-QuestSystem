package com.natureul.cozycrazyquests.mixin;

import com.natureul.cozycrazyquests.BountifulBridge;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
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

    /**
     * Kotlin's lazy `val stack` compiles to getStack(). Bountiful already holds the exact world
     * and board position in this creator, so stamp the finished item as soon as it exists. This
     * occurs before the board copies the bounty into player-facing inventories, keeping Bountiful's
     * own per-player taken-mask comparison coherent.
     */
    @Inject(method = "getStack", at = @At("RETURN"), remap = false, require = 0)
    private void cozycrazyquests$stampIssuingBoard(CallbackInfoReturnable<ItemStack> cir) {
        BountifulBridge.stampCreatorSource(this, cir.getReturnValue());
    }

    /**
     * Bountiful 6.0.4 randomly creates one OR two unrelated objectives for a bounty.
     * That is fine for generic Bountiful, but it produced nonsense such as
     * "Wool for the Loom" + a skeleton hunt on the same notice. CozyCrazyCraft
     * uses one coherent objective per ordinary notice. Authored multi-step
     * contracts are created deliberately by our quest layer instead of by this
     * random split.
     */
    @ModifyConstant(method = "genObjectives", constant = @Constant(intValue = 2), remap = false, require = 0)
    private int cozycrazyquests$ordinaryNoticesUseOneObjective(int original) {
        return 1;
    }
}
