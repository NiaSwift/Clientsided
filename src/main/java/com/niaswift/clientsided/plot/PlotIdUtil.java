package com.niaswift.clientsided.plot;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public final class PlotIdUtil {

    private static final String ID_PREFIX = "ID: ";

    private PlotIdUtil() {
    }

    public static String extractPlotId(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) {
            return null;
        }

        for (Component line : lore.lines()) {
            String text = line.getString();
            if (text.startsWith(ID_PREFIX)) {
                return text.substring(ID_PREFIX.length()).trim();
            }
        }

        return null;
    }
}
