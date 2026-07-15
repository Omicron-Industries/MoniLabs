package net.neganote.monilabs.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic.*;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neganote.monilabs.common.machine.multiblock.MicroverseProjectorMachine;
import net.neganote.monilabs.common.machine.multiblock.OmnicSynthesizerMachine;
import net.neganote.monilabs.common.machine.multiblock.SculkVatMachine;
import net.neganote.monilabs.config.MoniConfig;

import javax.annotation.ParametersAreNonnullByDefault;

@SuppressWarnings("unused")
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoniRecipeModifiers {

    public static ModifierFunction sculkVatRecipeModifier(MetaMachine machine, GTRecipe recipe) {
        if (machine instanceof SculkVatMachine sculkVat) {
            var tanks = sculkVat.getCapabilitiesFlat(IO.OUT, GTRecipeCapabilities.FLUID)
                    .stream()
                    .filter(NotifiableFluidTank.class::isInstance)
                    .map(NotifiableFluidTank.class::cast)
                    .toList();
            var stored = tanks.get(0).getFluidInTank(0).getAmount();
            var capacity = tanks.get(0).getTankCapacity(0);
            double x = (double) stored / capacity;
            double expMod = Math.log(MoniConfig.INSTANCE.values.sculkVatEfficiencyMultiplier) * 2.0;
            double modifier = Math.pow(1.0 / Math.exp(expMod * Math.pow((x - 0.5), 2.0)), 2.0);
            return ModifierFunction.builder()
                    .outputModifier(new ContentModifier(modifier, 0.0))
                    .build();
        } else {
            return RecipeModifier.nullWrongType(SculkVatMachine.class, machine);
        }
    }

    public static RecipeModifier omnicSynthRecipeModifier() {
        return (metaMachine, gtRecipe) -> {
            if (metaMachine instanceof OmnicSynthesizerMachine omnic) {
                Item item = RecipeHelper.getInputItems(gtRecipe).get(0).getItem();
                double multiplier;
                if (!omnic.recipeModifierCalculated) {
                    boolean found = true;
                    int index = omnic.diversityList.indexOf(item);
                    if (index < 0) {
                        found = false;
                        index = omnic.diversityList.size();
                    }

                    omnic.diversityPoints += (int) Math
                            .floor(Math.pow(index, MoniConfig.INSTANCE.values.omnicSynthesizerExponent));
                    multiplier = (double) omnic.diversityPoints / 100;
                    omnic.recipeModifierAmount = multiplier;
                    omnic.recipeModifierCalculated = true;
                    omnic.diversityPoints = omnic.diversityPoints % 100;

                    if (found) {
                        omnic.diversityList.remove(index);
                    }
                    omnic.diversityList.add(0, item);
                } else {
                    multiplier = omnic.recipeModifierAmount;
                }

                return ModifierFunction.builder()
                        .outputModifier(ContentModifier.multiplier(multiplier))
                        .build();
            }
            return RecipeModifier.nullWrongType(OmnicSynthesizerMachine.class, metaMachine);
        };
    }

    public static RecipeModifier MICROVERSE_OC = MoniRecipeModifiers::microverseOC;

    public static ModifierFunction microverseOC(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof MicroverseProjectorMachine projector)) {
            return RecipeModifier.nullWrongType(MicroverseProjectorMachine.class, machine);
        }
        if (RecipeHelper.getRecipeEUtTier(recipe) > projector.getTier()) {
            return ModifierFunction.cancel(Component.translatable("gtceu.recipe_modifier.insufficient_voltage"));
        }
        int projectorTier = projector.getProjectorTier();
        int recipeTier;
        if (recipe.data.contains("projector_tier")) {
            recipeTier = recipe.data.getByte("projector_tier");
        } else {
            recipeTier = 1;
        }
        int maxOCs = projector.getTier() - RecipeHelper.getRecipeEUtTier(recipe);
        OverclockingLogic logic = (p, v) -> microverseProjectorTierOC(p, v, projectorTier, recipeTier);
        return logic.getModifier(machine, recipe, projector.getOverclockVoltage());
    }

    // Heavily modeled after/copied from EBF OC logic but without subtick
    public static OCResult microverseProjectorTierOC(OCParams params, long maxVoltage, int projectorTier,
                                                     int recipeTier) {
        double duration = params.duration();
        double eut = params.eut();
        int ocAmount = params.ocAmount();

        double durationMultiplier = 1;

        int perfectOCAmount = projectorTier - recipeTier;

        int ocLevel = 0;
        while (ocAmount-- > 0) {
            boolean perfect = perfectOCAmount-- > 0;
            double potentialEUt = eut * OverclockingLogic.STD_VOLTAGE_FACTOR;
            if (potentialEUt > maxVoltage) break;
            double dFactor = perfect ? OverclockingLogic.PERFECT_DURATION_FACTOR :
                    OverclockingLogic.STD_DURATION_FACTOR;

            double potentialDuration = duration * dFactor;
            if (potentialDuration < 1) break;

            duration = potentialDuration;
            durationMultiplier *= dFactor;

            // Only set EUt after checking duration - no need to OC if duration would be too low
            eut = potentialEUt;
            ocLevel++;
        }

        return new OCResult(Math.pow(OverclockingLogic.STD_VOLTAGE_FACTOR, ocLevel), durationMultiplier, ocLevel, 1);
    }

    public static RecipeModifier MICROVERSE_PARALLEL_HATCH = MoniRecipeModifiers::hatchParallelMicroverse;

    // Identical to GTRecipeModifiers.hatchParallel, with the addition of the ability to blacklist parallels
    // on a per-recipe basis.
    public static ModifierFunction hatchParallelMicroverse(MetaMachine machine,
                                                           GTRecipe recipe) {
        if (recipe.data.contains("blacklistParallel") && recipe.data.getBoolean("blacklistParallel")) {
            return ModifierFunction.IDENTITY;
        }
        if (machine instanceof IMultiController controller && controller.isFormed()) {
            int parallels = controller.getParallelHatch()
                    .map(hatch -> ParallelLogic.getParallelAmount(machine, recipe, hatch.getCurrentParallel()))
                    .orElse(1);

            if (parallels == 1) return ModifierFunction.IDENTITY;
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .eutMultiplier(parallels)
                    .parallels(parallels)
                    .build();
        }
        return ModifierFunction.IDENTITY;
    }

    public static RecipeModifier OC_AS_PARALLELS = MoniRecipeModifiers::greenhouseOCasParallels;

    public static ModifierFunction greenhouseOCasParallels(MetaMachine machine, GTRecipe recipe) {
        var workableMachine = (WorkableElectricMultiblockMachine) machine;

        int recipeTier = GTUtil.getTierByVoltage(RecipeHelper.getRealEUt(recipe).getTotalEU());

        if (recipeTier > workableMachine.getTier()) {
            return ModifierFunction.cancel(Component.translatable("gtceu.recipe_modifier.insufficient_voltage"));
        }

        int ocTier = GTUtil.getOCTierByVoltage(workableMachine.getOverclockVoltage());

        int maxParallels = (int) Math.pow(2, ocTier - recipeTier);

        int parallels = ParallelLogic.getParallelAmount(machine, recipe, maxParallels);

        if (parallels == 1) return ModifierFunction.IDENTITY;

        var eutMultiplier = Math.pow(4, Math.ceil(Math.log(parallels) / Math.log(2)));

        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallels))
                .eutMultiplier(eutMultiplier)
                .parallels(parallels)
                .build();
    }
}
