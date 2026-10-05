package net.neganote.monilabs.packmode;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.neganote.monilabs.MoniLabs;

@Mod.EventBusSubscriber(modid = MoniLabs.MOD_ID, value = Dist.CLIENT)
public final class XpCostClientEvents {

    private XpCostClientEvents() {}

    @SubscribeEvent
    public static void loggedOut(ClientPlayerNetworkEvent.LoggingOut event) {
        xpModeCost.setFreeClient(false);
    }
}
