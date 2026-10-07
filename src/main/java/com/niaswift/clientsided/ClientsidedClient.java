package com.niaswift.clientsided;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import com.niaswift.clientsided.plot.PlotIdUtil;
import com.niaswift.clientsided.plot.PlotIgnoreConfig;
import com.niaswift.clientsided.plot.TrendingPlayersScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.DeltaTracker;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientsidedClient implements ClientModInitializer {

    private static final String ALREADY_CONNECTED_MESSAGE = "You are already connected to this server!";

    private static KeyMapping toggleHUDKeybind;
    private static KeyMapping openScreenKeybind;
    private static KeyMapping showCursorKeybind;
    private static KeyMapping ignorePlotKeybind;
    private static boolean toggleHUD;
    private static boolean awaitingServerNode1Response;
    private static boolean passThroughSCommand;

    /** For plot-ignore debug: log when {@link Screen} instance changes. */
    private static Screen plotDebugLastScreen;

    @Override
    public void onInitializeClient() {
        PlotIgnoreConfig.load();

        ClientSendMessageEvents.MODIFY_COMMAND.register(command -> {
            String normalized = command.trim();
            if (!normalized.equals("s")) {
                return command;
            }
            if (passThroughSCommand) {
                passThroughSCommand = false;
                return command;
            }
            awaitingServerNode1Response = true;
            return "server node1";
        });

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) ->
            handleServerNode1Response(message.getString())
        );
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) ->
            handleServerNode1Response(message.getString())
        );

        toggleHUDKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.clientsided.toggleHUD", // The translation key of the keybinding's name
            InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_KP_1, // The keycode of the key
            KeyMapping.Category.CREATIVE // The translation key of the keybinding's category.
        ));

        openScreenKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.clientsided.openScreen", // The translation key of the keybinding's name
            InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_KP_2, // The keycode of the key
            KeyMapping.Category.CREATIVE // The translation key of the keybinding's category.
        ));

        showCursorKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.clientsided.showCursor", // The translation key of the keybinding's name
            InputConstants.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_KP_3, // The keycode of the key
            KeyMapping.Category.CREATIVE // The translation key of the keybinding's category.
        ));

        ignorePlotKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.clientsided.ignorePlot",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_4,
            KeyMapping.Category.MISC
        ));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof AbstractContainerScreen<?> trendingScreen)) {
                return;
            }
            if (!TrendingPlayersScreenHelper.isTrendingPlayersScreen(trendingScreen)) {
                return;
            }
