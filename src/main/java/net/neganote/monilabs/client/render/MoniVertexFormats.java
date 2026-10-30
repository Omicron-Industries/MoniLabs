package net.neganote.monilabs.client.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoniVertexFormats {

    public static VertexFormat MICROVERSE = new VertexFormat(
            ImmutableMap.<String, VertexFormatElement>builder()
                    .put("Position", DefaultVertexFormat.ELEMENT_POSITION)
                    .put("BaseColor", DefaultVertexFormat.ELEMENT_COLOR)
                    .put("ParticleSpeeds",
                            new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
                    .put("Colors",
                            new VertexFormatElement(0, VertexFormatElement.Type.UINT, VertexFormatElement.Usage.UV, 4))
                    .put("Anchor",
                            new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
                    .put("Integrity",
                            new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 1))
                    .put("RenderEffect",
                            new VertexFormatElement(0, VertexFormatElement.Type.UBYTE,
                                    VertexFormatElement.Usage.GENERIC, 1))
                    .build());

    public static VertexFormat MICROVERSE_IRIS = new VertexFormat(
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
                    .put("Anchor",
                            new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 4))
                    .put("Integrity",
                            new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 1))
                    .put("RenderEffect",
                            new VertexFormatElement(0, VertexFormatElement.Type.UBYTE,
                                    VertexFormatElement.Usage.GENERIC, 1))
                    .build());
}
