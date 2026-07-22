package net.neganote.monilabs.common.machine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.util.TooltipHelper;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.LaserHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.neganote.monilabs.MoniLabs;
import net.neganote.monilabs.client.render.MoniDynamicRenderHelper;
import net.neganote.monilabs.common.block.MoniBlocks;
import net.neganote.monilabs.common.data.materials.MoniMaterials;
import net.neganote.monilabs.common.data.tooltips.MoniTooltipHelper;
import net.neganote.monilabs.common.machine.multiblock.*;
import net.neganote.monilabs.common.machine.part.*;
import net.neganote.monilabs.config.MoniConfig;
import net.neganote.monilabs.data.models.MoniMachineModels;
import net.neganote.monilabs.gtbridge.MoniRecipeTypes;
import net.neganote.monilabs.recipe.MoniRecipeModifiers;

import appeng.core.definitions.AEBlocks;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.IN;
import static com.gregtechceu.gtceu.api.capability.recipe.IO.OUT;
import static com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties.IS_FORMED;
import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.registerTieredMachines;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableTieredHullMachineModel;
import static net.neganote.monilabs.MoniLabs.REGISTRATE;

@SuppressWarnings("unused")
public class MoniMachines {

    public static @NotNull BiConsumer<IMultiController, List<Component>> currentColorDisplayInfo() {
        return (controller, components) -> {
            if (controller instanceof PrismaticCrucibleMachine prismMachine && controller.isFormed()) {
                components.add(Component.translatable("monilabs.prismatic.current_color",
                        Component.translatable(prismMachine.getColorState().nameKey)));
            }
        };
    }

    public static @NotNull BiConsumer<IMultiController, List<Component>> currentDiversityPointsInfo() {
        return (controller, list) -> {
            if (controller instanceof OmnicSynthesizerMachine omnic) {
                list.add(Component.translatable("monilabs.omnic.current_diversity_points", omnic.diversityPoints)
                        .append(Component.literal("%")));
            }
        };
    }

    public static final BiConsumer<ItemStack, List<Component>> PRISMATIC_TOOLTIPS = (stack, list) -> {
        list.add(
                Component.translatable("monilabs.tooltip.prismatic.0",
                        Component.translatable("monilabs.tooltip.prismatic.rainbow")
                                .withStyle(TooltipHelper.RAINBOW_HSL_SLOW)));
        list.add(
                Component.translatable("monilabs.tooltip.prismatic.1"));
        list.add(
                Component.translatable("monilabs.tooltip.prismatic.2"));
    };

    public static final BiConsumer<ItemStack, List<Component>> CREATIVE_ENERGY_MULTI_TOOLTIPS = (stack, list) -> {
        list.add(
                Component.translatable("monilabs.tooltip.creative_energy_multi_description.0",
                        Component.translatable("monilabs.tooltip.universe_lerp")
                                .withStyle(MoniTooltipHelper.UNIVERSE_HSL)));
        list.add(
                Component.translatable("monilabs.tooltip.creative_energy_multi_description.1"));
    };
    public static final BiConsumer<ItemStack, List<Component>> SCULK_VAT_TOOLTIPS = (stack, list) -> {
        list.add(
                Component.translatable("monilabs.tooltip.sculk_vat_description.0",
                        Component.translatable("monilabs.tooltip.sculk_lerp")
                                .withStyle(MoniTooltipHelper.SCULK_HSL)));
        list.add(
                Component.translatable("monilabs.tooltip.sculk_vat_description.1"));
    };
    public static final BiConsumer<ItemStack, List<Component>> CREATIVE_DATA_MULTI_TOOLTIPS = (stack, list) -> {
        list.add(
                Component.translatable("monilabs.tooltip.creative_data_multi_description.0",
                        Component.translatable("monilabs.tooltip.universe_lerp")
                                .withStyle(MoniTooltipHelper.UNIVERSE_HSL)));
        list.add(
                Component.translatable("monilabs.tooltip.creative_data_multi_description.1"));
    };

    public static final BiConsumer<ItemStack, List<Component>> BASIC_MICROVERSE_PROJECTOR_TOOLTIPS = (stack, list) -> {
        list.add(
                Component.translatable("tooltip.monilabs.basic_microverse_projector.description.0",
                        Component.translatable("monilabs.tooltip.microverses.space_gradient")
                                .withStyle(MoniTooltipHelper.NEBULA_HSL)));
        list.add(
                Component.translatable("tooltip.monilabs.basic_microverse_projector.description.1"));
        list.add(
                Component.translatable("tooltip.monilabs.basic_microverse_projector.description.2"));
        if (MoniConfig.INSTANCE.values.hostileMicroverseTooltip) {
            list.add(
                    Component.translatable("tooltip.monilabs.hostile_microverse.0"));
            list.add(
                    Component.translatable("tooltip.monilabs.hostile_microverse.1"));
        }
    };

    public static final BiConsumer<ItemStack, List<Component>> ADVANCED_MICROVERSE_PROJECTOR_TOOLTIPS = (stack,
                                                                                                         list) -> {
        list.add(
                Component.translatable("tooltip.monilabs.advanced_microverse_projector.description.0",
                        Component.translatable("monilabs.tooltip.microverses.space_gradient")
                                .withStyle(MoniTooltipHelper.NEBULA_HSL)));
        list.add(
                Component.translatable("tooltip.monilabs.advanced_microverse_projector.description.1"));
        list.add(
                Component.translatable("tooltip.monilabs.advanced_microverse_projector.description.2"));
    };

    public static final BiConsumer<ItemStack, List<Component>> ELITE_MICROVERSE_PROJECTOR_TOOLTIPS = (stack, list) -> {
        list.add(
                Component.translatable("tooltip.monilabs.elite_microverse_projector.description.0",
                        Component.translatable("monilabs.tooltip.microverses.space_gradient")
                                .withStyle(MoniTooltipHelper.NEBULA_HSL),
                        Component.translatable("monilabs.tooltip.microversal.space_gradient")
                                .withStyle(MoniTooltipHelper.NEBULA_HSL)));
        list.add(
                Component.translatable("tooltip.monilabs.elite_microverse_projector.description.1"));
        list.add(
                Component.translatable("tooltip.monilabs.elite_microverse_projector.description.2"));
    };
    public static final BiConsumer<ItemStack, List<Component>> HYPERBOLIC_MICROVERSE_PROJECTOR_TOOLTIPS = (stack,
                                                                                                           list) -> {
        list.add(
                Component.translatable("monilabs.tooltip.hyper_desc",
                        Component.translatable("monilabs.tooltip.microverses.space_gradient")
                                .withStyle(MoniTooltipHelper.NEBULA_HSL)));
        list.add(
                Component.translatable("tooltip.monilabs.hyperbolic_microverse_projector.description.1"));
        list.add(
                Component.translatable("gtceu.multiblock.parallelizable.tooltip"));
        list.add(
                Component.translatable("tooltip.monilabs.hyperbolic_microverse_projector.description.2"));
    };

