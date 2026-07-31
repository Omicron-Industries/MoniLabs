package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class CreativeEnergyMultiMachine extends UniqueWorkableElectricMultiblockMachine {

    private static final Set<UUID> ACTIVE_OWNERS = new HashSet<>();

    private static final Map<UUID, CacheEntry> PLAYER_CACHE = new HashMap<>();

    private static final Set<NotifiableEnergyContainer> LOADED_ENERGY_CONTAINERS = Collections
            .newSetFromMap(new IdentityHashMap<>());

    private static final int CACHE_LIFETIME_TICKS = 20;

    private record CacheEntry(boolean enabled, long cachedAtTick) {}

    private final ConditionalSubscriptionHandler creativeEnergySubscription;

    private boolean currentlyActive = false;

    public CreativeEnergyMultiMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);

        this.creativeEnergySubscription = new ConditionalSubscriptionHandler(this, this::tickEnableCreativeEnergy,
                this::isSubscriptionActive);
    }

    public static void registerEnergyContainer(NotifiableEnergyContainer container) {
        LOADED_ENERGY_CONTAINERS.add(container);
    }

    public static void unregisterEnergyContainer(NotifiableEnergyContainer container) {
        LOADED_ENERGY_CONTAINERS.remove(container);
    }

    public static boolean isCreativeEnergyEnabledFor(UUID playerUUID) {
        if (ACTIVE_OWNERS.isEmpty()) {
            return false;
        }

        long now = currentTick();
        CacheEntry cached = PLAYER_CACHE.get(playerUUID);
        if (cached != null && (now - cached.cachedAtTick()) < CACHE_LIFETIME_TICKS) {
            return cached.enabled();
        }

        MachineOwner owner = MachineOwner.getOwner(playerUUID);
        boolean enabled = owner != null && ACTIVE_OWNERS.stream().anyMatch(owner::isPlayerInTeam);
        PLAYER_CACHE.put(playerUUID, new CacheEntry(enabled, now));
        return enabled;
    }

    private static long currentTick() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.getTickCount() : 0L;
    }

    public static void clearActiveOwners() {
        ACTIVE_OWNERS.clear();
        PLAYER_CACHE.clear();
        LOADED_ENERGY_CONTAINERS.clear();
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
            ACTIVE_OWNERS.add(ownerUUID);
        } else {
            ACTIVE_OWNERS.remove(ownerUUID);
        }
        currentlyActive = enabled;
        PLAYER_CACHE.clear();

        for (NotifiableEnergyContainer container : collectContainersInTeamOf(ownerUUID)) {
            container.checkOutputSubscription();
            container.notifyListeners();
        }
    }

    private static List<NotifiableEnergyContainer> collectContainersInTeamOf(UUID teamMemberUUID) {
        return LOADED_ENERGY_CONTAINERS.stream()
                .filter(container -> {
                    UUID containerOwnerUUID = container.getMachine().getOwnerUUID();
                    if (containerOwnerUUID == null) return false;
                    MachineOwner containerOwner = MachineOwner.getOwner(containerOwnerUUID);
                    return containerOwner != null && containerOwner.isPlayerInTeam(teamMemberUUID);
                })
                .toList();
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
