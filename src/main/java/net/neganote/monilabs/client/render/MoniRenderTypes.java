package net.neganote.monilabs.client.render;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoniRenderTypes {

    public static VertexFormat POSITION_VEC4_COLOR_PACKEDVEC4UINTS = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                    .put("BaseColor", DefaultVertexFormat.ELEMENT_COLOR)
                    .put("ParticleSpeeds",
                            new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
                    .put("Colors",
                            new VertexFormatElement(0, VertexFormatElement.Type.UINT, VertexFormatElement.Usage.UV, 4))
                    .build());

    public static VertexFormat POSITION_VEC4_COLOR_PACKEDVEC4UINTS_IRIS = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                    .put("BaseColor", DefaultVertexFormat.ELEMENT_COLOR)
                    .put("iris_UV0", DefaultVertexFormat.ELEMENT_UV)
                    .put("iris_UV1", DefaultVertexFormat.ELEMENT_UV)
                    .put("iris_UV2", DefaultVertexFormat.ELEMENT_UV)
                    .put("iris_Normal", DefaultVertexFormat.ELEMENT_NORMAL)
                    .put("iris_Padding", DefaultVertexFormat.ELEMENT_PADDING)
                    .put("iris_Entity",
                            new VertexFormatElement(11, VertexFormatElement.Type.USHORT, VertexFormatElement.Usage.UV,
                                    3))
                    .put("mc_midTexCoord",
                            new VertexFormatElement(12, VertexFormatElement.Type.FLOAT,
                                    VertexFormatElement.Usage.GENERIC, 2))
                    .put("at_tangent",
                            new VertexFormatElement(13, VertexFormatElement.Type.BYTE,
                                    VertexFormatElement.Usage.GENERIC, 4))
                    .put("Padding2", DefaultVertexFormat.ELEMENT_PADDING)
                    .put("ParticleSpeeds",
                            new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
                    .put("Colors",
                            new VertexFormatElement(0, VertexFormatElement.Type.UINT, VertexFormatElement.Usage.UV, 4))
                    .build());

    public static RenderType END_PORTAL_COLORED = RenderType.create("end_portal_colored",
            POSITION_VEC4_COLOR_PACKEDVEC4UINTS, VertexFormat.Mode.QUADS, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(() -> {
                        ShaderInstance shader = MoniShaders.ENDPORTAL_COLORED_SHADER;
                        shader.safeGetUniform("EndPortalLayers").set(15);
                        return shader;
                    }))
                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                            .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                            .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false).build())
                    .createCompositeState(false));

    public static RenderType END_PORTAL_COLORED_IRIS = RenderType.create("end_portal_colored_iris",
            POSITION_VEC4_COLOR_PACKEDVEC4UINTS_IRIS, VertexFormat.Mode.QUADS, 256, false, false,
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
                            .build())
                    .createCompositeState(false));
}
