package net.neganote.monilabs.mixin;

import com.gregtechceu.gtceu.api.item.component.IDurabilityBar;
import com.gregtechceu.gtceu.client.renderer.item.ToolChargeBarRenderer;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = IDurabilityBar.class, remap = false)
public interface IDurabilityBarMixin {

    /**
     * @author NegaNote
     * @reason must use overwrite to remove durability bar from turbine rotors because interfaces cannot use injections
     */
    @Overwrite
    default boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        IDurabilityBar self = (IDurabilityBar) this;
        if (!(self instanceof TurbineRotorBehaviour)) {
            return ToolChargeBarRenderer.renderDurabilityBar(guiGraphics, stack, self, xOffset, yOffset);
        }
        return true;
    }

    /**
     * @author NegaNote
     * @reason remove the vanilla durability bar as well, still cannot use injections in an interface
     */
    @Overwrite
    default boolean isBarVisible(ItemStack stack) {
        return !(this instanceof TurbineRotorBehaviour);
    }
}
