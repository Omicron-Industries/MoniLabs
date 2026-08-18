package net.neganote.monilabs.common.item;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neganote.monilabs.MoniLabs;
import net.neganote.monilabs.common.cover.MoniCovers;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import static net.neganote.monilabs.MoniLabs.REGISTRATE;

@SuppressWarnings("unused")
public class MoniItems {

    static {
        REGISTRATE.creativeModeTab(() -> MoniLabs.MONI_CREATIVE_TAB);
    }

    public static void init() {
        MoniCovers.init();
    }

    public static final ItemEntry<Item> MAX_ELECTRIC_MOTOR = REGISTRATE
            .item("max_electric_motor", Item::new)
            .lang("MAX Electric Motor")
            .register();

    public static final ItemEntry<Item> MAX_ELECTRIC_PISTON = REGISTRATE
            .item("max_electric_piston", Item::new)
            .lang("MAX Electric Piston")
            .register();

    public static final ItemEntry<Item> MAX_EMITTER = REGISTRATE
            .item("max_emitter", Item::new)
            .lang("MAX Emitter")
            .register();

    public static final ItemEntry<Item> MAX_SENSOR = REGISTRATE
            .item("max_sensor", Item::new)
            .lang("MAX Sensor")
            .register();

    public static final ItemEntry<Item> MAX_FIELD_GENERATOR = REGISTRATE
            .item("max_field_generator", Item::new)
            .lang("MAX Field Generator")
            .register();

    public static final ItemEntry<ComponentItem> MAX_CONVEYOR_MODULE = REGISTRATE
            .item("max_conveyor_module", ComponentItem::create)
            .lang("MAX Conveyor Module")
            .onRegister(attach(new CoverPlaceBehavior(MoniCovers.MAX_CONVEYOR_MODULE)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.conveyor.module.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .register();

    public static final ItemEntry<ComponentItem> MAX_ROBOT_ARM = REGISTRATE
            .item("max_robot_arm", ComponentItem::create)
            .lang("MAX Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(MoniCovers.MAX_ROBOT_ARM)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate_stacks", 16));
            })))
            .register();

    public static ItemEntry<ComponentItem> MAX_ELECTRIC_PUMP = REGISTRATE
            .item("max_electric_pump", ComponentItem::create)
            .lang("MAX Electric Pump")
            .onRegister(attach(new CoverPlaceBehavior(MoniCovers.MAX_ELECTRIC_PUMP)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.electric.pump.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate",
                        1280 * 64 * 64 * 4 / 20));
            })))
            .register();

    public static ItemEntry<ComponentItem> MAX_FLUID_REGULATOR = REGISTRATE
            .item("max_fluid_regulator", ComponentItem::create)
            .lang("MAX Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(MoniCovers.MAX_FLUID_REGULATOR)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.fluid.regulator.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.fluid_transfer_rate",
                        1280 * 64 * 64 * 4 / 20));
            })))
            .register();

    public static final ItemEntry<Item> QUANTUM_FLUX = REGISTRATE
            .item("quantum_flux", Item::new)
            .lang("Quantum Flux")
            .register();

    // Copied from GTItems
    public static <T extends IComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }
}
