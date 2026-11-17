package net.neganote.monilabs.client.render;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoniRenderTypes {

    public static RenderType MICROVERSE = RenderType.create("microverse",
            MoniVertexFormats.MICROVERSE, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> {
                        ShaderInstance shader = MoniShaders.MICROVERSE_SHADER;
                        shader.safeGetUniform("StarLayers").set(15);
                        return shader;
                    }))
                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                            .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                            .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                            .add(MicroverseRenderHelper.COSMIC_WEB_LOCATION, true, false)
                            .build())
                    .createCompositeState(false));

    public static RenderType MICROVERSE_IRIS = RenderType.create("microverse_iris",
            MoniVertexFormats.MICROVERSE_IRIS, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> {
                        var pipeline = Iris.getPipelineManager().getPipeline();
                        if (pipeline.isEmpty())
                            throw new RuntimeException("Expected iris rendering pipeline to be not null!!");
                        return ((IrisRenderingPipeline) pipeline.get()).getShaderMap()
                                .getShader(ShaderKey.BLOCK_ENTITY_DIFFUSE);
                    }))
                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                            .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                            .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                            .add(MicroverseRenderHelper.COSMIC_WEB_LOCATION, true, false)
                            .build())
                    .createCompositeState(false));
}
