package com.niaswift.clientsided.mixin.client;

import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

@Mixin(HandledScreen.class)
public interface HandledScreenInvoker {

    @Invoker("isPointOverSlot")
    boolean clientsided$isPointOverSlot(Slot slot, double pointX, double pointY);
}
