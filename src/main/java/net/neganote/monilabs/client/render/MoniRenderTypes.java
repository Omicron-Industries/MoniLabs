package net.neganote.monilabs.client.render;

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
}
