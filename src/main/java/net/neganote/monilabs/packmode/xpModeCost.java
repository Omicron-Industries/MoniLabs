package net.neganote.monilabs.packmode;

import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.util.thread.EffectiveSide;

public final class xpModeCost {

    private static volatile boolean serverFree;
    private static volatile boolean clientFree;

    private xpModeCost() {}

    public static boolean isFree() {
        return isFree(EffectiveSide.get());
    }

    public static boolean isFree(LogicalSide side) {
        return side == LogicalSide.SERVER ? serverFree : clientFree;
    }

    static void setModeServer(PackMode mode) {
        serverFree = mode.hasFreeXpCosts();
    }

    static void setFreeClient(boolean free) {
        clientFree = free;
    }
}
