package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.EnergyStack;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeCombustionEngineMachine;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.neganote.monilabs.common.data.materials.MoniMaterials;
import net.neganote.monilabs.common.item.MoniItems;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

// Code heavily borrowed from LargeCombustionEngine
public class LargeLapotronicGeneratorMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    @Getter
    private final int tier;

    @DescSynced
    private boolean isManaBoosted = false;
    private int runningTimer = 0;

    private static final ItemStack QFLUX_STACK = new ItemStack(MoniItems.QUANTUM_FLUX, 1);
    private static final FluidStack NORMAL_MANA_STACK = MoniMaterials.Mana.getFluid(2);
    private static final FluidStack EXTREME_MANA_STACK = MoniMaterials.Mana.getFluid(5);

    public LargeLapotronicGeneratorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    protected GTRecipe getQuantumFluxRecipe() {
        return GTRecipeBuilder.ofRaw().inputItems(QFLUX_STACK).buildRawRecipe();
    }

    protected GTRecipe getManaRecipe() {
        return GTRecipeBuilder.ofRaw().inputFluids(isExtreme() ? EXTREME_MANA_STACK : NORMAL_MANA_STACK)
                .buildRawRecipe();
    }

    @Override
    public long getOverclockVoltage() {
        if (isManaBoosted) return GTValues.V[tier] * 2;
        else return GTValues.V[tier];
    }

    protected double getProductionBoost() {
        if (!isManaBoosted) return 1;
        return isExtreme() ? 2.0 : 1.5;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        // check lubricant

        if (runningTimer % (20 * 60 * 5) == 0) {
            // insufficient lubricant
            if (!RecipeHelper.handleRecipeIO(this, getQuantumFluxRecipe(), IO.IN, this.recipeLogic.getChanceCaches())
                    .isSuccess()) {
                recipeLogic.interruptRecipe();
                return false;
            }
        }
        // check boost fluid
        if (isBoostAllowed() && runningTimer % (20 * 5) == 0) {
            var boosterRecipe = getManaRecipe();
            this.isManaBoosted = RecipeHelper.matchRecipe(this, boosterRecipe).isSuccess() &&
                    RecipeHelper.handleRecipeIO(this, boosterRecipe, IO.IN, this.recipeLogic.getChanceCaches())
                            .isSuccess();
        }

        runningTimer++;
        if (runningTimer > 72000) runningTimer %= 72000; // reset once every hour of running

        return value;
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof LargeLapotronicGeneratorMachine lapotronicMachine)) {
            return RecipeModifier.nullWrongType(LargeCombustionEngineMachine.class, machine);
        }
        EnergyStack EUt = recipe.getOutputEUt();
        // has lubricant
        if (!EUt.isEmpty() &&
                RecipeHelper.matchRecipe(lapotronicMachine, lapotronicMachine.getQuantumFluxRecipe()).isSuccess()) {
            int maxParallel = (int) (lapotronicMachine.getOverclockVoltage() / EUt.getTotalEU()); // get maximum
                                                                                                  // parallel
            int actualParallel = ParallelLogic.getParallelAmount(lapotronicMachine, recipe, maxParallel);
            double eutMultiplier = actualParallel * lapotronicMachine.getProductionBoost();

            return ModifierFunction.builder()
                    .inputModifier(ContentModifier.multiplier(actualParallel))
                    .outputModifier(ContentModifier.multiplier(actualParallel))
                    .eutMultiplier(eutMultiplier)
                    .parallels(actualParallel)
                    .build();
        }
        return ModifierFunction.NULL;
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    private boolean isExtreme() {
        return getTier() > GTValues.EV;
    }

    public boolean isBoostAllowed() {
        return getMaxVoltage() >= GTValues.V[getTier() + 1];
    }
}
