package net.neganote.monilabs.mixin.gt;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.PowerSubstationMachine;

import net.minecraft.server.level.ServerLevel;
import net.neganote.monilabs.common.machine.multiblock.CreativeEnergyMultiMachine;
import net.neganote.monilabs.common.machine.multiblock.UniqueWorkableElectricMultiblockMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = PowerSubstationMachine.class, remap = false)
public class PowerSubstationMachineMixin extends MetaMachine {

    public PowerSubstationMachineMixin(IMachineBlockEntity holder) {
        super(holder);
    }

    // Prevents substations from performing any power transfers while TES is running
    @Inject(method = "transferEnergyTick()V", at = @At(value = "HEAD"), cancellable = true)
    public void monilabs$injectBeforeTransferEnergyTick(CallbackInfo ci) {
        if (getLevel() instanceof ServerLevel) {
            UUID uuid = this.getOwnerUUID();
            var uniqueMachines = UniqueWorkableElectricMultiblockMachine.ACTIVE_OWNERS.get(uuid);
            if (uniqueMachines != null && uniqueMachines.get(CreativeEnergyMultiMachine.class) != null) {
                if (!(uniqueMachines.get(CreativeEnergyMultiMachine.class) instanceof CreativeEnergyMultiMachine cemm))
                    return;
                if (cemm.isProviding) {
                    ci.cancel();
                }
            }
        }
    }
}
