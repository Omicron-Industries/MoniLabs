package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

// Copied from CosmicCore with some minor changes (thank you Caitlynn!)
public class UniqueWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            UniqueWorkableElectricMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    // Map of Players -> Unique Machines they have
    public static final HashMap<UUID, HashMap<Class<?>, MetaMachine>> ACTIVE_OWNERS = new HashMap<>();

    private TickableSubscription initOwnerSubs;

    @Persisted
    private long claimTick = 0L;

    public UniqueWorkableElectricMultiblockMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public Class<?> getMachineType() {
        throw new IllegalStateException("getMachineType() must be overridden in subclass!");
    }

    public boolean isDuplicate() {
        UUID playerUUID = this.getOwnerUUID();
        if (playerUUID == null) return false;
        if (ACTIVE_OWNERS.containsKey(playerUUID)) {
            return ACTIVE_OWNERS.get(playerUUID).containsKey(getMachineType()) &&
                    ACTIVE_OWNERS.get(playerUUID).get(getMachineType()) != this;
        } else {
            return ACTIVE_OWNERS.entrySet().stream()
                    .filter(entry -> {
                        MachineOwner owner = getOwner();
                        if (owner == null) return false;
                        return owner.isPlayerInTeam(playerUUID);
                    })
                    .anyMatch(entry -> entry.getValue().containsKey(getMachineType()) &&
                            entry.getValue().get(getMachineType()) != this);
        }
    }

    public static boolean machineIsActiveAndInTeam(MetaMachine machine, Predicate<MetaMachine> providing,
                                                   Class<?> machineType) {
        if (machine.getLevel() instanceof ServerLevel) {
            UUID playerUUID = machine.getOwnerUUID();
            if (playerUUID == null || playerUUID.equals(MachineOwner.EMPTY)) return false;
            MachineOwner owner = machine.getOwner();
            if (owner == null) return false;
            return UniqueWorkableElectricMultiblockMachine.ACTIVE_OWNERS.entrySet().stream()
                    .filter(entry -> owner.isPlayerInTeam(entry.getKey()))
                    .anyMatch(entry -> {
                        var uniqueMachines = entry.getValue();
                        if (uniqueMachines != null && uniqueMachines.get(machineType) != null) {
                            return providing.test(uniqueMachines.get(machineType));
                        }
                        return false;
                    });
        }
        return false;
    }

    private void claimSlot(boolean promote) {
        UUID owner = getOwnerUUID();
        if (owner == null || getLevel() == null) return;
        var playerMachines = ACTIVE_OWNERS.get(owner);
        var resident = playerMachines == null ? null : playerMachines.get(getMachineType());

        if (resident == null) {
            playerMachines = ACTIVE_OWNERS.computeIfAbsent(owner, k -> new HashMap<>());
            playerMachines.put(getMachineType(), this);
        } else if (resident != this &&
                resident instanceof UniqueWorkableElectricMultiblockMachine other &&
                this.claimTick > other.claimTick) {
                    playerMachines.put(getMachineType(), this);
                    other.setWorkingEnabled(false);
                }

        if (promote && playerMachines.get(getMachineType()) == this) {
            claimTick = getLevel().getGameTime();
        }
    }

    private void releaseSlot(boolean forfeit) {
        if (forfeit) claimTick = 0L;
        UUID owner = getOwnerUUID();
        if (owner == null) return;
        var playerMachines = ACTIVE_OWNERS.get(owner);
        if (playerMachines == null) return;
        if (playerMachines.get(getMachineType()) == this) {
            playerMachines.remove(getMachineType());
        }
        if (playerMachines.isEmpty()) {
            ACTIVE_OWNERS.remove(owner);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (isRemote()) return;
        initOwnerSubs = subscribeServerTick(this::tickWaitForOwner);
    }

    private void tickWaitForOwner() {
        UUID owner = getOwnerUUID();
        if (owner == null) return;
        unsubscribe(initOwnerSubs);
        initOwnerSubs = null;
        if (isFormed()) {
            claimSlot(false);
        }
        if (isDuplicate()) {
            setWorkingEnabled(false);
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (isRemote()) return;
        claimSlot(false);
        if (isDuplicate()) {
            setWorkingEnabled(false);
        }
    }

    @Override
    public void onUnload() {
        if (!isRemote()) {
            releaseSlot(false);
        }
        super.onUnload();
    }

    @Override
    public void onStructureInvalid() {
        if (!isRemote()) {
            releaseSlot(true);
        }
        super.onStructureInvalid();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isRemote() && !isDuplicate() && isWorkingAllowed && isFormed()) {
            claimSlot(true);
        }
        super.setWorkingEnabled(!isDuplicate() && isWorkingAllowed);
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        if (this.isDuplicate()) {
            textList.add(Component.translatable("monilabs.multiblock.duplicate.0")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
            textList.add(Component.translatable("monilabs.multiblock.duplicate.1")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
        } else super.addDisplayText(textList);
    }
}
