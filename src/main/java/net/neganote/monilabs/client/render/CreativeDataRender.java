package net.neganote.monilabs.client.render;

import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neganote.monilabs.common.machine.multiblock.CreativeDataMultiMachine;
import net.neganote.monilabs.utils.LaserUtil;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.joml.Vector3f;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@SideOnly(Side.CLIENT)
public class CreativeDataRender extends DynamicRender<CreativeDataMultiMachine, CreativeDataRender> {

    public static final CreativeDataRender INSTANCE = new CreativeDataRender();

    public static final Codec<CreativeDataRender> CODEC = Codec.unit(INSTANCE);
    public static final DynamicRenderType<CreativeDataMultiMachine, CreativeDataRender> TYPE = new DynamicRenderType<>(
            CODEC);

    @Override
    public DynamicRenderType<CreativeDataMultiMachine, CreativeDataRender> getType() {
        return TYPE;
    }

    @Override
    public void render(CreativeDataMultiMachine machine, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        var frontFacing = machine.getFrontFacing();
        var upwardsFacing = machine.getUpwardsFacing();

        Direction front = RelativeDirection.FRONT.getRelativeFacing(frontFacing, upwardsFacing, machine.isFlipped());
        Direction back = front.getOpposite();
        Direction upwards = RelativeDirection.UP.getRelativeFacing(frontFacing, upwardsFacing, machine.isFlipped());

        float rayLength = 128.0f;
        Vector3f ray = new Vector3f(upwards.getNormal().getX() * rayLength, upwards.getNormal().getY() * rayLength,
                upwards.getNormal().getZ() * rayLength);

        Vector3f offset = back.step().mul(2.0f).add(upwards.step().mul(18.0f)).add(0.5f, 0.5f, 0.5f);

        int gameTime = Objects.requireNonNull(Minecraft.getInstance().player).tickCount;
        LaserUtil.renderLaser(ray, poseStack, buffer, 0.25f, 0.25f, 1.0f, 1.0f, offset.x, offset.y, offset.z,
                partialTick,
                gameTime, false);
    }

    @Override
    public boolean shouldRender(CreativeDataMultiMachine machine, Vec3 cameraPos) {
        return machine.isFormed() && machine.isActive();
    }

    @Override
    public boolean shouldRenderOffScreen(CreativeDataMultiMachine machine) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(CreativeDataMultiMachine machine) {
        return new AABB(machine.getBlockPos()).inflate(getViewDistance(), getViewDistance() + 32, getViewDistance());
    }
}
