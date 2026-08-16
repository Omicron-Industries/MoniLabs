package net.neganote.monilabs.mixin.gt;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.OpticalDataHatchMachine;

import net.minecraft.server.level.ServerLevel;
import net.neganote.monilabs.common.machine.multiblock.CreativeDataMultiMachine;
import net.neganote.monilabs.common.machine.multiblock.UniqueWorkableElectricMultiblockMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = OpticalDataHatchMachine.class, remap = false)
public class OpticalDataHatchMixin extends MetaMachine {

    public OpticalDataHatchMixin(IMachineBlockEntity holder) {
        super(holder);
    }

    @Inject(method = "isCreative", at = @At(value = "HEAD"), cancellable = true)
    public void beforeIsCreative(CallbackInfoReturnable<Boolean> cir) {
        if (getLevel() instanceof ServerLevel) {
            UUID uuid = this.getOwnerUUID();
            var uniqueMachines = UniqueWorkableElectricMultiblockMachine.ACTIVE_OWNERS.get(uuid);
            if (uniqueMachines != null && uniqueMachines.get(CreativeDataMultiMachine.class) != null) {
                if (!(uniqueMachines.get(CreativeDataMultiMachine.class) instanceof CreativeDataMultiMachine cdmm))
                    return;
                if (cdmm.isProviding) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
