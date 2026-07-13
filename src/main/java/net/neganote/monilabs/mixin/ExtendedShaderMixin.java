package net.neganote.monilabs.mixin;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import org.lwjgl.opengl.GL31;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ExtendedShader.class, remap = false)
public class ExtendedShaderMixin {

    @ModifyReceiver(
                    method = "<init>",
                    at = @At(value = "INVOKE",
                             target = "Lnet/irisshaders/iris/gl/program/ProgramSamplers$Builder;build()Lnet/irisshaders/iris/gl/program/ProgramSamplers;")

    )
    private ProgramSamplers.Builder moniLabs$injectEndPortalSamplers(ProgramSamplers.Builder instance,
                                                                     ResourceProvider resourceFactory, String string) {
        if (!string.contains("block_entity_diffuse")) {
            return instance;
        }
        if (instance.hasSampler("_EndPortalTexture")) {
            var id = Minecraft.getInstance().getTextureManager().getTexture(TheEndPortalRenderer.END_PORTAL_LOCATION)
                    .getId();
            var sampler = new GlSampler(true, false, false, false);
            IrisRenderSystem.samplerParameteri(sampler.getId(), GL31.GL_TEXTURE_WRAP_S, GL31.GL_REPEAT);
            IrisRenderSystem.samplerParameteri(sampler.getId(), GL31.GL_TEXTURE_WRAP_T, GL31.GL_REPEAT);
            instance.addDynamicSampler(TextureType.TEXTURE_2D, () -> id, sampler, "_EndPortalTexture");
        }
        if (instance.hasSampler("_EndSkyTexture")) {
            var id = Minecraft.getInstance().getTextureManager().getTexture(TheEndPortalRenderer.END_SKY_LOCATION)
                    .getId();
            instance.addDynamicSampler(TextureType.TEXTURE_2D, () -> id, new GlSampler(true, false, false, false),
                    "_EndSkyTexture");
        }
        return instance;
    }
}
