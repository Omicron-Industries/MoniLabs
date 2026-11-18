package net.neganote.monilabs.mixin.gt;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeCapabilities;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GTRecipeTypes.class, remap = false)
public class GTRecipeTypesMixin {

    @Shadow
    @Final
    public static GTRecipeType RESEARCH_STATION_RECIPES;

    @Definition(id = "SCANNER_RECIPES",
                field = "Lcom/gregtechceu/gtceu/common/data/GTRecipeTypes;SCANNER_RECIPES:Lcom/gregtechceu/gtceu/api/recipe/GTRecipeType;")
    @Expression("SCANNER_RECIPES = @(?)")
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private static GTRecipeType monilabs$addScannerRecipesToResearchStation(GTRecipeType original) {
        return original.onRecipeBuild((builder, provider) -> {
            if (!builder.input.containsKey(GTRecipeCapabilities.FLUID)) {
                RESEARCH_STATION_RECIPES.copyFrom(builder)
                        .CWUt(1)
                        .totalCWU(200)
                        .save(provider);
            }
        });
    }
}
