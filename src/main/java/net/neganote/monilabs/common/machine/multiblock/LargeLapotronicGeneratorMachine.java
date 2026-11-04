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
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.network.chat.Component;
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
    @Persisted
    private boolean isManaBoosted = false;
    @Persisted
    private int runningTimer = 0;

    private static final FluidStack REDSTONE_STACK = GTMaterials.Redstone.getFluid(1);
    private static final FluidStack NORMAL_MANA_STACK = MoniMaterials.Mana.getFluid(2);
    private static final FluidStack EXTREME_MANA_STACK = MoniMaterials.Mana.getFluid(5);

    public LargeLapotronicGeneratorMachine(IMachineBlockEntity holder, int tier) {
        super(holder);
        this.tier = tier;
    }

    protected GTRecipe getLiquidRedstoneRecipe() {
        return GTRecipeBuilder.ofRaw().inputFluids(REDSTONE_STACK).buildRawRecipe();
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

        // check liquid redstone (1mb/3s)
        if (runningTimer % (20 * 3) == 0) {
            // insufficient liquid redstone
            if (!RecipeHelper.handleRecipeIO(this, getLiquidRedstoneRecipe(), IO.IN, this.recipeLogic.getChanceCaches())
                    .isSuccess()) {
                recipeLogic.interruptRecipe();
                return false;
            }
        }
        // check boost fluid
        if (this.isManaBoosted && runningTimer % (20 * 5) == 0) {
            var boosterRecipe = getManaRecipe();
            this.isManaBoosted = RecipeHelper.matchRecipe(this, boosterRecipe).isSuccess() &&
                    RecipeHelper.handleRecipeIO(this, boosterRecipe, IO.IN, this.recipeLogic.getChanceCaches())
                            .isSuccess();
            if (!this.isManaBoosted) {
                recipeLogic.interruptRecipe();
                return false;
            }
        }

        runningTimer++;
        if (runningTimer > 72000) runningTimer %= 72000; // reset once every hour of running

        return value;
    }

    public boolean hasLiquidRedstone() {
        return RecipeHelper.matchRecipe(this, this.getLiquidRedstoneRecipe())
                .isSuccess();
    }

    public static ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof LargeLapotronicGeneratorMachine lapotronicMachine)) {
            return RecipeModifier.nullWrongType(LargeLapotronicGeneratorMachine.class, machine);
        }

        EnergyStack EUt = recipe.getOutputEUt();
        var recipeVoltage = EUt.voltage();

        if (!lapotronicMachine.hasLiquidRedstone()) {
            return ModifierFunction
                    .cancel(Component.translatable("info.monilabs.lapotronic_generator.no_redstone_available"));
        }

        if (!EUt.isEmpty()) {
            lapotronicMachine.isManaBoosted = lapotronicMachine.isBoostAllowed() &&
                    RecipeHelper.matchRecipe(lapotronicMachine, lapotronicMachine.getManaRecipe()).isSuccess();
            double inputMultiplier = lapotronicMachine.isManaBoosted ? 2.0 : 1.0;
            double eutMultiplier = ((double) lapotronicMachine.getOverclockVoltage() / recipeVoltage) *
                    lapotronicMachine.getProductionBoost();
            double durationMultiplier = recipeVoltage / ((double) GTValues.V[lapotronicMachine.tier]);

            return ModifierFunction.builder()
                    .eutMultiplier(eutMultiplier)
                    .durationMultiplier(durationMultiplier)
                    .inputModifier(ContentModifier.multiplier(inputMultiplier))
                    .parallels(lapotronicMachine.isManaBoosted ? 2 : 1)
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
