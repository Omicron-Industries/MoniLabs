package net.neganote.monilabs;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.neganote.monilabs.commands.PackSwitcherCommands;
import net.neganote.monilabs.common.data.materials.MoniMaterials;
import net.neganote.monilabs.common.item.MoniItems;
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

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Item> itemMapping : event.getAllMappings(Registries.ITEM)) {
            if (itemMapping.getKey().equals(MoniLabs.kjsResLoc("quantum_flux"))) {
                itemMapping.remap(MoniItems.QUANTUM_FLUX.get());
            } else if (itemMapping.getKey().equals(GTCEu.id("mana_bucket"))) {
                itemMapping.remap(MoniMaterials.Mana.getBucket());
            }
        }

        for (MissingMappingsEvent.Mapping<Fluid> fluidMapping : event.getAllMappings(Registries.FLUID)) {
            if (fluidMapping.getKey().equals(GTCEu.id("mana"))) {
                fluidMapping.remap(MoniMaterials.Mana.getFluid());
            } else if (fluidMapping.getKey().equals(GTCEu.id("flowing_mana"))) {
                fluidMapping.ignore();
            }
        }
    }
}
