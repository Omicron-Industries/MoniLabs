package net.neganote.monilabs.client.render;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.neganote.monilabs.MoniLabs;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class MoniShaders {

    public static ShaderInstance WORMHOLE_SHADER;
    public static ShaderInstance MICROVERSE_SHADER;

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    MoniLabs.id("rendertype_wormhole"), DefaultVertexFormat.POSITION),
                    (shaderInstance -> WORMHOLE_SHADER = shaderInstance));

            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    MoniLabs.id("rendertype_microverse"), MoniVertexFormats.MICROVERSE),
                    (shaderInstance -> MICROVERSE_SHADER = shaderInstance));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
