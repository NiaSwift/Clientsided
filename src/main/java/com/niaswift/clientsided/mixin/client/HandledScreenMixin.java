package com.niaswift.clientsided.mixin.client;

import com.niaswift.clientsided.plot.TrendingPlayersScreenHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void clientsided$hideIgnoredPlotSlot(
        DrawContext context,
        Slot slot,
        int x,
        int y,
        CallbackInfo callbackInfo
    ) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        if (TrendingPlayersScreenHelper.shouldHideSlot(screen, slot)) {
            callbackInfo.cancel();
        }
    }
}
