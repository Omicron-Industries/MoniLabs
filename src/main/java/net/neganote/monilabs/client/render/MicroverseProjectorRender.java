package net.neganote.monilabs.client.render;

import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neganote.monilabs.common.machine.multiblock.Microverse;
import net.neganote.monilabs.common.machine.multiblock.MicroverseProjectorMachine;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.Random;

// @SuppressWarnings("unused")
@SideOnly(Side.CLIENT)
public class MicroverseProjectorRender extends
                                       DynamicRender<MicroverseProjectorMachine, MicroverseProjectorRender> {

    private long lastDegenerateColorSwapTime = 0;

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
            case CORRUPTED -> new Vector4f(2.0f, 0.5f, 1.0f, 4.0f);
            // case ABYSSAL -> new Vector4f(1.5f, 0.25f, 0.75f, 0.5f);
            // case NECROSED -> new Vector4f(0.02f, 0.02f, 0.02f, 0.02f);
            // case SUPERCHARGED -> getSuperchargedSpeeds(1);
            // case DEGENERATE -> getCorruptedSpeeds(new Vector4f(4f, 5f, 3f, 4f), partialTicks);
        };
        int baseColor = switch (microverse) {
            case CORRUPTED /* DEGENERATE */ -> 0x150A14;
            default -> 0x06191C;
        };
        int renderEffect = switch (microverse) {
            case NONE, HOSTILE, NORMAL -> MicroverseRenderHelper.RENDER_EFFECT_NONE;
            case SHATTERED -> MicroverseRenderHelper.RENDER_EFFECT_SHATTERED;
            case CORRUPTED -> MicroverseRenderHelper.RENDER_EFFECT_CORRUPTED;
        };
        float integrity = projector.getMicroverseIntegrity() /
                (float) MicroverseProjectorMachine.MICROVERSE_MAX_INTEGRITY;
        renderProjectInterior(stack, buffer, upwards, front, left, tier, baseColor, colors, particleSpeeds, integrity,
                renderEffect);
    }

    private Vector4f getSuperchargedSpeeds(float multiplier) {
        return new Vector4f(5f * multiplier, 4f * multiplier, 3f * multiplier, 2f * multiplier);
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

    private Vector4f getAnchor(PoseStack stack, Direction upwards, Direction front, int offsetFront, int offsetUp,
                               float shellRadius) {
        Vec3i movement = upwards.getNormal().multiply(offsetUp).offset(front.getNormal().multiply(offsetFront));
        Vector3f center = stack.last().pose().transformPosition(movement.getX() + 0.5f, movement.getY() + 0.5f,
                movement.getZ() + 0.5f, new Vector3f());
        return new Vector4f(center, shellRadius);
    }

    private void renderProjectInterior(PoseStack stack, MultiBufferSource buffer, Direction upwards, Direction front,
                                       Direction left, int tier, int baseColor, Vector4i colors,
                                       Vector4f particleSpeeds, float integrity, int renderEffect) {
        Vector4f anchor;
        switch (tier) {
            case 1:
                anchor = getAnchor(stack, upwards, front, -1, 1, 0.9f);
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -1, 1,
                        1.002f, 1.002f, 1.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);
                break;
            case 2:
                anchor = getAnchor(stack, upwards, front, -2, 2, 2.6f);
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -2, 2,
                        3.002f, 3.002f, 3.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);
                break;
            case 3:
                anchor = getAnchor(stack, upwards, front, -4, 2, 5.0f);
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -4, 2,
                        5.002f, 7.002f, 5.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -4, 2,
                        7.002f, 5.002f, 5.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -4, 2,
                        5.002f, 5.002f, 7.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);
                break;
            case 4:
                anchor = getAnchor(stack, upwards, front, -5, 5, 5.4f);
                // Lower square
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -5, 1,
                        3.002f, 1.002f, 5.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -5, 1,
                        5.002f, 1.002f, 3.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);

                // Middle tube
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -5, 5,
                        1.002f, 7.002f, 1.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);

                // Upper square
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -5, 9,
                        3.002f, 1.002f, 5.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);
                MicroverseRenderHelper.renderMicroverse(stack, buffer, upwards, front, left, -5, 9,
                        5.002f, 1.002f, 3.002f, baseColor, colors, particleSpeeds, anchor, integrity, renderEffect);
                break;
        }
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
