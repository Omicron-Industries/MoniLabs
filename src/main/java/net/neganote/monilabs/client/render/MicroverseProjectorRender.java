package net.neganote.monilabs.client.render;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.textures.UnitTextureAtlasSprite;
import net.neganote.monilabs.common.machine.multiblock.Microverse;
import net.neganote.monilabs.common.machine.multiblock.MicroverseProjectorMachine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.Random;

// @SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class MicroverseProjectorRender extends
                                       DynamicRender<MicroverseProjectorMachine, MicroverseProjectorRender> {

    private long lastDegenerateColorSwapTime = 0;
    private long lastSpeedSwapTime1 = 0;
    private long corruptedSpeedSwapEndTimestamp1 = 0;
    private long lastSpeedSwapTime2 = 0;
    private long corruptedSpeedSwapEndTimestamp2 = 0;

    private float corruptedSpeedModifier = 1.0f;

    private static final Random RANDOM = new Random();
    private static final int[] originalDegenerateColors = { 0x55FFFF, 0x7C00BD, 0xD10000, 0x000000 };
    private static final int[] currentDegenerateColors = { 0x55FFFF, 0x7C00BD, 0xD10000, 0x000000 };
    public static final MicroverseProjectorRender INSTANCE = new MicroverseProjectorRender();

    // spotless:off
    public static final Codec<MicroverseProjectorRender> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<MicroverseProjectorMachine, MicroverseProjectorRender> TYPE = new DynamicRenderType<>(MicroverseProjectorRender.CODEC);
    // spotless:on

    public MicroverseProjectorRender() {}

    @Override
    public void render(MicroverseProjectorMachine projector, float partialTicks, @NotNull PoseStack stack,
                       @NotNull MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        var frontFacing = projector.getFrontFacing();
        var upwardsFacing = projector.getUpwardsFacing();

        Direction front = RelativeDirection.FRONT.getRelative(frontFacing, upwardsFacing, projector.isFlipped());

        Direction upwards = RelativeDirection.UP.getRelative(frontFacing, upwardsFacing, projector.isFlipped());

        Direction left = RelativeDirection.LEFT.getRelative(frontFacing, upwardsFacing, projector.isFlipped());

        int tier = projector.getProjectorTier();
        Microverse microverse = projector.getMicroverse();
        // RGB. Left -> right: further away from player -> closer
        Vector4i colors = switch (microverse) {
            case NONE -> new Vector4i(0, 0, 0, 0);
            case NORMAL -> new Vector4i(0xBDB3FF, 0x196E5B, 0x6B41E8, 0x20A37C);
            case HOSTILE -> new Vector4i(0xFF0000, 0xE3B4CA, 0xC18F9E, 0x19AA9E);
            case SHATTERED -> new Vector4i(0xE0EEFF, 0x2EA5CD, 0x20C094, 0x52FFFF);
            case CORRUPTED -> new Vector4i(0xFF00FF, 0xED84FF, 0x991187, 0x00000);
            // case ABYSSAL -> new Vector4i(0x078079, 0X3EB2B5, 0x375B59, 0x07373A);
            // case NECROSED -> new Vector4i(0xE2D75B, 0xDB745C, 0x5B3956, 0x28EEF5);
            // case SUPERCHARGED -> new Vector4i(0xFFEDA2, 0xF9E616, 0xC31307, 0xFF5423);
            // case DEGENERATE -> getDegenerateColors();
        };
        Vector4f particleSpeeds = switch (microverse) {
            case NONE -> new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
            case NORMAL -> new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
            case HOSTILE -> new Vector4f(3.0f, 0.5f, 1.5f, 1.0f);
            case SHATTERED -> new Vector4f(0.5f, 0.5f, 0.5f, 0.5f);
            case CORRUPTED -> getCorruptedSpeeds(new Vector4f(2.0f, 0.5f, 1.0f, 4.0f), partialTicks);
            // case ABYSSAL -> new Vector4f(1.5f, 0.25f, 0.75f, 0.5f);
            // case NECROSED -> new Vector4f(0.02f, 0.02f, 0.02f, 0.02f);
            // case SUPERCHARGED -> getSuperchargedSpeeds(1);
            // case DEGENERATE -> getCorruptedSpeeds(new Vector4f(4f, 5f, 3f, 4f), partialTicks);
        };
        int baseColor = switch (microverse) {
            case CORRUPTED /* DEGENERATE */ -> 0x150A14;
            default -> 0x06191C;
        };
        renderMicroverse(stack, buffer, upwards, front, left, combinedLight, tier, baseColor, colors, particleSpeeds);
    }

    private Vector4f getSuperchargedSpeeds(float multiplier) {
        return new Vector4f(5f * multiplier, 4f * multiplier, 3f * multiplier, 2f * multiplier);
    }

    private Vector4f getCorruptedSpeeds(Vector4f orig, float frameTime) {
        long timeStamp = System.currentTimeMillis();
        if (timeStamp - lastSpeedSwapTime1 > RANDOM.nextLong(2500, 2900)) {
            lastSpeedSwapTime1 = timeStamp;
            corruptedSpeedSwapEndTimestamp1 = timeStamp + 150;
            corruptedSpeedModifier = 1;
        }
        if (timeStamp - lastSpeedSwapTime2 > RANDOM.nextLong(3700, 6000)) {
            lastSpeedSwapTime2 = timeStamp;
            corruptedSpeedSwapEndTimestamp2 = timeStamp + 250;
            corruptedSpeedModifier = 1;
        }
        if ((corruptedSpeedSwapEndTimestamp1 > timeStamp || corruptedSpeedSwapEndTimestamp1 == 0) ||
                corruptedSpeedSwapEndTimestamp2 > timeStamp || corruptedSpeedSwapEndTimestamp2 == 0) {
            corruptedSpeedModifier -= 0.003f * frameTime;
            orig.x *= corruptedSpeedModifier;
            orig.y *= corruptedSpeedModifier;
            orig.z *= corruptedSpeedModifier;
            orig.w *= corruptedSpeedModifier;
        }

        return orig;
    }

    private Vector4i getDegenerateColors() {
        long timeStamp = System.currentTimeMillis();
        if (timeStamp - lastDegenerateColorSwapTime > RANDOM.nextLong(1900, 2200)) {
            lastDegenerateColorSwapTime = timeStamp;
            int i1 = RANDOM.nextInt(originalDegenerateColors.length);
            int i2 = RANDOM.nextInt(originalDegenerateColors.length);
            while (i1 == i2)
                i1 = RANDOM.nextInt(originalDegenerateColors.length);
            int temp = originalDegenerateColors[i1];
            currentDegenerateColors[i1] = currentDegenerateColors[i2];
            currentDegenerateColors[i2] = temp;
        }

        return new Vector4i(currentDegenerateColors[0], currentDegenerateColors[1], currentDegenerateColors[2],
                currentDegenerateColors[3]);
    }

    private void renderMicroverse(PoseStack stack, MultiBufferSource buffer, Direction upwards, Direction front,
                                  Direction left, int combinedLight, int tier, int baseColor, Vector4i colors,
                                  Vector4f particleSpeeds) {
        switch (tier) {
            case 1:
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -1, 1,
                        1.002f, 1.002f, 1.002f, baseColor, colors, particleSpeeds);
                break;
            case 2:
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -2, 2,
                        3.002f, 3.002f, 3.002f, baseColor, colors, particleSpeeds);
                break;
            case 3:
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -4, 2,
                        5.002f, 7.002f, 5.002f, baseColor, colors, particleSpeeds);
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -4, 2,
                        7.002f, 5.002f, 5.002f, baseColor, colors, particleSpeeds);
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -4, 2,
                        5.002f, 5.002f, 7.002f, baseColor, colors, particleSpeeds);
                break;
            case 4:
                // Lower square
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -5, 1,
                        3.002f, 1.002f, 5.002f, baseColor, colors, particleSpeeds);
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -5, 1,
                        5.002f, 1.002f, 3.002f, baseColor, colors, particleSpeeds);

                // Middle tube
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -5, 5,
                        1.002f, 7.002f, 1.002f, baseColor, colors, particleSpeeds);

                // Upper square
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -5, 9,
                        3.002f, 1.002f, 5.002f, baseColor, colors, particleSpeeds);
                renderCuboid(stack, buffer, upwards, front, left, combinedLight, -5, 9,
                        5.002f, 1.002f, 3.002f, baseColor, colors, particleSpeeds);
                break;
        }
    }

    private void renderCuboid(PoseStack stack, MultiBufferSource buffer, Direction upwards, Direction front,
                              Direction left, int combinedLight, int offsetFront, int offsetUp,
                              float scaleFactorFB, float scaleFactorUD, float scaleFactorLR,
                              int baseColor, Vector4i colors, Vector4f particleSpeeds) {
        stack.pushPose();
        var upwardsNormal = upwards.getNormal();
        var frontNormal = front.getNormal();
        var leftNormal = left.getNormal();

        // Calculate offset
        Vec3i movement = upwardsNormal.multiply(offsetUp);
        movement = movement.offset(frontNormal.multiply(offsetFront));

        // Calculate scaling factors
        float scaleFactorX = 0.0f;
        float scaleFactorY = 0.0f;
        float scaleFactorZ = 0.0f;

        if (leftNormal.getX() != 0) {
            scaleFactorX = scaleFactorLR;
        } else if (leftNormal.getY() != 0) {
            // noinspection
            scaleFactorY = scaleFactorLR;
        } else if (leftNormal.getZ() != 0) {
            scaleFactorZ = scaleFactorLR;
        }

        if (frontNormal.getX() != 0) {
            scaleFactorX = scaleFactorFB;
        } else if (frontNormal.getY() != 0) {
            scaleFactorY = scaleFactorFB;
        } else if (frontNormal.getZ() != 0) {
            scaleFactorZ = scaleFactorFB;
        }

        if (upwardsNormal.getX() != 0) {
            scaleFactorX = scaleFactorUD;
        } else if (upwardsNormal.getY() != 0) {
            scaleFactorY = scaleFactorUD;
        } else if (upwardsNormal.getZ() != 0) {
            scaleFactorZ = scaleFactorUD;
        }

        float minX = movement.getX() + 0.5f - (scaleFactorX / 2.0f);
        float minY = movement.getY() + 0.5f - (scaleFactorY / 2.0f);
        float minZ = movement.getZ() + 0.5f - (scaleFactorZ / 2.0f);

        float maxX = movement.getX() + 0.5f + (scaleFactorX / 2.0f);
        float maxY = movement.getY() + 0.5f + (scaleFactorY / 2.0f);
        float maxZ = movement.getZ() + 0.5f + (scaleFactorZ / 2.0f);

        PoseStack.Pose pose = stack.last();

        // Send buffer data, clean up
        VertexConsumer consumer;
        if (GTCEu.isModLoaded(GTValues.MODID_OCULUS) && Iris.getCurrentPack().isPresent()) {
            consumer = buffer.getBuffer(MoniRenderTypes.END_PORTAL_COLORED_IRIS);
        } else {
            consumer = buffer.getBuffer(MoniRenderTypes.END_PORTAL_COLORED);
        }
        Iris.getCurrentPack().ifPresent(pack -> {
            Object2IntMap<BlockState> stateIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();
            if (stateIds != null) {
                CapturedRenderingState.INSTANCE
                        .setCurrentBlockEntity(stateIds.getOrDefault(Blocks.END_PORTAL.defaultBlockState(), -1));
                CapturedRenderingState.INSTANCE.setCurrentEntity(6767); // Should be unclaimed hopefully :)
            }
        });

        RenderBufferHelper.renderCube(consumer, pose, baseColor, colors,
                particleSpeeds,
                combinedLight, UnitTextureAtlasSprite.INSTANCE,
                minX, minY, minZ, maxX, maxY, maxZ);
        stack.popPose();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox(@NotNull MicroverseProjectorMachine machine) {
        return new AABB(machine.getPos()).inflate(getViewDistance(), 16, getViewDistance());
    }

    @Override
    public @NotNull DynamicRenderType<MicroverseProjectorMachine, MicroverseProjectorRender> getType() {
        return TYPE;
    }

    @Override
    public boolean shouldRender(MicroverseProjectorMachine machine, @NotNull Vec3 cameraPos) {
        return machine.getMicroverse() != Microverse.NONE && machine.isFormed();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull MicroverseProjectorMachine machine) {
        return true;
    }
}