//            Clientsided.LOGGER.info(
//                "[clientsided/plotIgnore] Trending GUI initialized; registering screen-only key listener"
//            );
            ScreenKeyboardEvents.afterKeyPress(screen).register((s, keyInput) -> {
                if (!ignorePlotKeybind.matches(keyInput)) {
                    return;
                }
//                Clientsided.LOGGER.info(
//                    "[clientsided/plotIgnore] Ignore Plot key in Trending GUI (key={}, scancode={})",
//                    keyInput.key(),
//                    keyInput.scancode()
//                );
                onIgnorePlotAddUnderCursor(client, trendingScreen);
            });
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Screen current = client.screen;
            if (current == plotDebugLastScreen) {
                return;
            }
            plotDebugLastScreen = current;
            if (current instanceof AbstractContainerScreen<?> handled) {
                String plainTitle = handled.getTitle().getString();
//                Clientsided.LOGGER.info(
//                    "[clientsided/plotIgnore] HandledScreen opened: plainTitle='{}' class={}",
//                    plainTitle,
//                    handled.getClass().getName()
//                );
                boolean trending = TrendingPlayersScreenHelper.isTrendingPlayersScreen(handled);
//                Clientsided.LOGGER.info(
//                    "[clientsided/plotIgnore] Trending GUI title match? {} (expected '{}')",
//                    trending,
//                    TrendingPlayersScreenHelper.TRENDING_PLAYERS_TITLE
//                );
            } else if (current != null) {
//                Clientsided.LOGGER.info(
//                    "[clientsided/plotIgnore] Screen opened (not HandledScreen): {}",
//                    current.getClass().getName()
//                );
            } else {
//                Clientsided.LOGGER.info("[clientsided/plotIgnore] Screen closed (in game / no GUI)");
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ( client.player == null ) return;
            if ( !toggleHUDKeybind.consumeClick()) return;

            toggleHUD =! toggleHUD;
            if (toggleHUD) {
                client.player.sendOverlayMessage(
                    Component.translatable("toggleHUD",
                        Component.translatable("toggleHUD.on").withStyle(ChatFormatting.GREEN))
                );
            } else {
                client.player.sendOverlayMessage(
                    Component.translatable("toggleHUD",
                        Component.translatable("toggleHUD.off").withStyle(ChatFormatting.RED))
                );
            }

        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ( client.player == null ) return;
            if ( !openScreenKeybind.consumeClick()) return;
            client.setScreen(new TestScreen());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if ( client.player == null ) return;
            if ( !showCursorKeybind.consumeClick()) return;
            client.mouseHandler.releaseMouse();
        });


        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, Identifier.parse(Clientsided.MOD_ID), this::hud);

    }

    private static void onIgnorePlotAddUnderCursor(Minecraft client, AbstractContainerScreen<?> handledScreen) {
        if (client.player == null) {
//            Clientsided.LOGGER.warn("[clientsided/plotIgnore] Abort: client.player is null");
            return;
        }

//        Clientsided.LOGGER.info("[clientsided/plotIgnore] Resolving slot under cursor");

        double mouseX = client.mouseHandler.xpos() * (double) client.getWindow().getGuiScaledWidth() / client.getWindow().getScreenWidth();
        double mouseY = client.mouseHandler.ypos() * (double) client.getWindow().getGuiScaledHeight() / client.getWindow().getScreenHeight();
//        Clientsided.LOGGER.info(
//            "[clientsided/plotIgnore] Raw mouse=({}, {}) scaled=({}, {}) window={}x{}",
//            client.mouse.getX(),
//            client.mouse.getY(),
//            mouseX,
//            mouseY,
//            client.getWindow().getScaledWidth(),
//            client.getWindow().getScaledHeight()
//        );

        Slot slot = TrendingPlayersScreenHelper.getSlotUnderMouse(handledScreen, mouseX, mouseY);
        if (slot == null) {
//            Clientsided.LOGGER.warn("[clientsided/plotIgnore] Abort: no slot under mouse (hover over an item first)");
            return;
        }

        ItemStack stack = slot.getItem();
//        Clientsided.LOGGER.info(
//            "[clientsided/plotIgnore] Hovered slot empty={} item={} slot={}",
//            stack.isEmpty(),
//            stack.getItem(),
//            slot
//        );

        String plotId = PlotIdUtil.extractPlotId(stack);
        if (plotId == null) {
//            Clientsided.LOGGER.warn("[clientsided/plotIgnore] Abort: no lore line starting with 'ID: ' (dump below)");
//            logLoreLinesForPlotDebug(stack);
            return;
        }

//        Clientsided.LOGGER.info("[clientsided/plotIgnore] Extracted plot id: '{}'", plotId);

        PlotIgnoreConfig.get().add(plotId);
//        if (PlotIgnoreConfig.get().add(plotId)) {
//            Clientsided.LOGGER.info("[clientsided/plotIgnore] Added '{}' to plot ignore list (saved to config)", plotId);
//            client.player.sendMessage(
//                Text.translatable("plotIgnore.added", plotId).formatted(Formatting.GRAY),
//                true
//            );
//        } else {
//            Clientsided.LOGGER.info("[clientsided/plotIgnore] Plot id '{}' was already in the ignore list", plotId);
//        }
    }

    private static void logLoreLinesForPlotDebug(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) {
            Clientsided.LOGGER.warn("[clientsided/plotIgnore]   (stack has no LORE component)");
            return;
        }
        int i = 0;
        for (Component line : lore.lines()) {
            Clientsided.LOGGER.info("[clientsided/plotIgnore]   lore[{}]='{}'", i, line.getString());
            i++;
        }
        if (i == 0) {
            Clientsided.LOGGER.warn("[clientsided/plotIgnore]   (lore component present but zero lines)");
        }
    }

    private static boolean handleServerNode1Response(String messageText) {
        if (!awaitingServerNode1Response) {
            return true;
        }

        if (!messageText.contains(ALREADY_CONNECTED_MESSAGE)) {
            awaitingServerNode1Response = false;
            return true;
        }

        awaitingServerNode1Response = false;
        passThroughSCommand = true;

        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.getConnection() != null) {
                client.getConnection().sendCommand("s");
            }
        });

        return false;
    }

    private void hud(GuiGraphicsExtractor context, DeltaTracker tickCounter) {

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if ( player == null ) return;

        int diamondCount = player.getInventory().countItem(Items.DIAMOND);

        if (   !toggleHUD
            || diamondCount == 0
            || client.gui.getChat().isChatFocused() ) return;


        Font textRenderer = client.font;
        int y = client.getWindow().getGuiScaledHeight();
        y -= textRenderer.lineHeight - 2;
        y -= 5;

        context.text(
                textRenderer,
                "Diamonds in your inventory: " + diamondCount,
                5,
                y,
                0xFFFFFFFF,
                true
        );

    }

}
