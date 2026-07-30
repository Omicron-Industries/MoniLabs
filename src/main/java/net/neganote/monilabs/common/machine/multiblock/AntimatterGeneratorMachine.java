package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraftforge.fluids.FluidStack;
import net.neganote.monilabs.common.machine.trait.DummyRecipeLogic;
import net.neganote.monilabs.gtbridge.MoniRecipeTypes;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class AntimatterGeneratorMachine extends WorkableElectricMultiblockMachine {

    protected ConditionalSubscriptionHandler generationSubscription;

    public AntimatterGeneratorMachine(BlockEntityCreationInfo info) {
        super(info, new DummyRecipeLogic());
        this.generationSubscription = new ConditionalSubscriptionHandler(this, this::generateEnergyTick,
                this::isFormed);
    }

    private void generateEnergyTick() {
        if (isWorkingEnabled() && isFormed()) {
            List<IFluidHandlerModifiable> hatches = getCapabilitiesFlat(IO.IN, FluidRecipeCapability.CAP)
                    .stream()
                    .filter(IFluidHandlerModifiable.class::isInstance)
                    .map(IFluidHandlerModifiable.class::cast)
                    .toList();

            var recipe = MoniRecipeTypes.ANTIMATTER_COLLIDER_RECIPES.db().find(this);

            if (recipe == null) {
                voidFluids(hatches);
                return;
            }

            var maxParallels = ParallelLogic.getMaxByInput(this, recipe, Integer.MAX_VALUE, Collections.emptyList());

            double batchBonus = Math.log10(maxParallels) + 1.0;

            assert getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP).size() ==
                    1 : "There must be exactly 1 dynamo or laser source hatch on the Antimatter Generator";

            getCapabilitiesFlat(IO.OUT, EURecipeCapability.CAP).stream()
                    .filter(IEnergyContainer.class::isInstance)
                    .map(IEnergyContainer.class::cast)
                    .forEach(container -> container.addEnergy(
                            (long) (maxParallels * batchBonus * recipe.getOutputEUt().getTotalEU())));
            voidFluids(hatches);
        }
    }

    private void voidFluids(List<IFluidHandlerModifiable> hatches) {
        for (var hatch : hatches) {
            hatch.setFluidInTank(0, FluidStack.EMPTY);
        }
    }

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);
        generationSubscription.updateSubscription();
        setRenderState(getRenderState().setValue(RecipeLogic.STATUS_PROPERTY,
                isWorkingEnabled() ? RecipeLogic.Status.WORKING : RecipeLogic.Status.IDLE));
    }

    @Override
    public void invalidateStructure(String name) {
        super.invalidateStructure(name);
        generationSubscription.updateSubscription();
        setRenderState(getRenderState().setValue(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE));
    }
}
