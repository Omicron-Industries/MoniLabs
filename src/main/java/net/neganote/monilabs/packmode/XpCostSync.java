package net.neganote.monilabs.packmode;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.neganote.monilabs.MoniLabs;

@Mod.EventBusSubscriber(modid = MoniLabs.MOD_ID)
public final class XpCostSync {

    private record PolicyPacket(boolean free) {}

    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            MoniLabs.id("xp_cost_policy"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private XpCostSync() {}

    public static void register() {
        CHANNEL.messageBuilder(PolicyPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((packet, buffer) -> buffer.writeBoolean(packet.free()))
                .decoder(buffer -> new PolicyPacket(buffer.readBoolean()))
                .consumerMainThread((packet, context) -> xpModeCost.setFreeClient(packet.free()))
                .add();
    }

    // Set thhe cost of Anvils and Enchanters based on the servers config
    @SubscribeEvent
    public static void serverStarting(ServerAboutToStartEvent event) {
        PackMode mode = PackMode.read(FMLPaths.CONFIGDIR.get().resolve("packmode.json"));
        xpModeCost.setModeServer(mode);
    }

    // Clients will always follow the server mode incase their mod file is different.
    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new PolicyPacket(xpModeCost.isFree(LogicalSide.SERVER)));
        }
    }
}
