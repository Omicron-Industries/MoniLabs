package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neganote.monilabs.common.item.MoniItems;
import net.neganote.monilabs.common.machine.trait.NotifiableMicroverseContainer;
import net.neganote.monilabs.config.MoniConfig;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("unused")
public class MicroverseProjectorMachine extends WorkableElectricMultiblockMachine {

    private final ConditionalSubscriptionHandler microverseHandler;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MicroverseProjectorMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    private final Set<BlockPos> fluidBlockOffsets = new HashSet<>();

    // Used for microverse projector tier
    @Getter
    private final int projectorTier;

    // Microverse type currently active
    @Persisted
    @DescSynced
    @Setter
    @Getter
    @RequireRerender
    @UpdateListener(methodName = "onMicroverseChange")
    private Microverse microverse;

    // Current microverse integrity/"health"
    @Persisted
    @DescSynced
    @Getter
    private int microverseIntegrity;

    private List<NotifiableItemStackHandler> inputBuses = null;
    private List<NotifiableItemStackHandler> outputBuses = null;

    // Constant for max health. Takes 500s (8m20s) to decay at a rate of 1/tick
    public static final int MICROVERSE_MAX_INTEGRITY = 100_000;
    public static final int FLUX_REPAIR_AMOUNT = 1000;

    private final static GTRecipe quantumFluxRecipe = GTRecipeBuilder.ofRaw().inputItems(MoniItems.QUANTUM_FLUX)
            .buildRawRecipe();

    @Persisted
    private final NotifiableMicroverseContainer microverseContainer;

