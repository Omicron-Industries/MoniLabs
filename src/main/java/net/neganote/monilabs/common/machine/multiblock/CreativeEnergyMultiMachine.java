package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import net.minecraft.server.level.ServerLevel;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class CreativeEnergyMultiMachine extends UniqueWorkableElectricMultiblockMachine {

    public static final Set<NotifiableEnergyContainer> LOADED_ENERGY_CONTAINERS = Collections
            .newSetFromMap(new IdentityHashMap<>());

    public boolean isProviding = false;

    private final ConditionalSubscriptionHandler creativeSubscription;

    public CreativeEnergyMultiMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.creativeSubscription = new ConditionalSubscriptionHandler(this, this::tickSync, this::isFormed);
    }

    private void tickSync() {
        syncCreativeEnergyState(!isDuplicate() && isWorkingEnabled() && recipeLogic.isWorking());
    }

    public static void registerEnergyContainer(NotifiableEnergyContainer container) {
        LOADED_ENERGY_CONTAINERS.add(container);
    }

    public static void unregisterEnergyContainer(NotifiableEnergyContainer container) {
        LOADED_ENERGY_CONTAINERS.remove(container);
    }

    @Override
    public Class<?> getMachineType() {
        return CreativeEnergyMultiMachine.class;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        creativeSubscription.updateSubscription();
        tickSync();
    }

    public void syncCreativeEnergyState(boolean active) {
        if (!(getLevel() instanceof ServerLevel)) return;
        if (active == this.isProviding) return;
        this.isProviding = active;
        for (NotifiableEnergyContainer container : collectContainersInTeamOf(getOwnerUUID())) {
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
        creativeSubscription.unsubscribe();
        syncCreativeEnergyState(false);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        syncCreativeEnergyState(!isDuplicate() && isWorkingEnabled() && recipeLogic.isWorking());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        creativeSubscription.unsubscribe();
        syncCreativeEnergyState(false);
    }
}
