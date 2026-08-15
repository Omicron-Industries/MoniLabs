package net.neganote.monilabs.mixin;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.neganote.monilabs.mixin.asm.AsmPatch;
import net.neganote.monilabs.mixin.asm.CraftAmountMenuOpenPatch;
import net.neganote.monilabs.mixin.asm.CraftingJobStatusPacketCtorPatch;
import net.neganote.monilabs.mixin.asm.EncodedPatternItemStackComponentPatch;
import net.neganote.monilabs.mixin.asm.GuiTextWithConstantPatch;
import net.neganote.monilabs.mixin.asm.PatternDetailsHelperEncodePatch;
import net.neganote.monilabs.mixin.asm.ProcessingPatternItemEncodePatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MoniMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("MoniLabs-Mixin");
    private static final boolean VERBOSE = Boolean.parseBoolean(System.getProperty("monilabs.mixin.verbose", "false"));

    private static final Set<String> AAE_MIXINS = Set.of(
            "net.neganote.monilabs.mixin.aae.MixinCraftingJobStatusPacketCtorShim",
            "net.neganote.monilabs.mixin.aae.MixinCraftingServiceAAE",
            "net.neganote.monilabs.mixin.aae.MixinEncodedPatternItemShim",
            "net.neganote.monilabs.mixin.aae.MixinGuiTextShim",
            "net.neganote.monilabs.mixin.aae.MixinPatternDetailsHelperShim",
            "net.neganote.monilabs.mixin.aae.MixinProcessingPatternItemShim");

    private static final Set<String> AE2WTLIB_MIXINS = Set.of(
            "net.neganote.monilabs.mixin.ae2wtlib.MixinCraftAmountMenuShim");

    private static final Map<String, AsmPatch> PATCHES = Stream.of(
            new CraftingJobStatusPacketCtorPatch(),
            new PatternDetailsHelperEncodePatch(),
            new ProcessingPatternItemEncodePatch(),
            new EncodedPatternItemStackComponentPatch(),
            new GuiTextWithConstantPatch(),
            new CraftAmountMenuOpenPatch())
            .collect(Collectors.toMap(AsmPatch::targetInternalName, Function.identity()));

    private static boolean isModLoaded(String modId) {
        try {
            if (ModList.get() == null) {
                return LoadingModList.get().getMods().stream()
                        .map(ModInfo::getModId)
                        .anyMatch(modId::equals);
            } else {
                return ModList.get().isLoaded(modId);
            }
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        if (VERBOSE) {
            LOGGER.info("[MoniMixinPlugin] Plugin onLoad: {}", mixinPackage);
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean apply = true;
        if (AAE_MIXINS.contains(mixinClassName)) {
            apply = isModLoaded("advanced_ae");
        }
        if (AE2WTLIB_MIXINS.contains(mixinClassName)) {
            apply = isModLoaded("ae2wtlib");
        }
        boolean isDatagen = System.getProperty("sun.java.command").contains("dataRun");
        if (mixinClassName.toLowerCase().contains("render") && isDatagen) {
            apply = false;
        }
        if (targetClassName.contains("jellysquid.mods.sodium")) {
            apply = isModLoaded("embeddium");
        }
        if (VERBOSE) {
            LOGGER.info("[MoniMixinPlugin] {} {} -> {}", apply ? "APPLY" : "SKIP", mixinClassName, targetClassName);
        }
        return apply;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        AsmPatch patch = PATCHES.get(targetClassName.replace('.', '/'));
        if (patch == null) return;

        try {
            patch.apply(targetClass);
            if (VERBOSE) {
                LOGGER.info("[MoniMixinPlugin] PATCH {} | {} => {}", targetClassName, patch.description(),
                        patch.isPresent(targetClass) ? "OK" : "FAILED");
            }
        } catch (Throwable t) {
            LOGGER.error("[MoniMixinPlugin] postApply patch failed for target {}", targetClassName, t);
        }
    }
}
