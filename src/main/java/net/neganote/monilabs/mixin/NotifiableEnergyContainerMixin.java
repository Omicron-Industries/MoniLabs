package net.neganote.monilabs.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.server.level.ServerLevel;
import net.neganote.monilabs.common.machine.multiblock.CreativeEnergyMultiMachine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(value = NotifiableEnergyContainer.class, remap = false)
public class NotifiableEnergyContainerMixin extends NotifiableRecipeHandlerTrait<EnergyStack> {

    @Shadow
    protected @Nullable TickableSubscription outputSubs;

    public NotifiableEnergyContainerMixin(MetaMachine machine) {
        super(machine);
    }

    @Shadow
    public long getEnergyCapacity() {
        return 0;
    }

    @Shadow
    public void serverTick() {}

    @Override
    public MetaMachine getMachine() {
        return super.getMachine();
    }

    @Inject(method = "getEnergyStored()J", at = @At(value = "HEAD"), cancellable = true)
    private void monilabs$injectBeforeGetEnergyStored(CallbackInfoReturnable<Long> cir) {
        MetaMachine machine = getMachine();
        if (machine.getLevel() instanceof ServerLevel) {
            outputSubs = machine.subscribeServerTick(this.outputSubs, this::serverTick);
            UUID uuid = machine.getOwnerUUID();
            if (uuid != null && CreativeEnergyMultiMachine.isCreativeEnergyEnabledFor(uuid)) {
                notifyListeners();
                // return 1 less so active transformers won't turn off
                cir.setReturnValue(getEnergyCapacity() - 1);
            }
        }
    }

    // This injection is so that it doesn't try and modify the *actual* stored energy, which could easily cheese
    // the power substation and the like to be filled even after the boolean is set back to false.
    @Inject(method = "changeEnergy", at = @At(value = "HEAD"), cancellable = true)
    private void monilabs$injectBeforeChangeEnergy(long energyToAdd, CallbackInfoReturnable<Long> cir) {
        MetaMachine machine = getMachine();
        if (machine.getLevel() instanceof ServerLevel) {
            outputSubs = machine.subscribeServerTick(this.outputSubs, this::serverTick);
            UUID uuid = machine.getOwnerUUID();
            if (uuid != null && CreativeEnergyMultiMachine.isCreativeEnergyEnabledFor(uuid)) {
                notifyListeners();
                cir.setReturnValue(energyToAdd);
            }
        }
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return NotifiableEnergyContainer.MANAGED_FIELD_HOLDER;
    }

    @Override
    @Shadow
    public IO getHandlerIO() {
        return null;
    }

    @Override
    @Shadow
    public List<EnergyStack> handleRecipeInner(IO io, GTRecipe recipe, List left, boolean b) {
        return List.of();
    }

    @Override
    @Shadow
    public @NotNull List<Object> getContents() {
        return List.of();
    }

    @Override
    @Shadow
    public double getTotalContentAmount() {
        return 0;
    }

    @Override
    @Shadow
    public RecipeCapability<EnergyStack> getCapability() {
        return null;
    }
}
