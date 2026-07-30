package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.neganote.monilabs.saveddata.UniqueMultiblockSavedData;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// Copied from CosmicCore with some major changes (thank you Caitlynn!)
public class UniqueWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    public UniqueWorkableElectricMultiblockMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    // Used to make sure you cannot have more than one of this multiblock per player / team
    @Getter
    @SaveField
    public boolean isDuplicate = false;

    @Override
    public void formStructure(@NotNull String substructureName) {
        super.formStructure(substructureName);

        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getOwnerUUID();
            var multiblockId = getDefinition().getId().toString();
            var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);

            if (uniqueMultiblockMapping.hasData(owner, multiblockId)) {
                this.isDuplicate = !uniqueMultiblockMapping.isUnique(owner, multiblockId, getBlockPos());
                if (isDuplicate) recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
            } else uniqueMultiblockMapping.addMultiblock(owner, getDefinition().getId().toString(),
                    getBlockPos());

        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        if (getLevel() instanceof ServerLevel serverLevel) {
            var owner = getOwnerUUID();
            var uniqueMultiblockMapping = UniqueMultiblockSavedData.getOrCreate(serverLevel);
            uniqueMultiblockMapping.removeMultiblock(owner, getDefinition().getId().toString(),
                    getBlockPos());
        }
    }

    @Override
    public @NotNull List<IWidget> getWidgetsForDisplay(@NotNull PanelSyncManager syncManager) {
        BooleanSyncValue isDuplicate = new BooleanSyncValue(this::isDuplicate);
        List<IWidget> widgets = new ArrayList<>();
        widgets.add(
                Text.of(Component.translatable("monilabs.multiblock.duplicate.0")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)))
                        .asWidget()
                        .setEnabledIf(w -> isDuplicate.getBoolValue()));
        widgets.add(
                Text.of(Component.translatable("monilabs.multiblock.duplicate.1")
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)))
                        .asWidget()
                        .setEnabledIf(w -> isDuplicate.getBoolValue()));

        widgets.addAll(super.getWidgetsForDisplay(syncManager));
        return widgets;
    }
}
