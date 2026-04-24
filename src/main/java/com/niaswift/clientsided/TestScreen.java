package com.niaswift.clientsided;


import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
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
        player = client.player;
    }


    public ButtonWidget button;
    public ButtonWidget draggableButton;
    static private final int buttonWidth = 200;
    static private final int buttonHeight = 20;
    static private Integer draggableButtonX;
    static private int draggableButtonY = 20;
    static private boolean dragging;

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (draggableButton.isMouseOver(click.x(), click.y())) dragging = true;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragging = false;
        this.setFocused(null);
        return super.mouseReleased(click);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
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

        button = ButtonWidget.builder(Text.literal("Button"), button -> {
                if (player != null) player.sendMessage(Text.of("You clicked button!"), false);
            })
            .dimensions(width / 2 - 205, 20, buttonWidth, buttonHeight)
            .tooltip(Tooltip.of(Text.literal("Tooltip of button")))
            .build();

        if (draggableButtonX == null) draggableButtonX = width / 2 + 5;
        draggableButton = ButtonWidget.builder(Text.literal("Draggable Button"), button -> {
                if (player != null) player.sendMessage(Text.of("You clicked the draggable button!"), false);
            })
            .dimensions(draggableButtonX, draggableButtonY, buttonWidth, buttonHeight)
            .tooltip(Tooltip.of(Text.literal("Tooltip of draggable button")))
            .build();

        addDrawableChild(button);
        addDrawableChild(draggableButton);
    }
}