    public MicroverseProjectorMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, args);
        this.projectorTier = tier;
        this.microverseHandler = new ConditionalSubscriptionHandler(this, this::microverseTick, this::isFormed);
        updateMicroverse(0, false);
        this.microverseContainer = new NotifiableMicroverseContainer(this);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureInvalid() {
        if (microverse.decayRate != 0) {
            updateMicroverse(0, false);
        }
        super.onStructureInvalid();
        microverseHandler.updateSubscription();
        inputBuses = null;
        outputBuses = null;
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        microverseHandler.updateSubscription();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null) return false;
        if (microverseIntegrity == 0 && microverse != Microverse.NONE) return false;
        if (recipe.data.contains("projector_tier") && recipe.data.getLong("projector_tier") > projectorTier) {
            RecipeLogic.putFailureReason(this, recipe,
                    Component.translatable("monilabs.failure_reason.insufficient_projector_tier"));
            return false;
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {
        if (!super.onWorking()) {
            return false;
        }

        if ((outputBuses == null || outputBuses.isEmpty()) &&
                MoniConfig.INSTANCE.values.microminerReturnedOnZeroIntegrity) {
            outputBuses = getCapabilitiesFlat(IO.OUT, ItemRecipeCapability.CAP).stream()
                    .filter(NotifiableItemStackHandler.class::isInstance)
                    .map(NotifiableItemStackHandler.class::cast)
                    .toList();
        }

        var activeRecipe = recipeLogic.getLastRecipe();

        if (activeRecipe != null && activeRecipe.data.contains("damage_rate")) {
            int decayRate = activeRecipe.data.getInt("damage_rate");
            decayRate *= activeRecipe.parallels;

            var originalDuration = activeRecipe.data.getInt("duration");

            var durationDifference = originalDuration / activeRecipe.duration;
            decayRate *= durationDifference;

            microverseIntegrity = Math.min(Math.max(microverseIntegrity - decayRate, 0), MICROVERSE_MAX_INTEGRITY);
            if (microverseIntegrity == 0 && microverse != Microverse.NONE) {
                if (MoniConfig.INSTANCE.values.microminerReturnedOnZeroIntegrity) {
                    var contents = (Ingredient) activeRecipe.getInputContents(ItemRecipeCapability.CAP).get(0)
                            .getContent();
                    List<Ingredient> left = List.of(contents);
                    for (var outputBus : outputBuses) {
                        left = outputBus.handleRecipe(IO.OUT, activeRecipe, left, false);
                        if (left == null) {
                            break;
                        }
                    }
                }

                if (microverse == Microverse.SHATTERED) {
                    microverseIntegrity = MICROVERSE_MAX_INTEGRITY >> 1; // start at half integrity
                    microverse = Microverse.CORRUPTED;
                    markDirty();
                } else {
                    microverseIntegrity = 0;
                    microverse = Microverse.NONE;
                }
                recipeLogic.resetRecipeLogic();
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        var activeRecipe = recipeLogic.getLastRecipe();
        if (activeRecipe != null && activeRecipe.data.contains("updated_microverse")) {
            int updatedMicroverse = activeRecipe.data.getInt("updated_microverse");
            updateMicroverse(updatedMicroverse, activeRecipe.data.getBoolean("keep_integrity"));
        }
    }

    public void microverseTick() {
        if (inputBuses == null || inputBuses.isEmpty()) {
            inputBuses = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP).stream()
                    .filter(NotifiableItemStackHandler.class::isInstance)
                    .map(NotifiableItemStackHandler.class::cast)
                    .toList();
        }

        if (microverse.isRepairable) {
            var missingHealth = MICROVERSE_MAX_INTEGRITY - microverseIntegrity;
            var fluxToFullHeal = missingHealth / FLUX_REPAIR_AMOUNT;
            var fluxAvailable = ParallelLogic.getMaxByInput(this, quantumFluxRecipe, Integer.MAX_VALUE,
                    Collections.emptyList());

            var fluxToConsume = microverse.isHungry ? fluxAvailable : Math.min(fluxToFullHeal, fluxAvailable);

            if (fluxToConsume > 0) {
                List<Ingredient> fluxList = ObjectArrayList
                        .of(SizedIngredient.create(new ItemStack(MoniItems.QUANTUM_FLUX.get(), fluxToConsume)));

                var scaledRecipe = quantumFluxRecipe.copy(new ContentModifier(fluxToConsume, 0.0));

                for (var bus : inputBuses) {
                    fluxList = bus.handleRecipe(IO.IN, scaledRecipe, fluxList, false);
                    if (fluxList == null) break;
                }

                var usedToHeal = Math.min(fluxToFullHeal, fluxToConsume);
                microverseIntegrity += usedToHeal * FLUX_REPAIR_AMOUNT;

                if (microverse.isHungry && fluxToConsume > usedToHeal) {
                    int rollbackCount = fluxToConsume - usedToHeal;
                    if (recipeLogic.getLastRecipe() != null && recipeLogic.getProgress() > 1) {
                        recipeLogic.setProgress(Math.max(1, recipeLogic.getProgress() - (20 * rollbackCount)));
                    }
                }
            }
        }
        if (microverse.decayRate != 0) {
            int decayRate = microverse.decayRate;
            microverseIntegrity -= decayRate;
            if (microverseIntegrity <= 0) {
                updateMicroverse(0, false);
            }
        }
    }

    private void updateMicroverse(int pKey, boolean keepIntegrity) {
        microverse = Microverse.getMicroverseFromKey(pKey);
        if (microverse == Microverse.NONE) {
            microverseIntegrity = 0;
        } else {
            microverseIntegrity = (keepIntegrity ? microverseIntegrity : MICROVERSE_MAX_INTEGRITY);
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            textList.add(Component.translatable("microverse.monilabs.current_microverse",
                    Component.translatable(microverse.langKey)));
            if (microverse != Microverse.NONE) {
                textList.add(Component.translatable("microverse.monilabs.integrity",
                        (float) microverseIntegrity / FLUX_REPAIR_AMOUNT));
            }
        }
    }

    public void onMicroverseChange(Microverse oldMicroverse, Microverse newMicroverse) {
        scheduleRenderUpdate();
    }
}
