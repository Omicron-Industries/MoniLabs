package net.neganote.monilabs.mixin.blackhole;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.irisshaders.iris.Iris;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.neganote.monilabs.client.render.BlackHoleRenderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class TranslucentRenderMixin {

    @WrapOperation(
                   method = "renderLevel",
                   at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderChunkLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V"))
    private void moniLabs$wrapSecondRenderChunkLayer(
                                                     LevelRenderer instance,
                                                     RenderType renderType,
                                                     PoseStack poseStack,
                                                     double camX,
                                                     double camY,
                                                     double camZ,
                                                     Matrix4f projectionMatrix,
                                                     Operation<Void> original,
                                                     @Local(argsOnly = true) Camera camera) {
        boolean hasIrisPacks = GTCEu.isModLoaded(GTValues.MODID_OCULUS) && Iris.getCurrentPack().isPresent();

        if (renderType == RenderType.translucent() && !hasIrisPacks &&
                BlackHoleRenderer.hasBlackHoles()) {
            BlackHoleRenderer.preTranslucentPass(instance, renderType, poseStack, camX, camY, camZ, projectionMatrix,
                    original,
                    camera);

        } else {
            original.call(instance, renderType, poseStack, camX, camY, camZ, projectionMatrix);
        }
    }
}
