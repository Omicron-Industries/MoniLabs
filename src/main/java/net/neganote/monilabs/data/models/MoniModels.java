package net.neganote.monilabs.data.models;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.neganote.monilabs.MoniLabs;
import net.neganote.monilabs.common.block.PrismaticActiveBlock;
import net.neganote.monilabs.common.machine.multiblock.Color;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import static net.neganote.monilabs.common.block.PrismaticActiveBlock.COLOR;

public class MoniModels {

    public static NonNullBiConsumer<DataGenContext<Block, PrismaticActiveBlock>, RegistrateBlockstateProvider> createPrismaticActiveBlockModel(ResourceLocation texturePath) {
        return (ctx, prov) -> {
            ActiveBlock block = ctx.getEntry();

            ModelFile inactive = prov
                    .models()
                    .cubeAll(ctx.getName(), texturePath.withPrefix("block/"));

            VariantBlockStateBuilder builder = prov
                    .getVariantBuilder(block)
                    .partialState()
                    .with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState()
                    .modelFile(inactive)
                    .addModel();

            ResourceLocation location = texturePath.withPrefix("block/");
            for (Color color : Color.ACTUAL_COLORS) {
                String colorStr = "_" + color.name().toLowerCase();
                ModelFile file = prov
                        .models()
                        .cubeAll(ctx.getName() + colorStr, location.withSuffix(colorStr));

                builder
                        .partialState()
                        .with(GTBlockStateProperties.ACTIVE, true)
                        .with(COLOR, color.key)
                        .modelForState()
                        .modelFile(file)
                        .addModel();
            }
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, ActiveBlock>, RegistrateBlockstateProvider> createActiveBlockModel(ResourceLocation texturePath) {
        return (ctx, prov) -> {
            ActiveBlock block = ctx.getEntry();

            ModelFile inactive = prov
                    .models()
                    .cubeAll(ctx.getName(), texturePath);

            VariantBlockStateBuilder builder = prov
                    .getVariantBuilder(block)
                    .partialState()
                    .with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState()
                    .modelFile(inactive)
                    .addModel();

            ModelFile active = prov
                    .models()
                    .cubeAll(ctx.getName(), texturePath.withSuffix("_active"));

            builder
                    .partialState()
                    .with(GTBlockStateProperties.ACTIVE, true)
                    .modelForState()
                    .modelFile(active)
                    .addModel();
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, PrismaticActiveBlock>, RegistrateBlockstateProvider> createPrismaticActiveUpsideDownBeaconModel(ResourceLocation texturePath) {
        return (ctx, prov) -> {
            ActiveBlock block = ctx.getEntry();

            ResourceLocation path = texturePath.withPrefix("block/");
            ResourceLocation framePath = path.withSuffix("/frame");
            ResourceLocation corePath = path.withSuffix("/core");

            ResourceLocation beaconPath = MoniLabs.id("block/beacon_upsidedown");

            ModelFile inactive = prov
                    .models()
                    .withExistingParent(ctx.getName(), beaconPath)
                    .texture("frame", framePath)
                    .texture("core", corePath)
                    .texture("particle", framePath)
                    .renderType("cutout");

            VariantBlockStateBuilder builder = prov
                    .getVariantBuilder(block)
                    .partialState()
                    .with(GTBlockStateProperties.ACTIVE, false)
                    .modelForState()
                    .modelFile(inactive)
                    .addModel();

            for (Color color : Color.ACTUAL_COLORS) {
                String colorStr = "_" + color.name().toLowerCase();
                ModelFile file = prov
                        .models()
                        .withExistingParent(ctx.getName() + colorStr, beaconPath)
                        .texture("frame", framePath)
                        .texture("core", corePath.withSuffix(colorStr))
                        .texture("particle", framePath)
                        .renderType("cutout");

                builder
                        .partialState()
                        .with(GTBlockStateProperties.ACTIVE, true)
                        .with(COLOR, color.key)
                        .modelForState()
                        .modelFile(file)
                        .addModel();
            }
        };
    }
}