    public static final MachineDefinition[] PARALLEL_HATCH = registerTieredParallelMachines("parallel_hatch",
            ParallelHatchPartMachine::new,
            (tier, builder) -> builder
                    .langValue(switch (tier) {
                        case 9 -> "Giga";
                        case 10 -> "Omega";
                        default -> "Simple"; // Should never be hit.
                    } + " Parallel Control Hatch")
                    .rotationState(RotationState.ALL)
                    .abilities(PartAbility.PARALLEL_HATCH)
                    .modelProperty(IS_FORMED, false)
                    .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
                    .model(createWorkableTieredHullMachineModel(
                            MoniLabs.id("block/machines/parallel_hatch_mk" + (tier - 4)))
                            .andThen((ctx, prov, model) -> {
                                model.addReplaceableTextures("bottom", "top", "side");
                            }))
                    .tooltips(Component.translatable("gtceu.machine.parallel_hatch_mk" + tier + ".tooltip"),
                            Component.translatable("gtceu.part_sharing.disabled"))
                    .register(),
            UHV, UEV);

    public static MachineDefinition[] registerTieredParallelMachines(String name,
                                                                     BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                     BiFunction<Integer, MachineBuilder<MachineDefinition, ?>, MachineDefinition> builder,
                                                                     int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static MachineDefinition CHROMA_SENSOR_HATCH = REGISTRATE
            .machine("chroma_sensor_hatch", ChromaSensorHatchPartMachine::new)
            .langValue("Chroma Sensor Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("monilabs.tooltip.chroma_sensor_hatch.0"),
                    Component.translatable("monilabs.tooltip.chroma_sensor_hatch.1"),
                    Component.translatable("monilabs.tooltip.chroma_sensor_hatch.2"))
            .modelProperty(RenderColor.COLOR_PROPERTY, RenderColor.NONE)
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayChromaCasingMachineModel("chroma_sensor", "casing/netherite"))
            .tier(GTValues.UHV)
            .register();

