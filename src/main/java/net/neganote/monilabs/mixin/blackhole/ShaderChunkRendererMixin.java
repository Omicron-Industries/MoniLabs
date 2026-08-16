package net.neganote.monilabs.mixin.blackhole;

import net.minecraft.client.renderer.RenderType;
import net.neganote.monilabs.client.render.BlackHoleRenderer;
import net.neganote.monilabs.client.render.BlackHoleRendererHelpers;
import net.neganote.monilabs.mixin.accessor.TerrainRenderPassAccessor;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShaderChunkRenderer.class, remap = false)
public class ShaderChunkRendererMixin {

    @Shadow
    protected GlProgram<ChunkShaderInterface> activeProgram;

    @Inject(method = "createShader", at = @At("HEAD"), remap = false)
    private void moniLabs$saveShaderType(String path, ChunkShaderOptions options,
                                         CallbackInfoReturnable<GlProgram<ChunkShaderInterface>> cir) {
        BlackHoleRendererHelpers.isTranslucentShader = ((TerrainRenderPassAccessor) options.pass()).getLayer() ==
                RenderType.translucent();
    }

    @Inject(method = "begin", at = @At("TAIL"), remap = false)
    private void moniLabs$handleTranslucentPassBegin(TerrainRenderPass pass, CallbackInfo ci) {
        BlackHoleRenderer.handleTranslucentPassBegin(activeProgram.handle());
    }
}
