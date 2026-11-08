package net.neganote.monilabs.mixin.gt;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RotorHolderPartMachine.class, remap = false)
public abstract class RotorHolderPartMachineMixin {

    @Redirect(method = "<init>",
              at = @At(value = "NEW",
                       target = "(Lcom/gregtechceu/gtceu/api/machine/MetaMachine;ILcom/gregtechceu/gtceu/api/capability/recipe/IO;Lcom/gregtechceu/gtceu/api/capability/recipe/IO;)Lcom/gregtechceu/gtceu/api/machine/trait/NotifiableItemStackHandler;"))
    private NotifiableItemStackHandler redirectRotorHolderInventory(MetaMachine machine, int slots, IO handlerIO,
                                                                    IO capabilityIO) {
        return new NotifiableItemStackHandler(machine, slots, handlerIO, capabilityIO,
                inSlots -> new CustomItemStackHandler(inSlots) {

                    @Override
                    public int getSlotLimit(int slot) {
                        return 1;
                    }
                });
    }
}
