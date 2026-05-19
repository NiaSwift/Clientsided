package com.niaswift.clientsided.plot;

import com.niaswift.clientsided.mixin.client.HandledScreenInvoker;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class TrendingPlayersScreenHelper {

    public static final String TRENDING_PLAYERS_TITLE = "Trending - Players";

    private TrendingPlayersScreenHelper() {
    }

    public static boolean isTrendingPlayersScreen(HandledScreen<?> screen) {
        return TRENDING_PLAYERS_TITLE.equals(screen.getTitle().getString());
    }

    public static Slot getSlotUnderMouse(HandledScreen<?> screen, double mouseX, double mouseY) {
        HandledScreenInvoker invoker = (HandledScreenInvoker) screen;
        ScreenHandler handler = screen.getScreenHandler();

        for (Slot slot : handler.slots) {
            if (invoker.clientsided$isPointOverSlot(slot, mouseX, mouseY)) {
                return slot;
            }
        }

        return null;
    }

    public static boolean shouldHideSlot(HandledScreen<?> screen, Slot slot) {
        if (!isTrendingPlayersScreen(screen)) {
            return false;
        }

        String plotId = PlotIdUtil.extractPlotId(slot.getStack());
        return plotId != null && PlotIgnoreConfig.get().contains(plotId);
    }
}
