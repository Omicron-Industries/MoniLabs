package net.neganote.monilabs;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.neganote.monilabs.commands.PackSwitcherCommands;
import net.neganote.monilabs.common.machine.multiblock.CreativeEnergyMultiMachine;
import net.neganote.monilabs.common.machine.multiblock.UniqueWorkableElectricMultiblockMachine;

@Mod.EventBusSubscriber(modid = MoniLabs.MOD_ID)
public class MoniEvents {

    @SubscribeEvent
    public static void RegisterCommandsEvent(RegisterCommandsEvent event) {
        MoniLabs.LOGGER.debug("Registering commands...");
        PackSwitcherCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        UniqueWorkableElectricMultiblockMachine.ACTIVE_OWNERS.clear();
        CreativeEnergyMultiMachine.LOADED_ENERGY_CONTAINERS.clear();
    }
}
