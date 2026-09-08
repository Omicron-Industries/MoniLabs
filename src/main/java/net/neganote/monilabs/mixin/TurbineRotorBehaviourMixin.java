package net.neganote.monilabs.mixin;

import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TurbineRotorBehaviour.class, remap = false)
public class TurbineRotorBehaviourMixin {

    @Inject(method = "getRotorDurabilityPercent", at = @At(value = "HEAD"), cancellable = true)
    public void monilabs$rotorDuraPercent(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(100);
    }
}
