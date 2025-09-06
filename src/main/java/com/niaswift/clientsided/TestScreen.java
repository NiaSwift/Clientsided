package com.niaswift.clientsided;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

public class TestScreen extends Screen {

    private final ClientPlayerEntity player;

    protected TestScreen() {
        // The parameter is the title of the screen,
        // which will be narrated when you enter the screen.
        super(Text.literal("My tutorial screen"));
        client = MinecraftClient.getInstance();
        player = client.player;
    }


    public ButtonWidget button1;
    public ButtonWidget button2;

    @Override
    protected void init() {
        button1 = ButtonWidget.builder(Text.literal("Button 1"), button -> {
                if (player != null) player.sendMessage(Text.of("You clicked button1!"), false);
            })
            .dimensions(width / 2 - 205, 20, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Tooltip of button1")))
            .build();
        button2 = ButtonWidget.builder(Text.literal("Button 2"), button -> {
                if (player != null) player.sendMessage(Text.of("You clicked button2!"), false);
            })
            .dimensions(width / 2 + 5, 20, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Tooltip of button2")))
            .build();

        addDrawableChild(button1);
        addDrawableChild(button2);
    }
}