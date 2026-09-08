package net.neganote.monilabs.mixin;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.item.component.IMaterialPartItem;
import com.gregtechceu.gtceu.common.item.TurbineRotorBehaviour;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = IMaterialPartItem.class, remap = false)
public interface IMaterialPartItemMixin {

    /**
     * @author NegaNote
     * @reason Must remove durability tooltip on TurbineRotorBehaviours but cannot use injection because this
     *         targets an interface
     */
    @Overwrite
    default void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                 TooltipFlag isAdvanced) {
        Material material = this.getPartMaterial(stack);
        IMaterialPartItem current = (IMaterialPartItem) this;

        if (!(current instanceof TurbineRotorBehaviour)) {
            int maxDurability = this.getPartMaxDurability(stack);
            int damage = this.getPartDamage(stack);
            tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.durability",
                    maxDurability - damage, maxDurability));
        }

        tooltipComponents.add(Component.translatable("metaitem.tool.tooltip.primary_material",
                material.getLocalizedName()));
    }

    @Shadow
    int getPartDamage(ItemStack itemStack);

    @Shadow
    int getPartMaxDurability(ItemStack stack);

    @Shadow
    Material getPartMaterial(ItemStack itemStack);
}
