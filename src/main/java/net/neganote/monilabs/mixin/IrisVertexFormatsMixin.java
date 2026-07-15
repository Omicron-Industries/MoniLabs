package net.neganote.monilabs.mixin;

import net.irisshaders.iris.vertices.IrisVertexFormats;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = IrisVertexFormats.class, remap = false)
public class IrisVertexFormatsMixin {

    @ModifyReceiver(
                    method = "<clinit>",
                    at = @At(
                             value = "INVOKE",
                             target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;",
                             ordinal = 1))
    private static ImmutableMap.Builder<String, VertexFormatElement> moniLabs$modifyEntityElements(ImmutableMap.Builder<String, VertexFormatElement> builder) {
        builder.put("ParticleSpeeds",
                new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.PADDING, 4));
        builder.put("Colors",
                new VertexFormatElement(0, VertexFormatElement.Type.UINT, VertexFormatElement.Usage.PADDING, 4));
        return builder;
    }
}
