package net.neganote.monilabs.mixin.gt;

import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeCombustionEngineMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LargeCombustionEngineMachine.class, remap = false)
public class LargeCombustionEngineMachineMixin {

    @Inject(method = "getProductionBoost()D",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/common/machine/multiblock/generator/LargeCombustionEngineMachine;isExtreme()Z"),
            cancellable = true)
    public void monilabs$ensureBoostAlwaysOnePointFive(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(1.5D);
    }
}
