package net.neganote.monilabs.mixin;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class MoniMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("MoniLabs-Mixin");
    private static final boolean VERBOSE = Boolean.parseBoolean(System.getProperty("monilabs.mixin.verbose", "false"));

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
        if (mixinClassName.startsWith("net.neganote.monilabs.mixin.enderio.")) {
            apply = isModLoaded("enderio");
        }
        if (mixinClassName.startsWith("net.neganote.monilabs.mixin.easyanvils.")) {
            apply = isModLoaded("easyanvils");
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
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
