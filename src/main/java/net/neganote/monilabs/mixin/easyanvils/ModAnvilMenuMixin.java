package net.neganote.monilabs.mixin.easyanvils;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraftforge.fml.LogicalSide;
import net.neganote.monilabs.packmode.xpModeCost;

import fuzs.easyanvils.world.inventory.ModAnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(ModAnvilMenu.class)
public abstract class ModAnvilMenuMixin extends AnvilMenu {

    public ModAnvilMenuMixin(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(containerId, inventory, access);
    }

    @Shadow(remap = false)
    public abstract void setCost(int cost);

    // Let Easy Anvils handle the validation before wiping the XP demand
    @Inject(method = "createResult()V", at = @At("RETURN"))
    private void monilabs$applyXpCostPolicy(CallbackInfo ci) {
        LogicalSide side = player.level().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
        if (xpModeCost.isFree(side) && !resultSlots.getItem(0).isEmpty()) {
            setCost(0);
            broadcastChanges();
        }
    }
}
