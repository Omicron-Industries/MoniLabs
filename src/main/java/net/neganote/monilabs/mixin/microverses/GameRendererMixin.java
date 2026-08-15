package net.neganote.monilabs.mixin.microverses;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.neganote.monilabs.client.render.BlackHoleRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 1)
public class GameRendererMixin {

    @Inject(method = "renderLevel",
            at = @At(
                     value = "INVOKE",
                     target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V",
                     shift = At.Shift.AFTER))
    public void moniLabs$renderBlackHoleWithShaders(float partialTicks, long finishTimeNano, PoseStack poseStack,
                                                    CallbackInfo ci) {
        boolean hasIrisPacks = GTCEu.isModLoaded(GTValues.MODID_OCULUS) && Iris.getCurrentPack().isPresent();
        if (hasIrisPacks && !ShadowRenderer.ACTIVE) {
            BlackHoleRenderer.renderWithShadersOn();
        }
    }
}
