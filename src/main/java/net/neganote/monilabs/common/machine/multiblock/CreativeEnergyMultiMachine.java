package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public class CreativeEnergyMultiMachine extends UniqueWorkableElectricMultiblockMachine {

    private static final Map<UUID, Integer> ACTIVE_OWNER_COUNTS = new HashMap<>();

    private static final Map<UUID, CacheEntry> PLAYER_CACHE = new HashMap<>();

    private static final int CACHE_LIFETIME_TICKS = 20;

    private record CacheEntry(boolean enabled, long cachedAtTick) {}

    private final ConditionalSubscriptionHandler creativeEnergySubscription;

    private boolean currentlyActive = false;

    public CreativeEnergyMultiMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);

        this.creativeEnergySubscription = new ConditionalSubscriptionHandler(this, this::tickEnableCreativeEnergy,
                this::isSubscriptionActive);
    }

    public static boolean isCreativeEnergyEnabledFor(UUID playerUUID) {
        if (ACTIVE_OWNER_COUNTS.isEmpty()) return false;

        long now = currentTick();
        CacheEntry cached = PLAYER_CACHE.get(playerUUID);
        if (cached != null && (now - cached.cachedAtTick()) < CACHE_LIFETIME_TICKS) {
            return cached.enabled();
        }

        MachineOwner owner = MachineOwner.getOwner(playerUUID);
        boolean enabled = owner != null &&
                ACTIVE_OWNER_COUNTS.keySet().stream().anyMatch(owner::isPlayerInTeam);
        PLAYER_CACHE.put(playerUUID, new CacheEntry(enabled, now));
        return enabled;
    }

    private static long currentTick() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getTickCount() : 0L;
    }

    public static void clearActiveOwners() {
        ACTIVE_OWNER_COUNTS.clear();
        PLAYER_CACHE.clear();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        enableCreativeEnergy(recipeLogic.isWorking());
        creativeEnergySubscription.updateSubscription();
    }

    public void enableCreativeEnergy(boolean enabled) {
        if (!(getLevel() instanceof ServerLevel)) return;
        if (enabled == currentlyActive) return;

        UUID ownerUUID = getOwnerUUID();
        if (ownerUUID == null) {
            ownerUUID = MachineOwner.EMPTY;
        }

        if (enabled) {
            ACTIVE_OWNER_COUNTS.merge(ownerUUID, 1, Integer::sum);
        } else {
            ACTIVE_OWNER_COUNTS.computeIfPresent(ownerUUID, (uuid, count) -> count > 1 ? count - 1 : null);
        }

        currentlyActive = enabled;
        PLAYER_CACHE.clear();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        enableCreativeEnergy(false);
    }

    private void tickEnableCreativeEnergy() {
        enableCreativeEnergy(recipeLogic.isWorking());
    }

    private Boolean isSubscriptionActive() {
        return isFormed();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        if (!isWorkingAllowed) {
            enableCreativeEnergy(false);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        enableCreativeEnergy(false);
        creativeEnergySubscription.unsubscribe();
    }
}
