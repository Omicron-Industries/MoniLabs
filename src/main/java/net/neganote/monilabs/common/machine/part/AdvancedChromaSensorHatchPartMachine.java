package net.neganote.monilabs.common.machine.part;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;

import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neganote.monilabs.common.gui.widget.IndexedSelectorWidget;
import net.neganote.monilabs.common.machine.multiblock.Color;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static net.neganote.monilabs.common.machine.multiblock.Color.ACTUAL_COLORS;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedChromaSensorHatchPartMachine extends ChromaSensorHatchPartMachine
                                                  implements IFancyUIMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedChromaSensorHatchPartMachine.class,
            ChromaSensorHatchPartMachine.MANAGED_FIELD_HOLDER);

    @Setter
    @Getter
    @Persisted
    @DescSynced
    public Color detectorColor = Color.RED;

    @Setter
    @Getter
    @Persisted
    @DescSynced
    public boolean inverted = false;

    public AdvancedChromaSensorHatchPartMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return true;
    }

    @Override
    public int getOutputSignal(Direction direction) {
        if (direction == getFrontFacing().getOpposite()) {
            var prismacColor = getPrismacColor();
            if (prismacColor == null) {
                return 0;
            }
            if (inverted) {
                return prismacColor == detectorColor ? 0 : 15;
            } else {
                return prismacColor == detectorColor ? 15 : 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public Widget createUIWidget() {
        List<String> displayNames = Arrays.stream(ACTUAL_COLORS)
                .map(Color::getColoredDisplayName)
                .toList();

        WidgetGroup group = new WidgetGroup(0, 0, 70, 70);

        group.addWidget(new LabelWidget(-40, 15, "gui.monilabs.chroma.color.display"));

        group.addWidget(new IndexedSelectorWidget(
                -5, 11, 80, 20,
                displayNames,
                0)
                .setMaxCount(3)
                .setOnChanged(selectedName -> {
                    int idx = displayNames.indexOf(selectedName);
                    if (idx >= 0) setDetectorColor(ACTUAL_COLORS[idx]);
                })
                .setButtonBackground(ResourceBorderTexture.BUTTON_COMMON)
                .setSupplier(() -> getDetectorColor().getColoredDisplayName()));

        group.addWidget(new ToggleButtonWidget(
                80, 11, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, this::isInverted, this::setInverted)
                .isMultiLang()
                .setTooltipText("gui.advanced_chroma_sensor.invert"));

        return group;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
