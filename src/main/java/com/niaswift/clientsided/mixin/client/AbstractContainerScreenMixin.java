package com.niaswift.clientsided.mixin.client;

import com.niaswift.clientsided.plot.TrendingPlayersScreenHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void clientsided$hideIgnoredPlotSlot(
        GuiGraphics context,
        Slot slot,
        int x,
        int y,
        CallbackInfo callbackInfo
    ) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (TrendingPlayersScreenHelper.shouldHideSlot(screen, slot)) {
            callbackInfo.cancel();
        }
    }
}
