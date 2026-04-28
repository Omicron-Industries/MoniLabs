package net.neganote.monilabs.client.render;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import net.irisshaders.iris.Iris;
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
        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.UP,
                minX, maxY, minZ,
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                maxX, maxY, minZ);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.DOWN,
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, minY, maxZ,
                minX, minY, maxZ);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.NORTH,
                minX, minY, minZ,
                minX, maxY, minZ,
                maxX, maxY, minZ,
                maxX, minY, minZ);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.SOUTH,
                minX, minY, maxZ,
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                minX, maxY, maxZ);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.WEST,
                minX, minY, minZ,
                minX, minY, maxZ,
                minX, maxY, maxZ,
                minX, maxY, minZ);

        renderCubeFace(buffer, pose, baseColor, color, speeds, combinedLight, Direction.EAST,
                maxX, minY, minZ,
                maxX, maxY, minZ,
                maxX, maxY, maxZ,
                maxX, minY, maxZ);
    }

    public static void renderCubeFace(VertexConsumer buffer, PoseStack.Pose pose,
                                      int baseColor,
                                      Vector4i color, Vector4f speeds, int combinedLight, Direction normalDir,
                                      float x1, float y1, float z1,
                                      float x2, float y2, float z2,
                                      float x3, float y3, float z3,
                                      float x4, float y4, float z4) {
        Vector3fc normal = GTMatrixUtils.getDirectionAxis(normalDir);

        vertex(buffer, pose, x1, y1, z1, baseColor, color, speeds);
        vertex(buffer, pose, x2, y2, z2, baseColor, color, speeds);
        vertex(buffer, pose, x3, y3, z3, baseColor, color, speeds);
        vertex(buffer, pose, x4, y4, z4, baseColor, color, speeds);
    }

    public static void vertex(VertexConsumer buffer, PoseStack.Pose pose,
                              float x, float y, float z, int baseColor, Vector4i layerColors, Vector4f speeds) {
        if (GTCEu.isModLoaded(GTValues.MODID_OCULUS) && Iris.getCurrentPack().isPresent()) {
            buffer.vertex(pose.pose(), x, y, z);
            buffer.color(baseColor | 0xFF000000);
            buffer.uv(0, 0);
            buffer.overlayCoords(0);
            buffer.uv2(0);
            buffer.normal(pose.normal(), 0, 0, 0);
            buffer.endVertex();
            return;
        }

        BufferVertexConsumer consumer = (BufferVertexConsumer) buffer;
        buffer.vertex(pose.pose(), x, y, z);
        buffer.color(baseColor);
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
