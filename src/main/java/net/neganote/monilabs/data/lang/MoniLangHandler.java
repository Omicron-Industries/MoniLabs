package net.neganote.monilabs.data.lang;

import net.neganote.monilabs.MoniLabs;
import net.neganote.monilabs.config.MoniConfig;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import dev.toma.configuration.config.value.ConfigValue;
import dev.toma.configuration.config.value.ObjectValue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MoniLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("monilabs.prismatic.current_color", "Current color: %s");
        provider.add("monilabs.prismatic.color_name.red", "§4Red§r");
        provider.add("monilabs.prismatic.color_name.orange", "§6Orange§r");
        provider.add("monilabs.prismatic.color_name.yellow", "§eYellow§r");
        provider.add("monilabs.prismatic.color_name.lime", "§aLime§r");
        provider.add("monilabs.prismatic.color_name.green", "§2Green§r");
        provider.add("monilabs.prismatic.color_name.teal", "§3Teal§r");
        provider.add("monilabs.prismatic.color_name.cyan", "§bCyan§r");
        provider.add("monilabs.prismatic.color_name.azure", "§9Azure§r");
        provider.add("monilabs.prismatic.color_name.blue", "§1Blue§r");
        provider.add("monilabs.prismatic.color_name.indigo", "§5Indigo§r");
        provider.add("monilabs.prismatic.color_name.magenta", "§dMagenta§r");
        provider.add("monilabs.prismatic.color_name.pink", "§cPink§r");

        provider.add("monilabs.omnic.current_diversity_points", "Current omnium progress: %s");

        provider.add("monilabs.recipe.required_color", "Required Initial Color:\n%s");
        provider.add("monilabs.recipe.result_color", "Resulting Color: \n%s");
        provider.add("monilabs.recipe.result_color_relative", "Resulting Color Increment: \n%s");
        provider.add("monilabs.recipe.color_list_random_start", "Possible Resulting Colors: \n");
        provider.add("monilabs.recipe.color_list_random_start_relative", "Possible Color Increments: \n");
        provider.add("monilabs.recipe.color_list_separator", ", ");
        provider.add("monilabs.recipe.fully_random_color",
                "Resulting Color State: \n§5R§dA§4N§cD§eO§aM §bC§3O§7L§1O§5R§r!");
        provider.add("monilabs.recipe.accepted_colors", "Accepted Initial Colors: ");
        provider.add("monilabs.recipe.primary_input", "Primary");
        provider.add("monilabs.recipe.secondary_input", "Secondary");
        provider.add("monilabs.recipe.basic_input", "Primary | Secondary");
        provider.add("monilabs.recipe.tertiary_input", "Tertiary");
        provider.add("monilabs.recipe.any_input_color", "ANY");
        provider.add("monilabs.recipe.input_color_not", "NOT %s");
        provider.add("gtceu.machine.parallel_hatch_mk9.tooltip", "Allows to run up to 1024 recipes in parallel.");
        provider.add("gtceu.machine.parallel_hatch_mk10.tooltip", "Allows to run up to 4096 recipes in parallel.");

        multiLang(provider, "monilabs.multiblock.duplicate",
                "This multiblock is a duplicate",
                "Only one can exist");

        provider.add("monilabs.recipe.mistake_input_colors", "You made a mistake defining this recipe's input colors.");
        provider.add("monilabs.recipe.mistake_output_colors",
                "You made a mistake defining this recipe's output colors.");
        provider.add("gtceu.chromatic_processing", "Chromatic Processing");
        provider.add("gtceu.chromatic_transcendence", "Chromatic Transcendence");
        provider.add("gtceu.microverse", "Microverse Mission");
        provider.add("gtceu.creative_data_multi", "Omniscience Research Beacon");
        provider.add("gtceu.creative_energy_multi", "Transdimensional Energy Singularity");

        provider.add("monilabs.tooltip.prismatic_core", "The heart of the Prismatic Crucible!");

        provider.add("monilabs.tooltip.prismatic.rainbow", "the rainbow");

        multiLang(provider, "monilabs.tooltip.prismatic",
                "§7Uses the power of %s to transform items!",
                "§7Run recipes to §fswitch§7 the §fcolor§7.",
                "§7The color state §f§iMUST§7 be §fcorrect§7 for recipes to §frun§7!");

        multiLang(provider, "monilabs.tooltip.creative_energy_multi_description",
                "§7Imbues your factory with the power of the %s!",
                "§7While this machine is §frunning§7, all §fenergy containers§7 act like they are full of §fenergy§7.");
        provider.add("monilabs.tooltip.universe_lerp", "Universe");

        multiLang(provider, "monilabs.tooltip.creative_data_multi_description",
                "§7Imbues §r§7your factory with the knowledge of the %s!",
                "§7While this machine is §frunning§7, every §foptical data hatch§7 and §fdata access hatch§7 acts like it has all possible §fresearch§7.");

        multiLang(provider, "monilabs.tooltip.sculk_vat_description",
                "§7Uses the power of %s §7from the §2deepest §7depths of the earth to grow organic materials.",
                "§7The further the §foutput hatch §7is from being §fhalf-full§7, the more of the product is lost.");
        provider.add("monilabs.tooltip.sculk_lerp", "Sculk");

        multiLang(provider, "tooltip.monilabs.basic_microverse_projector.description",
                "§7A machine capable of projecting %s §7for basic miner missions.",
                "§7Runs §fmicrominer§7 missions to gather resources from the §fmicroverse§7.",
                "§7A microverse §ffirst§7 needs to be projected with a §fspecial mission§7, then resource missions can be run.");

        multiLang(provider, "tooltip.monilabs.hostile_microverse",
                "§7Hostile microverses are also available.",
                "§7All hostile missions restore §fintegrity§7, required for the microverse to stay active.");

        multiLang(provider, "tooltip.monilabs.advanced_microverse_projector.description",
                "§7An §Iadvanced machine capable of projecting %s for complex missions into the deepest parts of space.",
                "§7Can §fperfectly overclock§7 lower projector §ftier§7 missions §fonce§7.",
                "§fHigher tier missions§7 drain a small amount of §fintegrity§7 each. Needs to be §ffed§7 quantum flux to §frestore§7 integrity.");

        multiLang(provider, "tooltip.monilabs.elite_microverse_projector.description",
                "§7An §Ielite machine capable of projecting special %s §7for exotic missions on a %s scale.",
                "§7Can §fperfectly overclock§7 lower projector §ftier§7 missions §fonce§7 for each §ftier§7 above the §frequired§f mission tier.",
                "§7Can also host §fshattered§7 and §fcorrupted§7 microverses.");
        // §7An §Iepic§7 machine capable of projecting %s §7for hundreds of missions at once.
        multiLang(provider, "tooltip.monilabs.hyperbolic_microverse_projector.description",
                "§7An §Iepic machine capable of projecting %s §7for hundreds of missions at once.",
                "§7Can §fperfectly overclock§7 lower projector §ftier§7 missions §fonce§7 for each §ftier§7 above the §frequired§7 mission tier.",
                "§7Some missions are §fexempt§7 from being able to be §fparalleled§7.");

        provider.add("monilabs.failure_reason.improper_xp", "XP buffer is outside necessary range");
        provider.add("monilabs.failure_reason.insufficient_projector_tier", "Insufficient Projector Tier");

        provider.add("monilabs.tooltip.hyper_desc",
                "§7An §Iepic machine capable of projecting %s §7for hundreds of missions at once.");
        provider.add("monilabs.tooltip.microverse.space_gradient", "Microverse");
        provider.add("monilabs.tooltip.microverses.space_gradient", "Microverses");
        provider.add("monilabs.tooltip.microversal.space_gradient", "Microversal");

        provider.add("emi_info.monilabs.projector_info", "Projector tier: %d (%s)");
        provider.add("emi_info.monilabs.required_microverse", "Required type: %s");
        provider.add("emi_info.monilabs.new_microverse", "New type: %s");
        provider.add("emi_info.monilabs.integrity_drained", "Integrity drained: %.2f");
        provider.add("emi_info.monilabs.integrity_healed", "Integrity healed: %.2f");
        provider.add("emi_info.monilabs.cannot_parallel", "Cannot parallel");

        provider.add("monilabs.menu.packmodeswitcher.displayname", "Switch Pack Mode");
        provider.add("monilabs.menu.packmodeswitcher.description",
                "Copies the required files for the pack mode needed");
        provider.add("monilabs.menu.SaveTmpModeFileAction.displayname", "Save pack mode something something");
        provider.add("monilabs.menu.SaveTmpModeFileAction.description",
                "Saves the letter that packmodeswitcher reads");
        provider.add("monilabs.menu.SaveTmpModeFileAction.valuename", "(N/H/E) Letter");

        provider.add("monilabs.commands.helpnormal", "Normal Mode - The Default mode");
        provider.add("monilabs.commands.helphard",
                "Hard Mode - Adds more lines and progression, removes HNN and Monicoin spending");
        provider.add("monilabs.commands.helpexpert",
                "Expert Mode - A modifier for hard, enables some of the more extreme GTm settings among other things");

        multiLang(provider, "emi_info.creative_energy_multi",
                "Imbues your factory with",
                "Infinite Power");
        multiLang(provider, "emi_info.creative_data_multi",
                "Imbues your factory with",
                "the Wisdom of the Universe",
                "(All GT Research)");

        multiLang(provider, "monilabs.tooltip.chroma_sensor_hatch",
                "Outputs color state of Prismatic Crucible",
                "as a redstone signal. 0 for unformed,",
                "1-12 for each color the PrismaC can be in.");

        multiLang(provider, "monilabs.tooltip.advanced_chroma_sensor_hatch",
                "Outputs a redstone signal when the",
                "Prismatic Crucible is in a specified color.");

        multiLang(provider, "tooltip.monilabs.xp_draining_hatch",
                "Drains all XP to the Sculk Vat's internal",
                "buffer for its processing.",
                "Buffer continuously decays slightly.");

        multiLang(provider, "tooltip.monilabs.xp_sensor_hatch",
                "Outputs XP buffer status of Sculk Vat",
                "as a redstone signal.");

        provider.add("tooltip.monilabs.advanced_sensor_hatch", "as a configurable redstone signal.");

        multiLang(provider, "tooltip.monilabs.microverse_stability_hatch",
                "Outputs microverse stability",
                "as a redstone signal.");

        multiLang(provider, "tooltip.monilabs.microverse_type_hatch",
                "Outputs microverse type",
                "as a redstone signal.",
                "0: None",
                "1: Normal",
                "2: Shattered",
                "3: Corrupted");

        multiLang(provider, "tooltip.monilabs.advanced_microverse_type_hatch",
                "Outputs a redstone signal when the",
                "Microverse projector has projected a specified microverse type.");

        provider.add("tooltip.monilabs.microverse_type_hatch.hostile", "4: Hostile");

        provider.add("microverse.monilabs.type.none", "None");
        provider.add("microverse.monilabs.type.normal", "Normal");
        provider.add("microverse.monilabs.type.hostile", "Hostile");
        provider.add("microverse.monilabs.type.shattered", "Shattered");
        provider.add("microverse.monilabs.type.corrupted", "Corrupted");

        provider.add("projector.monilabs.tier.1", "Basic");
        provider.add("projector.monilabs.tier.2", "Advanced");
        provider.add("projector.monilabs.tier.3", "Elite");
        provider.add("projector.monilabs.tier.4", "Hyperbolic");

        provider.add("microverse.monilabs.current_microverse", "Microverse type: %s");
        provider.add("microverse.monilabs.integrity", "Microverse integrity: %.2f%%");

        provider.add("sculk_vat.monilabs.current_xp_buffer", "XP Stored: %d/%d");

        provider.add("config.jade.plugin_monilabs.color_info", "Prismatic Crucible Color Info");
        provider.add("config.jade.plugin_monilabs.microverse_info", "Microverse Projector Info");
        provider.add("config.jade.plugin_monilabs.sculk_vat_xp_info", "Sculk Vat XP Buffer Info");
        provider.add("config.jade.plugin_monilabs.omnic_synth_info", "Omnic Synthesizer Info");

        provider.add("gui.ae2.With", "With");

        multiLang(provider, "gtceu.placeholder_info.prismacColor",
                "Returns the current color of the Prismatic Crucible.",
                "Usage:",
                "  {prismacColor} -> Current color: (insert color here)");

        multiLang(provider, "gtceu.placeholder_info.microverseType",
                "Returns the type of microverse.",
                "Usage:",
                "  {microverseType} -> Microverse type: (insert type here)");

        multiLang(provider, "gtceu.placeholder_info.microverseStability",
                "Returns the stability of the microverse.",
                "Note that not having a microverse projected may result in nonsense values of integrity.",
                "Usage:",
                "  {microverseStability} -> Microverse integrity: (integrity, in percent)");

        multiLang(provider, "gtceu.placeholder_info.sculkVatXPBuffer",
                "Returns the current XP buffer of the Sculk Vat.",
                "Usage:",
                "  {sculkVatXPBuffer} -> XP Stored: (value in millibuckets)");

        multiLang(provider, "emi_info.monilabs.multiblock.sculk_vat",
                "Outputs may be up to 8x less",
                "when the output hatch is not",
                "half full (this is the maximum)",
                "Min XP: %d, Max: %d");

        provider.add("gui.monilabs.chroma.color.display", "Color: ");
        provider.add("gui.monilabs.chroma.color.unknown", "Unknown");
        provider.add("gui.monilabs.microverse_stability.min_threshold", "Set minimum threshold");
        provider.add("gui.monilabs.microverse_stability.max_threshold", "Set maximum threshold");
        provider.add("gui.monilabs.microverse_stability.min", "Min: ");
        provider.add("gui.monilabs.microverse_stability.max", "Max: ");
        provider.add("gui.advanced_chroma_sensor.display", "Type: ");
        provider.add("gui.advanced_chroma_sensor.none", "None");
        provider.add("gui.monilabs.xp_sensor.mode_percentage", "Mode: Percentage (%%) ");
        provider.add("gui.monilabs.xp_sensor.mode_raw_amount", "Mode: Raw Amount (XP) ");
        provider.add("gui.monilabs.xp_sensor.min_threshold", "Set minimum threshold");
        provider.add("gui.monilabs.xp_sensor.max_threshold", "Set maximum threshold");
        provider.add("gui.monilabs.xp_sensor.min", "Min: ");
        provider.add("gui.monilabs.xp_sensor.max", "Max: ");

        var advancedChromaSensorHatchDescription = "Toggle to invert the redstone logic\nBy default, redstone is emitted when the color chosen matches the active color of the Prismatic Crucible.";
        multilineLang(provider, "gui.advanced_chroma_sensor.invert.enabled",
                "Output: Inverted\n\n" + advancedChromaSensorHatchDescription);
        multilineLang(provider, "gui.advanced_chroma_sensor.invert.disabled",
                "Output: Normal\n\n" + advancedChromaSensorHatchDescription);

        var advancedMicroverseTypeSensorHatchDescription = "Toggle to invert the redstone logic\nBy default, redstone is emitted when the type chosen matches the active microverse type in the Microverse Projector.";
        multilineLang(provider, "gui.advanced_type_sensor.invert.enabled",
                "Output: Inverted\n\n" + advancedMicroverseTypeSensorHatchDescription);
        multilineLang(provider, "gui.advanced_type_sensor.invert.disabled",
                "Output: Normal\n\n" + advancedMicroverseTypeSensorHatchDescription);

        var advancedSculkExperienceSensorHatchDescription = "Toggle to invert the redstone logic\nBy default, redstone is emitted when the raw value or percentage matches the amount of XP in the internal buffer of the Sculk Vat.";
        multilineLang(provider, "gui.monilabs.xp_sensor.invert.enabled",
                "Output: Inverted\n\n" + advancedSculkExperienceSensorHatchDescription);
        multilineLang(provider, "gui.monilabs.xp_sensor.invert.disabled",
                "Output: Normal\n\n" + advancedSculkExperienceSensorHatchDescription);

        var advancedMicroverseStabilitySensorHatchDescription = "Toggle to invert the redstone logic\nBy default, redstone is emitted when the percentage matches the current stability of the Microverse Projector.";
        multilineLang(provider, "gui.monilabs.microverse_stability_sensor.invert.enabled",
                "Output: Inverted\n\n" + advancedMicroverseStabilitySensorHatchDescription);
        multilineLang(provider, "gui.monilabs.microverse_stability_sensor.invert.disabled",
                "Output: Normal\n\n" + advancedMicroverseStabilitySensorHatchDescription);

        multilineLang(provider, "gui.monilabs.xp_sensor.mode_toggle.enabled",
                "Mode: Percentage \n\nToggle to select the mode of Sculk Experience Signal.\nIn this mode, redstone is emitted when the amount of XP in the Sculk Vat matches the percentages");
        multilineLang(provider, "gui.monilabs.xp_sensor.mode_toggle.disabled",
                "Mode: Raw Amount\n\nToggle to select the mode of Sculk Experience Signal.\nIn this mode, redstone is emitted when the amount of XP in the Sculk Vat matches the raw values");

        dfs(provider, new HashSet<>(), MoniConfig.CONFIG_HOLDER.getValueMap());
    }

    private static void dfs(RegistrateLangProvider provider, Set<String> added, Map<String, ConfigValue<?>> map) {
        for (var entry : map.entrySet()) {
            var id = entry.getValue().getId();
            if (added.add(id)) {
                provider.add(String.format("config.%s.option.%s", MoniLabs.MOD_ID, id), id);
            }
            if (entry.getValue() instanceof ObjectValue objectValue) {
                dfs(provider, added, objectValue.get());
            }
        }
    }

    protected static void multiLang(RegistrateLangProvider provider, String key, String... values) {
        for (var i = 0; i < values.length; i++) {
            provider.add(getSubKey(key, i), values[i]);
        }
    }

    protected static void multilineLang(RegistrateLangProvider provider, String key, String multiline) {
        var lines = multiline.split("\n");
        multiLang(provider, key, lines);
    }

    protected static String getSubKey(String key, int index) {
        return key + "." + index;
    }
}
