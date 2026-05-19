package com.niaswift.clientsided;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import com.niaswift.clientsided.plot.PlotIdUtil;
import com.niaswift.clientsided.plot.PlotIgnoreConfig;
import com.niaswift.clientsided.plot.TrendingPlayersScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientsidedClient implements ClientModInitializer {

    private static final String ALREADY_CONNECTED_MESSAGE = "You are already connected to this server!";

    private static KeyBinding toggleHUDKeybind;
    private static KeyBinding openScreenKeybind;
    private static KeyBinding showCursorKeybind;
    private static KeyBinding ignorePlotKeybind;
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

        ignorePlotKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.clientsided.ignorePlot",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_4,
            KeyBinding.Category.MISC
        ));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof HandledScreen<?> trendingScreen)) {
                return;
            }
            if (!TrendingPlayersScreenHelper.isTrendingPlayersScreen(trendingScreen)) {
                return;
            }
//            Clientsided.LOGGER.info(
//                "[clientsided/plotIgnore] Trending GUI initialized; registering screen-only key listener"
//            );
            ScreenKeyboardEvents.afterKeyPress(screen).register((s, keyInput) -> {
                if (!ignorePlotKeybind.matchesKey(keyInput)) {
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
            Screen current = client.currentScreen;
            if (current == plotDebugLastScreen) {
                return;
            }
            plotDebugLastScreen = current;
            if (current instanceof HandledScreen<?> handled) {
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

    private static void onIgnorePlotAddUnderCursor(MinecraftClient client, HandledScreen<?> handledScreen) {
        if (client.player == null) {
//            Clientsided.LOGGER.warn("[clientsided/plotIgnore] Abort: client.player is null");
            return;
        }

//        Clientsided.LOGGER.info("[clientsided/plotIgnore] Resolving slot under cursor");

        double mouseX = client.mouse.getX() * (double) client.getWindow().getScaledWidth() / client.getWindow().getWidth();
        double mouseY = client.mouse.getY() * (double) client.getWindow().getScaledHeight() / client.getWindow().getHeight();
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

        ItemStack stack = slot.getStack();
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
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) {
            Clientsided.LOGGER.warn("[clientsided/plotIgnore]   (stack has no LORE component)");
            return;
        }
        int i = 0;
        for (Text line : lore.lines()) {
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

        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.getNetworkHandler() != null) {
                client.getNetworkHandler().sendChatCommand("s");
            }
        });

        return false;
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
