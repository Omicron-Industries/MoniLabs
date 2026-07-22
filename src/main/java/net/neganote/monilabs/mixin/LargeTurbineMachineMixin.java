package net.neganote.monilabs.mixin;

import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = LargeTurbineMachine.class, remap = false)
public class LargeTurbineMachineMixin {

    @Inject(method = "addDisplayText(Ljava/util/List;)V",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/api/machine/feature/multiblock/IRotorHolderMachine;getRotorDurabilityPercent()I"),
            cancellable = true)
    public void monilabs$displayText(List<Component> textList, CallbackInfo ci) {
        ci.cancel();
    }
}
