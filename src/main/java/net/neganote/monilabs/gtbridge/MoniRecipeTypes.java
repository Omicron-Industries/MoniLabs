package net.neganote.monilabs.gtbridge;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;

import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.resources.language.I18n;
import net.neganote.monilabs.capability.recipe.MoniRecipeCapabilities;
import net.neganote.monilabs.client.gui.MoniGuiTextures;
import net.neganote.monilabs.common.data.MoniSounds;
import net.neganote.monilabs.common.machine.multiblock.Color;
import net.neganote.monilabs.common.machine.multiblock.Microverse;
import net.neganote.monilabs.common.machine.multiblock.MicroverseProjectorMachine;

@SuppressWarnings("unused")
public class MoniRecipeTypes {

    public static GTRecipeType createPrismaCRecipeType(String name) {
        return GTRecipeTypes
                .register(name, GTRecipeTypes.MULTIBLOCK)
                .setMaxSize(IO.BOTH, MoniRecipeCapabilities.CHROMA, 1)
                .addDataInfo(data -> " ")
                .addDataInfo(data -> " ")
                .addDataInfo(data -> {
                    if (data.contains("output_states")) {
                        boolean isRelative = (data.contains("color_change_relative") &&
                                data.getBoolean("color_change_relative"));
                        int outputStatesCount = data.getInt("output_states");

                        if (isRelative) {
                            if (outputStatesCount == 1) {
                                int modulus = data.getInt("output_states_0");
                                return LocalizationUtils.format("monilabs.recipe.result_color_relative",
                                        String.valueOf(modulus));
                            }
                            if (outputStatesCount == Color.COLOR_COUNT) {
                                return LocalizationUtils.format("monilabs.recipe.fully_random_color");
                            }
                            StringBuilder builder = new StringBuilder(
                                    LocalizationUtils.format("monilabs.recipe.color_list_random_start_relative"));
                            for (int i = 0; i < outputStatesCount; i++) {
                                builder.append(LocalizationUtils
                                        .format(Color.getColorFromKey(data.getInt("output_states_" + i)).nameKey));
                                if (i % 3 == 2) {
                                    builder.append("\n");
                                } else {
                                    builder.append(LocalizationUtils.format("monilabs.recipe.color_list_separator"));
                                }

                            }
                            return builder.toString();

                        } else {
                            if (outputStatesCount == 1) {
                                int modulus = data.getInt("output_states_0");
                                return LocalizationUtils.format("monilabs.recipe.result_color",
                                        LocalizationUtils.format(Color.getColorFromKey(modulus).nameKey));
                            }
                            if (outputStatesCount == Color.COLOR_COUNT) {
                                return LocalizationUtils.format("monilabs.recipe.fully_random_color");
                            }
                            StringBuilder builder = new StringBuilder(
                                    LocalizationUtils.format("monilabs.recipe.color_list_random_start"));
                            for (int i = 0; i < outputStatesCount; i++) {
                                builder.append(LocalizationUtils
                                        .format(Color.getColorFromKey(data.getInt("output_states_" + i)).nameKey));
                                if (i % 3 == 2) {
                                    builder.append("\n");
                                } else {
                                    builder.append(LocalizationUtils.format("monilabs.recipe.color_list_separator"));
                                }

                            }
                            return builder.toString();
                        }
                    }
                    // Default behavior
                    return LocalizationUtils.format("monilabs.recipe.fully_random_color");
                })
                .addDataInfo(data -> " ")
                .addDataInfo(data -> " ")
                .addDataInfo(data -> " ")
                .addDataInfo(data -> " ")
                .setMaxTooltips(8)
                .setMaxIOSize(3, 3, 1, 1)
                .setEUIO(IO.IN)
                .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.LEFT_TO_RIGHT);
    }

    public static GTRecipeType CHROMATIC_PROCESSING = createPrismaCRecipeType("chromatic_processing");
    public static GTRecipeType CHROMATIC_TRANSCENDENCE = createPrismaCRecipeType("chromatic_transcendence");

    public static GTRecipeType MICROVERSE_RECIPES = GTRecipeTypes
            .register("microverse", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxSize(IO.IN, MoniRecipeCapabilities.MICROVERSE, 1)
            .setMaxIOSize(9, 9, 3, 0)
            .setSlotOverlay(false, false, GuiTextures.ARROW_INPUT_OVERLAY)
            .setProgressBar(MoniGuiTextures.PROGRESS_BAR_ROCKET, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(MoniSounds.MICROVERSE)
            .addDataInfo(data -> "")
            .addDataInfo((data) -> I18n.get("emi_info.monilabs.projector_info",
                    data.getByte("projector_tier"), I18n.get("projector.monilabs.tier." + data.getByte("projector_tier"))))
            .addDataInfo(data -> {
                String info = "";
                if (data.contains("updated_microverse")) {
                    info += I18n.get("emi_info.monilabs.new_microverse",
                            I18n.get(Microverse.values()[data.getInt("updated_microverse")].langKey)) + "\n";
                }
                if (data.contains("damage_rate") && data.getInt("damage_rate") != 0) {
                    var damageRate = data.getInt("damage_rate");
                    if (damageRate > 0) {
                        info += I18n.get("emi_info.monilabs.integrity_drained", (float) (data.getInt("damage_rate") *
                                data.getInt("duration")) / MicroverseProjectorMachine.FLUX_REPAIR_AMOUNT) + "%%\n";
                    } else {
                        info += I18n.get("emi_info.monilabs.integrity_healed", (float) (-data.getInt("damage_rate") *
                                data.getInt("duration")) / MicroverseProjectorMachine.FLUX_REPAIR_AMOUNT) + "%%\n";
                    }
                    // The extra percent is because EMI treats anything with a percent in it as a format string
                }
                if (data.contains("blacklistParallel") && data.getBoolean("blacklistParallel")) {
                    info += I18n.get("emi_info.monilabs.cannot_parallel");
                }
                return info;
            })
            .addDataInfo(data -> "")
            .addDataInfo(data -> "");

    public static GTRecipeType ANTIMATTER_COLLIDER_RECIPES = GTRecipeTypes
            .register("anti_collider", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.OUT)
            .setMaxIOSize(0, 0, 2, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.ALWAYS_FULL);

    public static GTRecipeType CREATIVE_ENERGY_MULTI_RECIPES = GTRecipeTypes
            .register("creative_energy_multi", GTRecipeTypes.MULTIBLOCK)
            .setMaxIOSize(0, 0, 2, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.ALWAYS_FULL)
            .setSound(GTSoundEntries.COMBUSTION)
            .addDataInfo(data -> LocalizationUtils.format("emi_info.creative_energy_multi.0"))
            .addDataInfo(data -> LocalizationUtils.format("emi_info.creative_energy_multi.1"));

    public static GTRecipeType CREATIVE_DATA_MULTI_RECIPES = GTRecipeTypes
            .register("creative_data_multi", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(0, 0, 1, 0)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW, ProgressTexture.FillDirection.ALWAYS_FULL)
            .setSound(GTSoundEntries.COMPUTATION)
            .addDataInfo(data -> "")
            .addDataInfo(data -> LocalizationUtils.format("emi_info.creative_data_multi.0"))
            .addDataInfo(data -> LocalizationUtils.format("emi_info.creative_data_multi.1"))
            .addDataInfo(data -> LocalizationUtils.format("emi_info.creative_data_multi.2"));

    public static GTRecipeType SCULK_VAT_RECIPES = GTRecipeTypes
            .register("sculk_vat", GTRecipeTypes.MULTIBLOCK)
            .setEUIO(IO.IN)
            .setMaxIOSize(2, 0, 3, 1)
            .setProgressBar(GuiTextures.PROGRESS_BAR_ARROW_MULTIPLE, ProgressTexture.FillDirection.LEFT_TO_RIGHT)
            .setSound(GTSoundEntries.CHEMICAL)
            .addDataInfo((data) -> LocalizationUtils.format("emi_info.monilabs.multiblock.sculk_vat.0"))
            .addDataInfo((data) -> LocalizationUtils.format("emi_info.monilabs.multiblock.sculk_vat.1"))
            .addDataInfo((data) -> LocalizationUtils.format("emi_info.monilabs.multiblock.sculk_vat.2"))
            .addDataInfo((data) -> {
                if (data.contains("minimumXp") && data.contains("maximumXp")) {
                    int minimumXp = data.getInt("minimumXp");
                    int maximumXp = data.getInt("maximumXp");
                    return LocalizationUtils.format("emi_info.monilabs.multiblock.sculk_vat.3", minimumXp, maximumXp);
                } else {
                    return "";
                }
            })
            .addDataInfo((data) -> "");

    public static void init() {}
}
