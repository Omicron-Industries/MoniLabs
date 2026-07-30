package net.neganote.monilabs.client.render;

import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neganote.monilabs.common.machine.multiblock.CreativeEnergyMultiMachine;
import net.neganote.monilabs.utils.LaserUtil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.joml.Vector3f;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SideOnly(Side.CLIENT)
public class CreativeEnergyRender extends DynamicRender<CreativeEnergyMultiMachine, CreativeEnergyRender> {

    public static final CreativeEnergyRender INSTANCE = new CreativeEnergyRender();

    // spotless:off
    public static final Codec<CreativeEnergyRender> CODEC = Codec.unit(INSTANCE);
    public static DynamicRenderType<CreativeEnergyMultiMachine, CreativeEnergyRender> TYPE = new DynamicRenderType<>(CreativeEnergyRender.CODEC);
    // spotless:on

    private CreativeEnergyRender() {}

    @Override
    public DynamicRenderType<CreativeEnergyMultiMachine, CreativeEnergyRender> getType() {
        return CreativeEnergyRender.TYPE;
    }

    @Override
    public void render(CreativeEnergyMultiMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var frontFacing = machine.getFrontFacing();
        var upwardsFacing = machine.getUpwardsFacing();

        Direction front = RelativeDirection.FRONT.getRelativeFacing(frontFacing, upwardsFacing, machine.isFlipped());

        Direction upwards = RelativeDirection.UP.getRelativeFacing(frontFacing, upwardsFacing, machine.isFlipped());

        poseStack.pushPose();
        float translateX = 0.5f;
        float translateY = 0.5f;
        float translateZ = 0.5f;

        Direction back = front.getOpposite();
        Vec3i backVec = back.getNormal().multiply(6);
        Vec3i upVec = upwards.getNormal().multiply(13);

        Vec3i bhPos = machine.getBlockPos().relative(back, 6).relative(upwards, 13);
        translateX += backVec.getX() + upVec.getX();
        translateY += backVec.getY() + upVec.getY();
        translateZ += backVec.getZ() + upVec.getZ();

        poseStack.translate(translateX, translateY, translateZ);

        int gameTime = Objects.requireNonNull(Minecraft.getInstance().player).tickCount;
        poseStack.pushPose();
        poseStack.scale(4, 1, 4);
        LaserUtil.renderLaser(new Vector3f(0, 256, 0), poseStack, buffer, 0.6f, 1f, 1f, 1f, 0, -translateY, 0,
                partialTick,
                gameTime,
                false);
        poseStack.popPose();

        BlackHoleRenderer.render(new Vector3f(bhPos.getX() + 0.5f, bhPos.getY() + 0.5f, bhPos.getZ() + 0.5f));
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(CreativeEnergyMultiMachine machine, Vec3 cameraPos) {
        return machine.isFormed() && machine.isActive();
    }

    @Override
    public boolean shouldRenderOffScreen(CreativeEnergyMultiMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }

    @Override
    public AABB getRenderBoundingBox(CreativeEnergyMultiMachine machine) {
        return new AABB(machine.getBlockPos()).inflate(getViewDistance(), getViewDistance() + 32, getViewDistance());
    }
}
