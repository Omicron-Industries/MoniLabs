package net.neganote.monilabs.mixin;

import net.minecraft.world.level.ItemLike;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import com.glodblock.github.extendedae.common.EAERegistryHandler;
import com.glodblock.github.extendedae.common.EPPItemAndBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EAERegistryHandler.class)
public class MixinEAERegistryHandler {

    @Inject(method = "registerAEUpgrade", at = @At("TAIL"), remap = false)
    private void injectAutoCompleteCard(CallbackInfo ci) {
        Upgrades.add((ItemLike) AEItems.AUTO_COMPLETE_CARD, (ItemLike) EPPItemAndBlock.EX_PATTERN_PROVIDER, 1);
        Upgrades.add((ItemLike) AEItems.AUTO_COMPLETE_CARD, (ItemLike) EPPItemAndBlock.EX_PATTERN_PROVIDER_PART, 1);
    }
}
