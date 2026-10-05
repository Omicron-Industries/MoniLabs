package net.neganote.monilabs.mixin.blackhole;

import net.irisshaders.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.neganote.monilabs.client.render.BlackHoleRenderer;
import net.neganote.monilabs.mixin.accessor.ShaderChunkRendererAccessor;
import net.neganote.monilabs.mixin.accessor.TerrainRenderPassAccessor;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.MultiDrawBatch;
import me.jellysquid.mods.sodium.client.gl.tessellation.GlTessellation;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.lwjgl.opengl.GL31;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DefaultChunkRenderer.class, remap = false)
public class DefaultChunkRendererMixin {

    @Shadow
    private static void executeDrawBatch(CommandList commandList, GlTessellation tessellation, MultiDrawBatch batch) {}

    @Redirect(
              method = "render",
              at = @At(
                       value = "INVOKE",
                       target = "Lme/jellysquid/mods/sodium/client/render/chunk/DefaultChunkRenderer;executeDrawBatch(Lme/jellysquid/mods/sodium/client/gl/device/CommandList;Lme/jellysquid/mods/sodium/client/gl/tessellation/GlTessellation;Lme/jellysquid/mods/sodium/client/gl/device/MultiDrawBatch;)V"))
    private void moniLabs$redirectExecuteDrawBatch(CommandList commandList, GlTessellation tessellation,
                                                   MultiDrawBatch batch,
                                                   @Local(argsOnly = true) TerrainRenderPass renderPass) {
        DefaultChunkRenderer instance = (DefaultChunkRenderer) (Object) this;
        if (((TerrainRenderPassAccessor) renderPass).getLayer() != RenderType.translucent() ||
                Iris.getCurrentPack().isPresent() || !BlackHoleRenderer.hasBlackHoles()) {
            // we do that because iris overrides getActiveProgram to null
            if (Iris.getCurrentPack().isEmpty())
                GL31.glUniform1i(
                        GL31.glGetUniformLocation(((ShaderChunkRendererAccessor) instance).getActiveProgram().handle(),
                                "uAnyBlackHoles"),
                        0);
            executeDrawBatch(commandList,
                    tessellation,
                    batch);
            return;
        }
        // Draw everything NOT in FRONT of black hole
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        GL31.glUniform1i(GL31.glGetUniformLocation(((ShaderChunkRendererAccessor) instance).getActiveProgram().handle(),
                "uDrawInFrontOfBlackHole"), 0);
        GL31.glUniform1i(GL31.glGetUniformLocation(((ShaderChunkRendererAccessor) instance).getActiveProgram().handle(),
                "uAnyBlackHoles"), 1);
        executeDrawBatch(commandList,
                tessellation,
                batch);

        // Somewhat unoptimized (but should be fine). Should be in the beginning of render method (the uniform and
        // glBindFramebuffer part)
        GL31.glUniform1i(GL31.glGetUniformLocation(((ShaderChunkRendererAccessor) instance).getActiveProgram().handle(),
                "uDrawInFrontOfBlackHole"), 1);
        GL31.glBindFramebuffer(GL31.GL_DRAW_FRAMEBUFFER, BlackHoleRenderer.miscTranslucentTexture.frameBufferId);
        RenderSystem.disableBlend();
        executeDrawBatch(commandList,
                tessellation,
                batch);
        RenderSystem.enableBlend();
    }
}
