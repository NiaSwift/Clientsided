package com.niaswift.clientsided;


import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class TestScreen extends Screen {

    private final LocalPlayer player;

    protected TestScreen() {
        // The parameter is the title of the screen,
        // which will be narrated when you enter the screen.
        super(Component.literal("My tutorial screen"));
        player = minecraft.player;
    }


    public Button button;
    public Button draggableButton;
    static private final int buttonWidth = 200;
    static private final int buttonHeight = 20;
    static private Integer draggableButtonX;
    static private int draggableButtonY = 20;
    static private boolean dragging;

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (draggableButton.isMouseOver(click.x(), click.y())) dragging = true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        dragging = false;
        this.setFocused(null);
        return super.mouseReleased(click);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        if (dragging) {
            draggableButtonX = mouseX - (buttonWidth / 2);
            draggableButtonY = mouseY - (buttonHeight / 2);
            draggableButton.setX(draggableButtonX);
            draggableButton.setY(draggableButtonY);
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    protected void init() {

        button = Button.builder(Component.literal("Button"), button -> {
                if (player != null) player.displayClientMessage(Component.nullToEmpty("You clicked button!"), false);
            })
            .bounds(width / 2 - 205, 20, buttonWidth, buttonHeight)
            .tooltip(Tooltip.create(Component.literal("Tooltip of button")))
            .build();

        if (draggableButtonX == null) draggableButtonX = width / 2 + 5;
        draggableButton = Button.builder(Component.literal("Draggable Button"), button -> {
                if (player != null) player.displayClientMessage(Component.nullToEmpty("You clicked the draggable button!"), false);
            })
            .bounds(draggableButtonX, draggableButtonY, buttonWidth, buttonHeight)
            .tooltip(Tooltip.create(Component.literal("Tooltip of draggable button")))
            .build();

        addRenderableWidget(button);
        addRenderableWidget(draggableButton);
    }
}