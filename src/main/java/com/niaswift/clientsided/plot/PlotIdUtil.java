package com.niaswift.clientsided.plot;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class PlotIdUtil {

    private static final String ID_PREFIX = "ID: ";

    private PlotIdUtil() {
    }

    public static String extractPlotId(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) {
            return null;
        }

        for (Text line : lore.lines()) {
            String text = line.getString();
            if (text.startsWith(ID_PREFIX)) {
                return text.substring(ID_PREFIX.length()).trim();
            }
        }

        return null;
    }
}