    public static MachineDefinition ADVANCED_CHROMA_SENSOR_HATCH = REGISTRATE
            .machine("advanced_chroma_sensor_hatch", AdvancedChromaSensorHatchPartMachine::new)
            .langValue("Advanced Chroma Sensor Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("monilabs.tooltip.advanced_chroma_sensor_hatch.0"),
                    Component.translatable("monilabs.tooltip.advanced_chroma_sensor_hatch.1"))
            .modelProperty(RenderColor.COLOR_PROPERTY, RenderColor.NONE)
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayChromaCasingMachineModel("chroma_sensor", "casing/netherite"))
            .tier(GTValues.UHV)
            .register();

    public static MachineDefinition SCULK_XP_DRAINING_HATCH = REGISTRATE
            .machine("sculk_xp_draining_hatch", SculkExperienceDrainingHatchPartMachine::new)
            .langValue("Sculk XP Draining Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("tooltip.monilabs.xp_draining_hatch.0"),
                    Component.translatable("tooltip.monilabs.xp_draining_hatch.1"),
                    Component.translatable("tooltip.monilabs.xp_draining_hatch.2"))
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayCasingMachineModel("exp_hatch_draining", "casing/cryolobus"))
            .tier(GTValues.ZPM)
            .register();

    public static MachineDefinition SCULK_XP_SENSOR_HATCH = REGISTRATE
            .machine("sculk_xp_sensor_hatch", SculkExperienceSensorHatchPartMachine::new)
            .langValue("Sculk XP Sensor Hatch")
            .rotationState(RotationState.ALL)

            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("tooltip.monilabs.xp_sensor_hatch.0"),
                    Component.translatable("tooltip.monilabs.xp_sensor_hatch.1"))
            .tier(GTValues.ZPM)
            .modelProperty(FillLevel.FILL_PROPERTY, FillLevel.EMPTY_TO_QUARTER)
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayFillLevelCasingMachineModel("exp_sensor", "casing/cryolobus"))
            .register();

    public static MachineDefinition ADVANCED_SCULK_XP_SENSOR_HATCH = REGISTRATE
            .machine("advanced_sculk_xp_sensor_hatch", AdvancedSculkExperienceSensorHatchPartMachine::new)
            .langValue("Advanced Sculk XP Sensor Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("tooltip.monilabs.xp_sensor_hatch.0"),
                    Component.translatable("tooltip.monilabs.advanced_sensor_hatch"))
            .tier(GTValues.ZPM)
            .modelProperty(FillLevel.FILL_PROPERTY, FillLevel.EMPTY_TO_QUARTER)
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayFillLevelCasingMachineModel("exp_sensor", "casing/cryolobus"))
            .register();

    public static MachineDefinition MICROVERSE_STABILITY_SENSOR_HATCH = REGISTRATE
            .machine("microverse_stability_sensor_hatch", MicroverseStabilitySensorHatchPartMachine::new)
            .langValue("Microverse Stability Sensor Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("tooltip.monilabs.microverse_stability_hatch.0"),
                    Component.translatable("tooltip.monilabs.microverse_stability_hatch.1"))
            .tier(GTValues.HV)
            .modelProperty(FillLevel.FILL_PROPERTY, FillLevel.EMPTY_TO_QUARTER)
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayFillLevelCasingMachineModel("stability_hatch", "casing/microverse"))
            .register();

    public static MachineDefinition ADVANCED_MICROVERSE_STABILITY_SENSOR_HATCH = REGISTRATE
            .machine("advanced_microverse_stability_sensor_hatch",
                    AdvancedMicroverseStabilitySensorHatchPartMachine::new)
            .langValue("Advanced Microverse Stability Sensor Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("tooltip.monilabs.microverse_stability_hatch.0"),
                    Component.translatable("tooltip.monilabs.advanced_sensor_hatch"))
            .tier(GTValues.HV)
            .modelProperty(FillLevel.FILL_PROPERTY, FillLevel.EMPTY_TO_QUARTER)
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayFillLevelCasingMachineModel("stability_hatch", "casing/microverse"))
            .register();

    public static MachineDefinition MICROVERSE_TYPE_SENSOR_HATCH = REGISTRATE
            .machine("microverse_type_sensor_hatch", MicroverseTypeSensorHatchPartMachine::new)
            .langValue("Microverse Type Sensor Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("tooltip.monilabs.microverse_type_hatch.0"),
                    Component.translatable("tooltip.monilabs.microverse_type_hatch.1"),
                    Component.translatable("tooltip.monilabs.microverse_type_hatch.2"),
                    Component.translatable("tooltip.monilabs.microverse_type_hatch.3"),
                    Component.translatable("tooltip.monilabs.microverse_type_hatch.4"),
                    Component.translatable("tooltip.monilabs.microverse_type_hatch.5"))
            .conditionalTooltip(Component.translatable("tooltip.monilabs.microverse_type_hatch.hostile"),
                    MoniConfig.INSTANCE.values.hostileMicroverseTooltip)
            .tier(GTValues.HV)
            .modelProperty(Microverse.MICROVERSE_TYPE, Microverse.NONE)
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayMicroverseCasingMachineModel("type_hatch", "casing/microverse"))
            .register();

    public static MachineDefinition ADVANCED_MICROVERSE_TYPE_SENSOR_HATCH = REGISTRATE
            .machine("advanced_microverse_type_sensor_hatch", AdvancedMicroverseTypeSensorHatchPartMachine::new)
            .langValue("Advanced Microverse Type Sensor Hatch")
            .rotationState(RotationState.ALL)
            .tooltips(Component.translatable("gtceu.part_sharing.disabled"),
                    Component.translatable("tooltip.monilabs.advanced_microverse_type_hatch.0"),
                    Component.translatable("tooltip.monilabs.advanced_microverse_type_hatch.1"))
            .tier(GTValues.HV)
            .modelProperty(Microverse.MICROVERSE_TYPE, Microverse.NONE)
            .modelProperty(IS_FORMED, false)
            .model(MoniMachineModels.createOverlayMicroverseCasingMachineModel("type_hatch", "casing/microverse"))
            .register();

    public static MultiblockMachineDefinition PRISMATIC_CRUCIBLE = REGISTRATE
            .multiblock("prismatic_crucible", PrismaticCrucibleMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .allowExtendedFacing(false)
            .recipeTypes(MoniRecipeTypes.CHROMATIC_PROCESSING, MoniRecipeTypes.CHROMATIC_TRANSCENDENCE)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    // spotless:off
                    .aisle("LLL#######LLL", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
                    .aisle("LLLLL###LLLLL", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#l#####l#F#", "#lll#####lll#")
                    .aisle("LLLLLLLLLLLLL", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##Fll###llF##", "#llll###llll#")
                    .aisle("#LLCCCCCCCLL#", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "#llCCC#CCCll#", "#llll###llll#")
                    .aisle("#LLCLLCLLCLL#", "#####LCL#####", "######C######", "#############", "#############", "#############", "#############", "#####lCl#####", "##lClCCClCl##", "##ll#####ll##")
                    .aisle("##LCLLLLLCL##", "####L###L####", "#############", "#############", "#############", "#############", "######F######", "####llCll####", "###CCl#lCC###", "#############")
                    .aisle("##LCCLLLCCL##", "####C###C####", "####C###C####", "#############", "#############", "#############", "#####FPF#####", "####CCCCC####", "####C###C####", "#############")
                    .aisle("##LCLLLLLCL##", "####L###L####", "#############", "#############", "#############", "#############", "######F######", "####llCll####", "###CCl#lCC###", "#############")
                    .aisle("#LLCLLCLLCLL#", "#####LCL#####", "######C######", "#############", "#############", "#############", "#############", "#####lCl#####", "##lClCCClCl##", "##ll#####ll##")
                    .aisle("#LLCCCCCCCLL#", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "###C#####C###", "#llCCC#CCCll#", "#llll###llll#")
                    .aisle("LLLLLLMLLLLLL", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##F#######F##", "##Fll###llF##", "#llll###llll#")
                    .aisle("LLLLL###LLLLL", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#########F#", "#F#l#####l#F#", "#lll#####lll#")
                    .aisle("LLL#######LLL", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############", "#############")
                    // spotless:on
                    .where("L",
                            Predicates.blocks(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING.get())
                                    .setMinGlobalLimited(88)
                                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2)
                                            .setMinGlobalLimited(1)
                                            .setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                    .or(Predicates.machines(CHROMA_SENSOR_HATCH))
                                    .or(Predicates.machines(ADVANCED_CHROMA_SENSOR_HATCH)))
                    .where("l", Predicates.blocks(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING.get()))
                    .where("C", Predicates.blocks(MoniBlocks.CHROMODYNAMIC_CONDUCTION_CASING.get()))
                    .where("M", Predicates.controller(Predicates.blocks(definition.getBlock())))
                    .where("P", Predicates.blocks(MoniBlocks.PRISMATIC_FOCUS.get()))
                    .where("F", Predicates.frames(GTMaterials.Neutronium))
                    .where("#", Predicates.any())
                    .build())
            .tooltipBuilder(PRISMATIC_TOOLTIPS)
            .additionalDisplay(MoniMachines.currentColorDisplayInfo())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels
                    .createWorkableCasingMachineModel(MoniLabs.id("block/casing/netherite"),
                            GTCEu.id("block/multiblock/processing_array"))
                    .andThen(b -> b.addDynamicRenderer(MoniDynamicRenderHelper::createPrismacLaserRender)))
            .hasBER(true)
            .register();

    public static MultiblockMachineDefinition BASIC_MICROVERSE_PROJECTOR = REGISTRATE
            .multiblock("basic_microverse_projector", (holder) -> new MicroverseProjectorMachine(holder, 1))
            .langValue("Basic Microverse Projector")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(MoniRecipeTypes.MICROVERSE_RECIPES)
            .recipeModifiers(MoniRecipeModifiers.MICROVERSE_OC)
            .appearanceBlock(MoniBlocks.MICROVERSE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCC", "CVC", "CCC")
                    .aisle("CCC", "GDG", "CCC")
                    .aisle("C@C", "CGC", "CCC")
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("D", Predicates.any())
                    .where("C", Predicates.blocks(MoniBlocks.MICROVERSE_CASING.get()).setMinGlobalLimited(10)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.machines(MICROVERSE_STABILITY_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(ADVANCED_MICROVERSE_STABILITY_SENSOR_HATCH))
                            .or(Predicates.machines(MICROVERSE_TYPE_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(ADVANCED_MICROVERSE_TYPE_SENSOR_HATCH)))
                    .where("G", Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get())
                            .or(Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                            .or(Predicates.blocks(GTBlocks.FUSION_GLASS.get()))
                            .or(Predicates.blocks(MoniBlocks.PRISM_GLASS.get())))
                    .where("V", Predicates.blocks(GTBlocks.CASING_GRATE.get()))
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels
                    .createWorkableCasingMachineModel(MoniLabs.id("block/casing/microverse"),
                            MoniLabs.id("block/machines/projectors"))
                    .andThen(b -> b.addDynamicRenderer(
                            MoniDynamicRenderHelper::createMicroverseProjectorRender)))
            .tooltipBuilder(BASIC_MICROVERSE_PROJECTOR_TOOLTIPS)
            .hasBER(true)
            .register();

    public static MultiblockMachineDefinition ADVANCED_MICROVERSE_PROJECTOR = REGISTRATE
            .multiblock("advanced_microverse_projector", (holder) -> new MicroverseProjectorMachine(holder, 2))
            .langValue("Advanced Microverse Projector")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(MoniRecipeTypes.MICROVERSE_RECIPES)
            .recipeModifiers(MoniRecipeModifiers.MICROVERSE_OC)
            .appearanceBlock(MoniBlocks.MICROVERSE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCCCC", "CGGGC", "CGGGC", "CGGGC", "CCCCC")
                    .aisle("CVCVC", "GDDDG", "GDDDG", "GDDDG", "CVCVC")
                    .aisle("CCCCC", "GDDDG", "GD#DG", "GDDDG", "CCCCC")
                    .aisle("CVCVC", "GDDDG", "GDDDG", "GDDDG", "CVCVC")
                    .aisle("CC@CC", "CGGGC", "CGGGC", "CGGGC", "CCCCC")
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("D", Predicates.any())
                    .where("C", Predicates.blocks(MoniBlocks.MICROVERSE_CASING.get()).setMinGlobalLimited(45)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.machines(MICROVERSE_STABILITY_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(ADVANCED_MICROVERSE_STABILITY_SENSOR_HATCH))
                            .or(Predicates.machines(MICROVERSE_TYPE_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(ADVANCED_MICROVERSE_TYPE_SENSOR_HATCH)))
                    .where("G", Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get())
                            .or(Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                            .or(Predicates.blocks(GTBlocks.FUSION_GLASS.get()))
                            .or(Predicates.blocks(MoniBlocks.PRISM_GLASS.get())))
                    .where("V", Predicates.blocks(GTBlocks.CASING_GRATE.get()))
                    .where("#", Predicates.any())
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels
                    .createWorkableCasingMachineModel(MoniLabs.id("block/casing/microverse"),
                            MoniLabs.id("block/machines/projectors"))
                    .andThen(b -> b.addDynamicRenderer(
                            MoniDynamicRenderHelper::createMicroverseProjectorRender)))
            .tooltipBuilder(ADVANCED_MICROVERSE_PROJECTOR_TOOLTIPS)
            .hasBER(true)
            .register();

    public static MultiblockMachineDefinition ELITE_MICROVERSE_PROJECTOR = REGISTRATE
            .multiblock("elite_microverse_projector", (holder) -> new MicroverseProjectorMachine(holder, 3))
            .langValue("Elite Microverse Projector")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(MoniRecipeTypes.MICROVERSE_RECIPES)
            .recipeModifiers(MoniRecipeModifiers.MICROVERSE_OC)
            .appearanceBlock(MoniBlocks.MICROVERSE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#########", "#########", "##CCCCC##", "##CVCVC##", "##CCCCC##", "##CVCVC##", "##CCCCC##",
                            "#########", "#########")
                    .aisle("#########", "##CGGGC##", "#CDDDDDC#", "#CDDDDDC#", "#CDDDDDC#", "#CDDDDDC#", "#CDDDDDC#",
                            "##CGGGC##", "#########")
                    .aisle("##CCCCC##", "#CDDDDDC#", "CDDDDDDDC", "CDDDDDDDC", "CDDDDDDDC", "CDDDDDDDC", "CDDDDDDDC",
                            "#CDDDDDC#", "##CCCCC##")
                    .aisle("##CGGGC##", "#GDDDDDG#", "CDDDDDDDC", "GDD###DDG", "GDD###DDG", "GDD###DDG", "CDDDDDDDC",
                            "#GDDDDDG#", "##CGGGC##")
                    .aisle("##CGGGC##", "#GDDDDDG#", "CDDDDDDDC", "GDD###DDG", "GDD###DDG", "GDD###DDG", "CDDDDDDDC",
                            "#GDDDDDG#", "##CGGGC##")
                    .aisle("##CGGGC##", "#GDDDDDG#", "CDDDDDDDC", "GDD###DDG", "GDD###DDG", "GDD###DDG", "CDDDDDDDC",
                            "#GDDDDDG#", "##CGGGC##")
                    .aisle("##CCCCC##", "#CDDDDDC#", "CDDDDDDDC", "CDDDDDDDC", "CDDDDDDDC", "CDDDDDDDC", "CDDDDDDDC",
                            "#CDDDDDC#", "##CCCCC##")
                    .aisle("#########", "##CGGGC##", "#CDDDDDC#", "#CDDDDDC#", "#CDDDDDC#", "#CDDDDDC#", "#CDDDDDC#",
                            "##CGGGC##", "#########")
                    .aisle("#########", "#########", "##CC@CC##", "##CGGGC##", "##CGGGC##", "##CGGGC##", "##CCCCC##",
                            "#########", "#########")
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("D", Predicates.any())
                    .where("C", Predicates.blocks(MoniBlocks.MICROVERSE_CASING.get()).setMinGlobalLimited(125)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.machines(MICROVERSE_STABILITY_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(ADVANCED_MICROVERSE_STABILITY_SENSOR_HATCH))
                            .or(Predicates.machines(MICROVERSE_TYPE_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(ADVANCED_MICROVERSE_TYPE_SENSOR_HATCH)))
                    .where("G", Predicates.blocks(GTBlocks.CASING_TEMPERED_GLASS.get())
                            .or(Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                            .or(Predicates.blocks(GTBlocks.FUSION_GLASS.get()))
                            .or(Predicates.blocks(MoniBlocks.PRISM_GLASS.get())))
                    .where("V", Predicates.blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                    .where("#", Predicates.any())
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels
                    .createWorkableCasingMachineModel(MoniLabs.id("block/casing/microverse"),
                            MoniLabs.id("block/machines/projectors"))
                    .andThen(b -> b.addDynamicRenderer(
                            MoniDynamicRenderHelper::createMicroverseProjectorRender)))
            .tooltipBuilder(ELITE_MICROVERSE_PROJECTOR_TOOLTIPS)
            .hasBER(true)
            .register();

    public static MultiblockMachineDefinition HYPERBOLIC_MICROVERSE_PROJECTOR = REGISTRATE
            .multiblock("hyperbolic_microverse_projector", (holder) -> new MicroverseProjectorMachine(holder, 4))
            .langValue("Hyperbolic Microverse Projector")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(MoniRecipeTypes.MICROVERSE_RECIPES)
            .recipeModifiers(MoniRecipeModifiers.MICROVERSE_PARALLEL_HATCH, MoniRecipeModifiers.MICROVERSE_OC)
            .appearanceBlock(MoniBlocks.MICROVERSE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("###CCCCC###", "###N###N###", "###N###N###", "###N###N###", "###N###N###", "###N###N###",
                            "###N###N###", "###N###N###", "###N###N###", "###N###N###", "###CCCCC###")
                    .aisle("#CCCCVCCCC#", "###########", "###########", "###########", "###########", "###########",
                            "###########", "###########", "###########", "###########", "#CCCCVCCCC#")
                    .aisle("#CVCCCCCVC#", "###CGGGC###", "###########", "###########", "###########", "###########",
                            "###########", "###########", "###########", "###CGGGC###", "#CVCCCCCVC#")
                    .aisle("CCCCCCCCCCC", "N#CCDDDCC#N", "N##CGGGC##N", "N#########N", "N#########N", "N#########N",
                            "N#########N", "N#########N", "N##CGGGC##N", "N#CCDDDCC#N", "CCCCCCCCCCC")
                    .aisle("CCCCCCCCCCC", "##GDDDDDG##", "###GYDYG###", "####YGY####", "####YGY####", "####YGY####",
                            "####YGY####", "####YGY####", "###GYDYG###", "##GDDDDDG##", "CCCCCCCCCCC")
                    .aisle("CVCCCWCCCVC", "##GDDDDDG##", "###GDDDG###", "####GDG####", "####GDG####", "####GDG####",
                            "####GDG####", "####GDG####", "###GDDDG###", "##GDDDDDG##", "CVCCCWCCCVC")
                    .aisle("CCCCCCCCCCC", "##GDDDDDG##", "###GYDYG###", "####YGY####", "####YGY####", "####YGY####",
                            "####YGY####", "####YGY####", "###GYDYG###", "##GDDDDDG##", "CCCCCCCCCCC")
                    .aisle("CCCCCCCCCCC", "N#CCDDDCC#N", "N##CGGGC##N", "N#########N", "N#########N", "N#########N",
                            "N#########N", "N#########N", "N##CGGGC##N", "N#CCDDDCC#N", "CCCCCCCCCCC")
                    .aisle("#CVCCCCCVC#", "###CGGGC###", "###########", "###########", "###########", "###########",
                            "###########", "###########", "###########", "###CGGGC###", "#CVCCCCCVC#")
                    .aisle("#CCCCVCCCC#", "###########", "###########", "###########", "###########", "###########",
                            "###########", "###########", "###########", "###########", "#CCCCVCCCC#")
                    .aisle("###CC@CC###", "###N###N###", "###N###N###", "###N###N###", "###N###N###", "###N###N###",
                            "###N###N###", "###N###N###", "###N###N###", "###N###N###", "###CCCCC###")
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("D", Predicates.any())
                    .where("C", Predicates.blocks(MoniBlocks.MICROVERSE_CASING.get()).setMinGlobalLimited(195)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes()))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.machines(MICROVERSE_STABILITY_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(ADVANCED_MICROVERSE_STABILITY_SENSOR_HATCH))
                            .or(Predicates.machines(MICROVERSE_TYPE_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(ADVANCED_MICROVERSE_TYPE_SENSOR_HATCH)))
                    .where("G", Predicates.blocks(GTBlocks.FUSION_GLASS.get())
                            .or(Predicates.blocks(MoniBlocks.PRISM_GLASS.get())))
                    .where("N", Predicates.frames(MoniMaterials.SculkBioalloy))
                    .where("V", Predicates.blocks(AEBlocks.QUARTZ_VIBRANT_GLASS.block()))
                    .where("W", Predicates.blocks(ForgeRegistries.BLOCKS
                            .getValue(MoniLabs.kjsResLoc("universal_warp_core"))))
                    .where("Y", Predicates.blocks(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING.get()))
                    .where("#", Predicates.any())
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels
                    .createWorkableCasingMachineModel(MoniLabs.id("block/casing/microverse"),
                            MoniLabs.id("block/machines/projectors"))
                    .andThen(b -> b.addDynamicRenderer(
                            MoniDynamicRenderHelper::createMicroverseProjectorRender)))
            .tooltipBuilder(HYPERBOLIC_MICROVERSE_PROJECTOR_TOOLTIPS)
            .hasBER(true)
            .register();

    public static MultiblockMachineDefinition CREATIVE_ENERGY_MULTI = REGISTRATE
            .multiblock("creative_energy_multi", CreativeEnergyMultiMachine::new)
            .langValue("Transdimensional Energy Singularity")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(MoniRecipeTypes.CREATIVE_ENERGY_MULTI_RECIPES)
            .noRecipeModifier()
            .appearanceBlock(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    // spotless:off
                    .aisle("###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "#######H#######", "#######H#######", "#######H#######", "#######H#######", "#######H#######", "#######H#######", "#######H#######", "###############")
                    .aisle("#####NNNNN#####", "#####NNNNN#####", "#######F#######", "#######F#######", "#######F#######", "###############", "###############", "###############", "###############", "#######H#######", "#######H#######", "#######H#######", "#####AAHAA#####", "#######H#######", "#####AAHAA#####", "#######H#######", "#######H#######", "#######H#######", "#######R#######")
                    .aisle("###NNNNNNNNN###", "###NNNNNNNNN###", "######AAA######", "######AAA######", "######AAA######", "#######F#######", "#######F#######", "###############", "#######H#######", "#######H#######", "#######H#######", "###############", "####A##P##A####", "###############", "####A##P##A####", "###############", "###############", "###############", "###############")
                    .aisle("##NNNNNNNNNNN##", "##NNNNNENNNNN##", "#######F#######", "#######F#######", "######AAA######", "######AAA######", "######AAA######", "#####CCCCC#####", "#######H#######", "#####CCCCC#####", "###############", "###############", "###A#######A###", "###############", "###A#######A###", "###############", "###############", "###############", "###############")
                    .aisle("##NNNNNNNNNNN##", "##NNNNAEANNNN##", "###############", "###############", "###############", "###############", "###############", "####CC###CC####", "#######H#######", "####CC###CC####", "###############", "###############", "##A#########A##", "###############", "##A#########A##", "###############", "###############", "###############", "###############")
                    .aisle("#NNNNNNNNNNNNN#", "#NNNNAAEAANNNN#", "#####VAEAV#####", "#####V###V#####", "###############", "###############", "###############", "###CC#####CC###", "###############", "###CC#####CC###", "###############", "###############", "#A###########A#", "###############", "#A###########A#", "###############", "###############", "###############", "###############")
                    .aisle("#NNNNNNNNNNNNN#", "#NNNAAAAAAANNN#", "##A##AAEAA##A##", "##A###AEA###A##", "##AA##VEV##AA##", "###A##V#V##A###", "###A#######A###", "###C#######C###", "###############", "###C#######C###", "###############", "###############", "#A###########A#", "###############", "#A###########A#", "###############", "###############", "###############", "###############")
                    .aisle("#NNNNNNNNNNNNN#", "#NNEEEAAAEEENN#", "#FAF#EEAEE#FAF#", "#FAF##EAE##FAF#", "#FAA##EEE##AAF#", "##FA###E###AF##", "##FA###E###AF##", "###C###E###C###", "##HHH##P##HHH##", "#HHC#######CHH#", "#HH#########HH#", "HH###########HH", "HHP#########PHH", "HH###########HH", "HHP#########PHH", "HH###########HH", "HH###########HH", "HH###########HH", "#R###########R#")
                    .aisle("#NNNNNNNNNNNNN#", "#NNNAAAAAAANNN#", "##A##AAEAA##A##", "##A###AEA###A##", "##AA##VEV##AA##", "###A##V#V##A###", "###A#######A###", "###C#######C###", "###############", "###C#######C###", "###############", "###############", "#A###########A#", "###############", "#A###########A#", "###############", "###############", "###############", "###############")
                    .aisle("#NNNNNNNNNNNNN#", "#NNNNAAEAANNNN#", "#####VAEAV#####", "#####V###V#####", "###############", "###############", "###############", "###CC#####CC###", "###############", "###CC#####CC###", "###############", "###############", "#A###########A#", "###############", "#A###########A#", "###############", "###############", "###############", "###############")
                    .aisle("##NNNNNNNNNNN##", "##NNNNAEANNNN##", "###############", "###############", "###############", "###############", "###############", "####CC###CC####", "#######H#######", "####CC###CC####", "###############", "###############", "##A#########A##", "###############", "##A#########A##", "###############", "###############", "###############", "###############")
                    .aisle("##NNNNNNNNNNN##", "##NNNNNENNNNN##", "#######F#######", "#######F#######", "######AAA######", "######AAA######", "######AAA######", "#####CCCCC#####", "#######H#######", "#####CCCCC#####", "###############", "###############", "###A#######A###", "###############", "###A#######A###", "###############", "###############", "###############", "###############")
                    .aisle("###NNNNNNNNN###", "###NNNNNNNNN###", "######AAA######", "######AAA######", "######AAA######", "#######F#######", "#######F#######", "###############", "#######H#######", "#######H#######", "#######H#######", "###############", "####A##P##A####", "###############", "####A##P##A####", "###############", "###############", "###############", "###############")
                    .aisle("#####NN@NN#####", "#####NNNNN#####", "#######F#######", "#######F#######", "#######F#######", "###############", "###############", "###############", "###############", "#######H#######", "#######H#######", "#######H#######", "#####AAHAA#####", "#######H#######", "#####AAHAA#####", "#######H#######", "#######H#######", "#######H#######", "#######R#######")
                    .aisle("###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "#######H#######", "#######H#######", "#######H#######", "#######H#######", "#######H#######", "#######H#######", "#######H#######", "###############")
                    // spotless:on
                    .where("N",
                            Predicates.blocks(MoniBlocks.DIMENSIONAL_STABILIZATION_NETHERITE_CASING.get())
                                    .setMinGlobalLimited(226)
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setPreviewCount(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where("E",
                            Predicates.blocks(MoniBlocks.ELTZ_CASING.get()))
                    .where("A", Predicates.blocks(GCYMBlocks.CASING_ATOMIC.get()))
                    .where("F", Predicates.frames(MoniMaterials.Eltz))
                    .where("V", Predicates.blocks(GCYMBlocks.HEAT_VENT.get()))
                    .where("P", Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                    .where("C", Predicates.blocks(GTBlocks.SUPERCONDUCTING_COIL.get()))
                    .where("H", Predicates.blocks(GTBlocks.HIGH_POWER_CASING.get()))
                    .where("R", Predicates.blocks(GTBlocks.MACHINE_CASING_UEV.get()))
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("#", Predicates.any())
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(MoniLabs.id("block/casing/netherite"),
                    GTCEu.id("block/multiblock/processing_array"))
                    .andThen(b -> b.addDynamicRenderer(
                            MoniDynamicRenderHelper::createCreativeEnergyRender)))
            .tooltipBuilder(CREATIVE_ENERGY_MULTI_TOOLTIPS)
            .register();

    public static MultiblockMachineDefinition CREATIVE_DATA_MULTI = REGISTRATE
            .multiblock("creative_data_multi", CreativeDataMultiMachine::new)
            .langValue("Omniscience Research Beacon")
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(MoniRecipeTypes.CREATIVE_DATA_MULTI_RECIPES)
            .noRecipeModifier()
            .appearanceBlock(MoniBlocks.BIOALLOY_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    // spotless:off
                    .aisle("###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "#####CbbbC#####", "###############", "###############", "###############")
                    .aisle("####BBBBBBB####", "####BEBRBEB####", "####BBBBBBB####", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "#####CbbbC#####", "###bb#####bb###", "###############", "###############", "###############")
                    .aisle("###HBBBBBBBH###", "###BBDBABDBB###", "###MBBBBBBBM###", "#####F###F#####", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "#####CbbbC#####", "###bb#####bb###", "##b#########b##", "###############", "###############", "###############")
                    .aisle("##HHBBBBBBBHH##", "##BBBDBABDBBB##", "##M#########M##", "###M#######M###", "######F#F######", "######F#F######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "######CbC######", "####bb###bb####", "##bb#######bb##", "#b###########b#", "###############", "###############", "###############")
                    .aisle("##HHBBBBBBBHH##", "##BBBDBABDBBB##", "###############", "###############", "####M#####M####", "####M#####M####", "####M#F#F#M####", "######F#F######", "######F#F######", "###############", "###############", "###############", "###############", "###############", "###############", "####bbCbCbb####", "###b#######b###", "##b#########b##", "#b###########b#", "###############", "###############", "###############")
                    .aisle("BBBHBBBBBBBHBBB", "#BBBBDBABDBBBB#", "#F####BBB####F#", "##F#########F##", "###############", "###############", "###############", "#####M###M#####", "#####M###M#####", "#####MF#FM#####", "#####MF#FM#####", "######F#F######", "######F#F######", "######F#F######", "######CCC######", "####bb###bb####", "##Cb#######bC##", "#C###########C#", "C#############C", "###############", "###############", "###############")
                    .aisle("BBBHBBBBBBBHBBB", "#BBBBDDADDBBBB#", "#####HDGDH#####", "######DGD######", "###F##DGD##F###", "###F##DGD##F###", "####F#DGD#F####", "####F#DGD#F####", "####F#DGD#F####", "#####FDGDF#####", "#####FDGDF#####", "#####FDGDF#####", "#####FDGDF#####", "#####FDGDF#####", "#####CbbbC#####", "###CC#####CC###", "##b#########b##", "#b###########b#", "b#############b", "###############", "###############", "###############")
                    .aisle("BBBHHHHBHHHHBBB", "#BBBBDHAHDBBBB#", "#####HHAHH#####", "######GAG######", "######GAG######", "######GAG######", "######GAG######", "######GAG######", "######GAG######", "######GAG######", "######GAG######", "######GAG######", "######GAG######", "######GAG######", "#####CbAbC#####", "###bb##A##bb###", "##b####A####b##", "#b#####A#####b#", "b######A######b", "#######A#######", "#######A#######", "#######I#######")
                    .aisle("BBBHBBBBBBBHBBB", "#BBBBDDBDDBBBB#", "#####HDGDH#####", "######DGD######", "###F##DGD##F###", "###F##DGD##F###", "####F#DGD#F####", "####F#DGD#F####", "####F#DGD#F####", "#####FDGDF#####", "#####FDGDF#####", "#####FDGDF#####", "#####FDGDF#####", "#####FDGDF#####", "#####CbbbC#####", "###CC#####CC###", "##b#########b##", "#b###########b#", "b#############b", "###############", "###############", "###############")
                    .aisle("BBBHBBBBBBBHBBB", "#BBBBBBBBBBBBB#", "#F####B@B####F#", "##F#########F##", "###############", "###############", "###############", "#####M###M#####", "#####M###M#####", "#####MF#FM#####", "#####MF#FM#####", "######F#F######", "######F#F######", "######F#F######", "######CCC######", "####bb###bb####", "##Cb#######bC##", "#C###########C#", "C#############C", "###############", "###############", "###############")
                    .aisle("##HHBBBBBBBHH##", "##BBBBBBBBBBB##", "###############", "###############", "####M#####M####", "####M#####M####", "####M#F#F#M####", "######F#F######", "######F#F######", "###############", "###############", "###############", "###############", "###############", "###############", "####bbCbCbb####", "###b#######b###", "##b#########b##", "#b###########b#", "###############", "###############", "###############")
                    .aisle("##HHBBBBBBBHH##", "##BBBBBBBBBBB##", "##M#########M##", "###M#######M###", "######F#F######", "######F#F######", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "######CbC######", "####bb###bb####", "##bb#######bb##", "#b###########b#", "###############", "###############", "###############")
                    .aisle("###HHBBBBBHH###", "###BBBBBBBBB###", "###M#######M###", "#####F###F#####", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "#####CbbbC#####", "###bb#####bb###", "##b#########b##", "###############", "###############", "###############")
                    .aisle("#####BBBBB#####", "#####BBBBB#####", "#####F###F#####", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "#####CbbbC#####", "###bb#####bb###", "###############", "###############", "###############")
                    .aisle("#####BBBBB#####", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "###############", "#####CbbbC#####", "###############", "###############", "###############")
                    // spotless:on
                    .where("#", Predicates.any())
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("G", Predicates.blocks(MoniBlocks.PRISM_GLASS.get()))
                    .where("A", Predicates.blocks(GTBlocks.ADVANCED_COMPUTER_CASING.get()))
                    .where("C", Predicates.blocks(GTBlocks.COMPUTER_CASING.get()))
                    .where("H", Predicates.blocks(GTBlocks.COMPUTER_HEAT_VENT.get()))
                    .where("D", Predicates.blocks(GTBlocks.HIGH_POWER_CASING.get()))
                    .where("F", Predicates.frames(MoniMaterials.TranscendentalMatrix))
                    .where("M", Predicates.frames(MoniMaterials.CrystalMatrix))
                    .where("I", Predicates.blocks(MoniBlocks.KNOWLEDGE_TRANSMISSION_ARRAY.get()))
                    .where("R", Predicates.abilities(PartAbility.COMPUTATION_DATA_RECEPTION))
                    .where("b", Predicates.blocks(MoniBlocks.BIOALLOY_CASING.get()))
                    .where("B",
                            Predicates.blocks(MoniBlocks.BIOALLOY_CASING.get()).setMinGlobalLimited(240)
                                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMinGlobalLimited(1))
                                    .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1)))
                    .where("E", Predicates.abilities(PartAbility.INPUT_ENERGY))
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(MoniLabs.id("block/casing/bioalloy"),
                    GTCEu.id("block/multiblock/processing_array"))
                    .andThen(b -> b.addDynamicRenderer(
                            MoniDynamicRenderHelper::createCreativeDataRender)))
            .tooltipBuilder(CREATIVE_DATA_MULTI_TOOLTIPS)
            .register();

    public static MultiblockMachineDefinition SCULK_VAT = REGISTRATE
            .multiblock("sculk_vat", SculkVatMachine::new)
            .recipeTypes(MoniRecipeTypes.SCULK_VAT_RECIPES)
            .recipeModifiers(GTRecipeModifiers.OC_NON_PERFECT, MoniRecipeModifiers::sculkVatRecipeModifier)
            .appearanceBlock(MoniBlocks.CRYOLOBUS_CASING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("#CCC#", "#CLC#", "#CLC#", "#CLC#", "#CCC#", "#F#F#", "#ccc#")
                    .aisle("CCCCC", "C   C", "C   C", "C   C", "C   C", "FSSSF", "ccccc")
                    .aisle("CCCCC", "L P L", "L P L", "L P L", "C P C", "#SSS#", "ccccc")
                    .aisle("CCCCC", "C   C", "C   C", "C   C", "C   C", "FSSSF", "ccccc")
                    .aisle("#C@C#", "#CLC#", "#CLC#", "#CLC#", "#CCC#", "#F#F#", "#ccc#")
                    .where("@", Predicates.controller(Predicates.blocks(definition.get())))
                    .where("C", Predicates.blocks(MoniBlocks.CRYOLOBUS_CASING.get()).setMinGlobalLimited(47)
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS_1X).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                            .or(Predicates.machines(MoniMachines.SCULK_XP_DRAINING_HATCH).setMaxGlobalLimited(1))
                            .or(Predicates.machines(MoniMachines.SCULK_XP_SENSOR_HATCH).setPreviewCount(1))
                            .or(Predicates.machines(MoniMachines.ADVANCED_SCULK_XP_SENSOR_HATCH)))
                    .where("c", Predicates.blocks(MoniBlocks.CRYOLOBUS_CASING.get()))
                    .where("L", Predicates.blocks(GTBlocks.CASING_LAMINATED_GLASS.get()))
                    .where("F", Predicates.frames(GTMaterials.BlackSteel))
                    .where("S", Predicates.blocks(GTBlocks.FILTER_CASING_STERILE.get()))
                    .where("P", Predicates.blocks(GTBlocks.CASING_TITANIUM_PIPE.get()))
                    .where(" ", Predicates.air())
                    .where("#", Predicates.any())
                    .build())
            .modelProperty(RecipeLogic.STATUS_PROPERTY, RecipeLogic.Status.IDLE)
            .model(GTMachineModels.createWorkableCasingMachineModel(MoniLabs.id("block/casing/cryolobus"),
                    GTCEu.id("block/machines/fermenter"))
                    .andThen(b -> b.addDynamicRenderer(
                            () -> MoniDynamicRenderHelper.createSculkVatRender(0.125f,
                                    List.of(RelativeDirection.BACK, RelativeDirection.FRONT, RelativeDirection.LEFT,
                                            RelativeDirection.RIGHT)))))
            .tooltipBuilder(SCULK_VAT_TOOLTIPS)
            .register();

    // MAX stuff
    public static MachineDefinition registerLaserHatch(GTRegistrate registrate, IO io, int amperage,
                                                       PartAbility ability, int tier) {
        String name = io == IN ? "target" : "source";
        return registerTieredMachines(registrate, amperage + "a_laser_" + name + "_hatch",
                (holder, tierInner) -> new LaserHatchPartMachine(holder, io, tierInner, amperage), (tierInner, builder) -> builder
                        .langValue(VNF[tier] + "§r " + FormattingUtil.formatNumbers(amperage) + "§eA§r Laser " +
                                FormattingUtil.toEnglishName(name) + " Hatch")
                        .rotationState(RotationState.ALL)
                        .tooltips(Component.translatable("gtceu.machine.laser_hatch." + name + ".tooltip"),
                                Component.translatable("gtceu.machine.laser_hatch.both.tooltip"),
                                Component.translatable("gtceu.universal.tooltip.voltage_" + (io == IN ? "in" : "out"),
                                        FormattingUtil.formatNumbers(V[tierInner]), VNF[tierInner]),
                                Component.translatable("gtceu.universal.tooltip.amperage_in", amperage),
                                Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        FormattingUtil
                                                .formatNumbers(
                                                        EnergyHatchPartMachine.getHatchEnergyCapacity(tier, amperage))),
                                Component.translatable("gtceu.part_sharing.disabled"))
                        .abilities(ability)
                        .modelProperty(IS_FORMED, false)
                        .overlayTieredHullModel(GTCEu.id("block/machine/part/laser_" + name + "_hatch"))
                        .register(),
                tier)[0];
    }

    public static final MachineDefinition MAX_LASER_INPUT_HATCH_256 = registerLaserHatch(REGISTRATE, IN,
            256, PartAbility.INPUT_LASER, MAX);
    public static final MachineDefinition MAX_LASER_OUTPUT_HATCH_256 = registerLaserHatch(REGISTRATE, OUT,
            256, PartAbility.OUTPUT_LASER, MAX);
    public static final MachineDefinition MAX_LASER_INPUT_HATCH_1024 = registerLaserHatch(REGISTRATE, IN,
            1024, PartAbility.INPUT_LASER, MAX);
    public static final MachineDefinition MAX_LASER_OUTPUT_HATCH_1024 = registerLaserHatch(REGISTRATE, OUT,
            1024, PartAbility.OUTPUT_LASER, MAX);
    public static final MachineDefinition MAX_LASER_INPUT_HATCH_4096 = registerLaserHatch(REGISTRATE, IN,
            4096, PartAbility.INPUT_LASER, MAX);
    public static final MachineDefinition MAX_LASER_OUTPUT_HATCH_4096 = registerLaserHatch(REGISTRATE, OUT,
            4096, PartAbility.OUTPUT_LASER, MAX);

    public static final MachineDefinition EV_LASER_INPUT_HATCH_256 = registerLaserHatch(REGISTRATE, IN,
            256, PartAbility.INPUT_LASER, EV);
    public static final MachineDefinition EV_LASER_OUTPUT_HATCH_256 = registerLaserHatch(REGISTRATE, OUT,
            256, PartAbility.OUTPUT_LASER, EV);
    public static final MachineDefinition EV_LASER_INPUT_HATCH_1024 = registerLaserHatch(REGISTRATE, IN,
            1024, PartAbility.INPUT_LASER, EV);
    public static final MachineDefinition EV_LASER_OUTPUT_HATCH_1024 = registerLaserHatch(REGISTRATE, OUT,
            1024, PartAbility.OUTPUT_LASER, EV);
    public static final MachineDefinition EV_LASER_INPUT_HATCH_4096 = registerLaserHatch(REGISTRATE, IN,
            4096, PartAbility.INPUT_LASER, EV);
    public static final MachineDefinition EV_LASER_OUTPUT_HATCH_4096 = registerLaserHatch(REGISTRATE, OUT,
            4096, PartAbility.OUTPUT_LASER, EV);

    public static void init() {}
}
