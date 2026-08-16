package net.neganote.monilabs.common.machine.multiblock;

import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;

import net.minecraft.server.level.ServerLevel;

@SuppressWarnings("unused")
public class CreativeDataMultiMachine extends UniqueWorkableElectricMultiblockMachine {

    public boolean isProviding = false;

    private final ConditionalSubscriptionHandler creativeSubscription;

    public CreativeDataMultiMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.creativeSubscription = new ConditionalSubscriptionHandler(this, this::tickSync, this::isFormed);
    }

    private void tickSync() {
        syncCreativeData(!isDuplicate() && isWorkingEnabled() && recipeLogic.isWorking());
    }

    @Override
    public Class<?> getMachineType() {
        return CreativeDataMultiMachine.class;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        creativeSubscription.updateSubscription();
        tickSync();
    }

    public void syncCreativeData(boolean active) {
        if (!(getLevel() instanceof ServerLevel)) return;
        if (active == this.isProviding) return;
        this.isProviding = active;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        creativeSubscription.unsubscribe();
        syncCreativeData(false);
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        syncCreativeData(!isDuplicate() && isWorkingEnabled() && recipeLogic.isWorking());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        creativeSubscription.unsubscribe();
        syncCreativeData(false);
    }
}
