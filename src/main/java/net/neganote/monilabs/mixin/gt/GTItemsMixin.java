package net.neganote.monilabs.mixin.gt;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = GTItems.class, remap = false)
public abstract class GTItemsMixin {

    @ModifyArg(method = "<clinit>",
               at = @At(value = "INVOKE",
                        target = "Lcom/tterrag/registrate/builders/ItemBuilder;properties(Lcom/tterrag/registrate/util/nullness/NonNullUnaryOperator;)Lcom/tterrag/registrate/builders/ItemBuilder;"),
               slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=turbine_rotor"),
                              to = @At(value = "FIELD",
                                       opcode = Opcodes.PUTSTATIC,
                                       target = "Lcom/gregtechceu/gtceu/common/data/GTItems;TURBINE_ROTOR:Lcom/tterrag/registrate/util/entry/ItemEntry;")),
               allow = 1)
    private static NonNullUnaryOperator<Item.Properties> modifyTurbineRotorProperties(NonNullUnaryOperator<Item.Properties> original) {
        return p -> original.apply(p).stacksTo(16);
    }
}
