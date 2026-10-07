package com.niaswift.clientsided.mixin.client;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenInvoker {

    @Invoker("isHovering")
    boolean clientsided$isPointOverSlot(Slot slot, double pointX, double pointY);
}
