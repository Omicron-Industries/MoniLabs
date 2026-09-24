package net.neganote.monilabs.mixin.enderio;

import net.neganote.monilabs.packmode.xpModeCost;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.enderio.machines.common.recipe.EnchanterRecipe", remap = false)
public abstract class EnchanterRecipeMixin {

    // Ender IO has a floor of 1 level minimum, this overrides that to 0
    @Inject(method = "getXPCostForLevel(I)I", at = @At("RETURN"), cancellable = true)
    private void monilabs$applyXpCostPolicy(int level, CallbackInfoReturnable<Integer> cir) {
        if (xpModeCost.isFree()) {
            cir.setReturnValue(0);
        }
    }
}
