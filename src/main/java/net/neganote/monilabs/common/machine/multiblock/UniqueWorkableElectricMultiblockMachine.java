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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

// Copied from CosmicCore with some minor changes (thank you Caitlynn!)
public class UniqueWorkableElectricMultiblockMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            UniqueWorkableElectricMultiblockMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    // Map of machine owner UUIDs to their unique machines.
    public static final HashMap<UUID, HashMap<Class<?>, MetaMachine>> ACTIVE_OWNERS = new HashMap<>();

    private TickableSubscription ownerSubscription;
    private UUID observedOwnerKey;
    private UUID claimedOwnerKey;

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

    public static @Nullable UUID getOwnerKey(MetaMachine machine) {
        MachineOwner owner = machine.getOwner();
        return owner == null ? null : getOwnerKey(owner);
    }

    private static UUID getOwnerKey(MachineOwner owner) {
        UUID key = owner.getUUID();
        // EMPTY is shared by all players without a resolved team or guild.
        return MachineOwner.EMPTY.equals(key) ? owner.getPlayerUUID() : key;
    }

    public static <T extends MetaMachine> boolean teamHasMachine(MetaMachine machine, Class<T> type,
                                                                 Predicate<T> condition) {
        MachineOwner owner = machine.getOwner();
        if (owner == null) return false;
        if (ownerHasMachine(owner, type, condition)) return true;
        for (UUID player : owner.getMembers()) {
            MachineOwner member = MachineOwner.getOwner(player);
            if (member != null && ownerHasMachine(member, type, condition)) return true;
        }
        return false;
    }

    private static <T extends MetaMachine> boolean ownerHasMachine(MachineOwner owner, Class<T> type,
                                                                   Predicate<T> condition) {
        var machines = ACTIVE_OWNERS.get(getOwnerKey(owner));
        if (machines == null) return false;
        MetaMachine machine = machines.get(type);
        return type.isInstance(machine) && condition.test(type.cast(machine));
    }

    public boolean isDuplicate() {
        UUID owner = getOwnerKey(this);
        return owner != null &&
                ACTIVE_OWNERS.containsKey(owner) &&
                ACTIVE_OWNERS.get(owner).containsKey(getMachineType()) &&
                ACTIVE_OWNERS.get(owner).get(getMachineType()) != this;
    }

    private void claimSlot(boolean promote) {
        UUID owner = getOwnerKey(this);
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
        if (playerMachines.get(getMachineType()) == this) {
            claimedOwnerKey = owner;
        }
    }

    private void releaseSlot(boolean forfeit) {
        if (forfeit) claimTick = 0L;
        UUID owner = claimedOwnerKey;
        claimedOwnerKey = null;
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
        ownerSubscription = subscribeServerTick(this::tickOwner);
    }

    private void tickOwner() {
        UUID owner = getOwnerKey(this);
        if (Objects.equals(owner, observedOwnerKey)) return;
        UUID previousOwnerKey = observedOwnerKey;
        observedOwnerKey = owner;
        releaseSlot(false);
        if (owner == null) {
            onOwnerKeyChanged(previousOwnerKey);
            return;
        }
        if (isFormed()) {
            claimSlot(false);
        }
        if (isDuplicate()) {
            setWorkingEnabled(false);
        }
        onOwnerKeyChanged(previousOwnerKey);
    }

    protected void onOwnerKeyChanged(@Nullable UUID previousOwnerKey) {}

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
            unsubscribe(ownerSubscription);
            ownerSubscription = null;
            observedOwnerKey = null;
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
