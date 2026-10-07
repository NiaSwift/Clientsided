package com.niaswift.clientsided.plot;

import com.niaswift.clientsided.mixin.client.AbstractContainerScreenInvoker;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public final class TrendingPlayersScreenHelper {

    public static final String TRENDING_PLAYERS_TITLE = "Now Playing";

    private TrendingPlayersScreenHelper() {
    }

    public static boolean isTrendingPlayersScreen(AbstractContainerScreen<?> screen) {
        return TRENDING_PLAYERS_TITLE.equals(screen.getTitle().getString());
    }

    public static Slot getSlotUnderMouse(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        AbstractContainerScreenInvoker invoker = (AbstractContainerScreenInvoker) screen;
        AbstractContainerMenu handler = screen.getMenu();

        for (Slot slot : handler.slots) {
            if (invoker.clientsided$isPointOverSlot(slot, mouseX, mouseY)) {
                return slot;
            }
        }

        return null;
    }

    public static boolean shouldHideSlot(AbstractContainerScreen<?> screen, Slot slot) {
        if (!isTrendingPlayersScreen(screen)) {
            return false;
        }

        String plotId = PlotIdUtil.extractPlotId(slot.getItem());
        return plotId != null && PlotIgnoreConfig.get().contains(plotId);
    }
}
