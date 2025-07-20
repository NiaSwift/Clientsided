package com.niaswift.clientsided;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientsidedClient implements ClientModInitializer {

    private static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {

        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.clientsided.test", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_V, // The keycode of the key
                "key.categories.creative" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ( client.player != null ) {
                while (keyBinding.wasPressed()) {
                    client.player.sendMessage(Text.literal("Key 1 was pressed!"), false);
                }
            }
        });


        HudElementRegistry.addFirst(
                Identifier.of(Clientsided.MOD_ID),
                (context, tickCounter) -> {
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if (
                            player != null
                                    && player.getHealth() > 10
                    ) {
                        int diamondCount = player.getInventory().count(Items.DIAMOND);
                        context.drawText(
                                MinecraftClient.getInstance().textRenderer,
                                "Diamonds in your inventory: " + diamondCount,
                                5,
                                527,
                                0xFFFFFFFF,
                                true
                        );
                    }
                });

    }

}
