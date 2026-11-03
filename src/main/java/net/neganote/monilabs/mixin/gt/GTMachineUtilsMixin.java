package net.neganote.monilabs.mixin.gt;

import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = GTMachineUtils.class, remap = false)
public class GTMachineUtilsMixin {

    // While sloppa made this mixin, I know what it's doing. It's using the @Slice to find the exact location of the constant value 4L
    // to replace it with 3L, so I believe I could replicate it on my own if needed without further AI assistance.
    @ModifyConstant(method = "registerLargeCombustionEngine(Lcom/gregtechceu/gtceu/api/registry/registrate/GTRegistrate;Ljava/lang/String;ILjava/util/function/Supplier;Ljava/util/function/Supplier;Ljava/util/function/Supplier;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/resources/ResourceLocation;)Lcom/gregtechceu/gtceu/api/machine/MultiblockMachineDefinition;",
                    slice = @Slice(from = @At(value = "CONSTANT",
                                              args = "stringValue=gtceu.machine.large_combustion_engine.tooltip.boost_extreme"),
                                   to = @At(value = "CONSTANT",
                                            args = "stringValue=gtceu.machine.large_combustion_engine.tooltip.boost_regular")),
                    constant = @Constant(longValue = 4L))
    private static long monilabs$boostExtremeMultiplier(long original) {
        return 3L;
    }
}
