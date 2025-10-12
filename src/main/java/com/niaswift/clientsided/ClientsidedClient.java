package com.niaswift.clientsided;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientsidedClient implements ClientModInitializer {

    private static KeyBinding toggleHUDKeybind;
    private static KeyBinding openScreenKeybind;
    private static KeyBinding showCursorKeybind;
    private static boolean toggleHUD;

    @Override
    public void onInitializeClient() {

        toggleHUDKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.clientsided.toggleHUD", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_KP_1, // The keycode of the key
            KeyBinding.Category.CREATIVE // The translation key of the keybinding's category.
        ));

        openScreenKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.clientsided.openScreen", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_KP_2, // The keycode of the key
            KeyBinding.Category.CREATIVE // The translation key of the keybinding's category.
        ));

        showCursorKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.clientsided.showCursor", // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_KP_3, // The keycode of the key
            KeyBinding.Category.CREATIVE // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ( client.player == null ) return;
            if ( !toggleHUDKeybind.wasPressed()) return;

            toggleHUD =! toggleHUD;
            if (toggleHUD) {
                client.player.sendMessage(
                    Text.translatable("toggleHUD",
                        Text.translatable("toggleHUD.on").formatted(Formatting.GREEN)),
                    true
                );
            } else {
                client.player.sendMessage(
                    Text.translatable("toggleHUD",
                        Text.translatable("toggleHUD.off").formatted(Formatting.RED)),
                    true
                );
            }

        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ( client.player == null ) return;
            if ( !openScreenKeybind.wasPressed()) return;
            client.setScreen(new TestScreen());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ( client.player == null ) return;
            if ( !showCursorKeybind.wasPressed()) return;
            client.mouse.unlockCursor();
        });


        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, Identifier.of(Clientsided.MOD_ID), this::hud);

    }

    private void hud(DrawContext context, RenderTickCounter tickCounter) {

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if ( player == null ) return;

        int diamondCount = player.getInventory().count(Items.DIAMOND);

        if (   !toggleHUD
            || diamondCount == 0
            || client.inGameHud.getChatHud().isChatFocused() ) return;


        TextRenderer textRenderer = client.textRenderer;
        int y = client.getWindow().getScaledHeight();
        y -= textRenderer.fontHeight - 2;
        y -= 5;

        context.drawText(
                textRenderer,
                "Diamonds in your inventory: " + diamondCount,
                5,
                y,
                0xFFFFFFFF,
                true
        );

    }

}
