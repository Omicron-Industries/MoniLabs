package net.neganote.monilabs.mixin;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IRotorHolderMachine.class, remap = false)
public interface RotorHolderMachineMixin {

    /**
     * @author NegaNote
     * @reason killing rotor damage, can't use an injection on an interface
     */
    @Overwrite
    default void damageRotor(int damageAmount) {}
}
