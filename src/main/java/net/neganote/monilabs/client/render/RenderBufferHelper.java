package net.neganote.monilabs.client.render;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4i;

// Direct copy from the gt class with some changes
@OnlyIn(Dist.CLIENT)
public class RenderBufferHelper {

    public static void renderCube(VertexConsumer buffer, PoseStack.Pose pose,
                                  int baseColor, Vector4i color, Vector4f speeds, int combinedLight,
                                  TextureAtlasSprite sprite,
                                  float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.UP, minX, maxY, minZ, uMin,
                vMax, minX, maxY, maxZ, uMax, vMax, maxX, maxY, maxZ, uMax, vMin, maxX, maxY, minZ, uMin, vMin);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.DOWN, minX, minY, minZ, uMin,
                vMax, maxX, minY, minZ, uMax, vMax, maxX, minY, maxZ, uMax, vMin, minX, minY, maxZ, uMin, vMin);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.NORTH, minX, minY, minZ, uMin,
                vMax, minX, maxY, minZ, uMax, vMax, maxX, maxY, minZ, uMax, vMin, maxX, minY, minZ, uMin, vMin);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.SOUTH, minX, minY, maxZ, uMin,
                vMax, maxX, minY, maxZ, uMax, vMax, maxX, maxY, maxZ, uMax, vMin, minX, maxY, maxZ, uMin, vMin);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.WEST, minX, minY, minZ, uMin,
                vMax, minX, minY, maxZ, uMax, vMax, minX, maxY, maxZ, uMax, vMin, minX, maxY, minZ, uMin, vMin);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.EAST, maxX, minY, minZ, uMin,
                vMax, maxX, maxY, minZ, uMax, vMax, maxX, maxY, maxZ, uMax, vMin, maxX, minY, maxZ, uMin, vMin);
    }

    public static void renderCubeFace(VertexConsumer buffer, PoseStack.Pose pose,
                                      int baseColor,
                                      Vector4i color, Vector4f speeds, int combinedLight, Direction normalDir,
                                      float x1, float y1, float z1,
                                      float u1, float v1,
                                      float x2, float y2, float z2,
                                      float u2, float v2,
                                      float x3, float y3, float z3,
                                      float u3, float v3,
                                      float x4, float y4, float z4,
                                      float u4, float v4) {
        Vector3fc normal = GTMatrixUtils.getDirectionAxis(normalDir);

        vertex(buffer, pose, x1, y1, z1, baseColor, u1, v1, normal, color, speeds);
        vertex(buffer, pose, x2, y2, z2, baseColor, u2, v2, normal, color, speeds);
        vertex(buffer, pose, x3, y3, z3, baseColor, u3, v3, normal, color, speeds);
        vertex(buffer, pose, x4, y4, z4, baseColor, u4, v4, normal, color, speeds);
    }

    public static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                              float x, float y, float z, int baseColor, float texU, float texV, Vector3fc normal,
                              Vector4i layerColors, Vector4f speeds) {
        BufferVertexConsumer consumer = (BufferVertexConsumer) buffer;
        buffer.vertex(pose.pose(), x, y, z);
        buffer.color(baseColor | 0xFF000000);
        if (GTCEu.isModLoaded(GTValues.MODID_OCULUS) && Iris.getCurrentPack().isPresent()) {
            buffer.uv(texU, texV); // uv0
            buffer.uv(0, 0); // uv1
            buffer.uv(0, 0); // uv2

            buffer.normal(pose.normal(), normal.x(), normal.y(), normal.z());
            // iris_Entity
            ((BufferVertexConsumer) buffer).putShort(0, (short) 6767);
            ((BufferVertexConsumer) buffer).putShort(2,
                    (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity());
            ((BufferVertexConsumer) buffer).putShort(4, (short) 0);
            consumer.nextElement();

            // mc_midTexCoord
            consumer.putFloat(0, 0);
            consumer.putFloat(4, 0);
            consumer.nextElement();

            // at_tangent
            consumer.putByte(0, (byte) 0);
            consumer.putByte(1, (byte) 0);
            consumer.putByte(2, (byte) 0);
            consumer.putByte(3, (byte) 0);
            consumer.nextElement();
        }
        consumer.putFloat(0, speeds.x);
        consumer.putFloat(4, speeds.y);
        consumer.putFloat(8, speeds.z);
        consumer.putFloat(12, speeds.w);
        consumer.nextElement();

        int[] colors = { layerColors.x, layerColors.y, layerColors.z, layerColors.w };
        for (int i = 0; i < 4; i++) {
            int c = colors[i];
            int offset = i * 4;

            consumer.putByte(offset + 0, (byte) (c & 0xFF));// Blue
            consumer.putByte(offset + 1, (byte) ((c >> 8) & 0xFF));// Green
            consumer.putByte(offset + 2, (byte) ((c >> 16) & 0xFF));// Red

            consumer.putByte(offset + 3, (byte) 0xFF);// Alpha
        }
        consumer.nextElement();
        buffer.endVertex();
    }
}
