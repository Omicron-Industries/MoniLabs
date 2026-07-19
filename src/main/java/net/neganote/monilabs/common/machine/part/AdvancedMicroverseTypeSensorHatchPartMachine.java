package net.neganote.monilabs.common.machine.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;

import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neganote.monilabs.common.gui.widget.IndexedSelectorWidget;
import net.neganote.monilabs.common.machine.multiblock.Microverse;
import net.neganote.monilabs.common.machine.multiblock.MicroverseProjectorMachine;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedMicroverseTypeSensorHatchPartMachine extends MicroverseTypeSensorHatchPartMachine
                                                          implements IFancyUIMachine {

    @Setter
    @Getter
    @Persisted
    @DescSynced
    public Microverse detectorMicroverse = Microverse.NONE;

    @Setter
    @Getter
    @Persisted
    @DescSynced
    public boolean inverted;

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedMicroverseTypeSensorHatchPartMachine.class,
            MicroverseTypeSensorHatchPartMachine.MANAGED_FIELD_HOLDER);

    public AdvancedMicroverseTypeSensorHatchPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    @Override
    public int getOutputSignal(@Nullable Direction direction) {
        if (direction == getFrontFacing().getOpposite()) {
            var controller = (MicroverseProjectorMachine) getController();

            if (controller == null) {
                return 0;
            }

            var projectorMicroverse = controller.getMicroverse();
            if (inverted) {
                return projectorMicroverse == detectorMicroverse ? 0 : 15;
            } else {
                return projectorMicroverse == detectorMicroverse ? 15 : 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public Widget createUIWidget() {
        List<String> displayNames = Arrays.stream(Microverse.values())
                .map(Microverse::getDisplayName)
                .toList();

        WidgetGroup group = new WidgetGroup(0, 0, 70, 70);
        group.addWidget(new LabelWidget(-40, 15, "gui.advanced_chroma_sensor.display"));

        group.addWidget(new IndexedSelectorWidget(
                -5, 11, 80, 20,
                displayNames, 0)
                .setMaxCount(3)
                .setOnChanged(selectedName -> {
                    int idx = displayNames.indexOf(selectedName);
                    if (idx >= 0) setDetectorMicroverse(Microverse.values()[idx]);
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setSupplier(() -> getDetectorMicroverse().getDisplayName()));

        group.addWidget(new ToggleButtonWidget(
                80, 11, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted)
                .isMultiLang()
                .setTooltipText("gui.advanced_type_sensor.invert"));

        return group;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
