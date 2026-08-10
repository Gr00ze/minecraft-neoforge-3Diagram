package com.gr00ze.diagram3D.gui;

import com.gr00ze.diagram3D.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DisplayConfigScreen extends Screen {

    private static final int PANEL_WIDTH_PERCENT = 50;
    private static final int PANEL_HEIGHT_PERCENT = 70;

    private static final int MIN_PANEL_WIDTH = 300;
    private static final int MIN_PANEL_HEIGHT = 220;

    private static final int PANEL_PADDING = 12;
    private static final int CENTER_OF_MASS_HEIGHT = 24;
    private static final int TABLE_TOP_MARGIN = 8;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    private Button centerOfMassButton;
    private ForceGroupTable forceGroupTable;

    public DisplayConfigScreen() {
        super(Component.literal("Visualization"));
    }

    @Override
    protected void init() {
        panelWidth = Math.max(
            MIN_PANEL_WIDTH,
            this.width * PANEL_WIDTH_PERCENT / 100
        );

        panelHeight = Math.max(
            MIN_PANEL_HEIGHT,
            this.height * PANEL_HEIGHT_PERCENT / 100
        );

        panelWidth = Math.min(panelWidth, this.width - 20);
        panelHeight = Math.min(panelHeight, this.height - 20);

        panelX = (this.width - panelWidth) / 2;
        panelY = (this.height - panelHeight) / 2;

        int contentX = panelX + PANEL_PADDING;
        int contentWidth = panelWidth - PANEL_PADDING * 2;

        int centerOfMassY = panelY + PANEL_PADDING;

        this.centerOfMassButton = Button.builder(
            getCenterOfMassLabel(),
            button -> cycleCenterOfMass()
        ).bounds(
            contentX + 90,
            centerOfMassY,
            85,
            20
        ).build();

        this.addRenderableWidget(centerOfMassButton);

        int tableY =
            centerOfMassY
                + CENTER_OF_MASS_HEIGHT
                + TABLE_TOP_MARGIN;

        int tableHeight =
            panelY
                + panelHeight
                - PANEL_PADDING
                - tableY;

        this.forceGroupTable = new ForceGroupTable(
            contentX,
            tableY,
            contentWidth,
            tableHeight,
            12
        );

        this.addRenderableWidget(forceGroupTable);
    }

    private Component getCenterOfMassLabel() {
        return switch (ClientConfig.getCenterOfMassMode()) {
            case DISABLED ->
                Component.literal("Disabled");

            case ICON ->
                Component.literal("Icon");

            case DETAILS ->
                Component.literal("Details");
        };
    }

    private void cycleCenterOfMass() {
        ClientConfig.CenterOfMassMode current =
            ClientConfig.getCenterOfMassMode();

        ClientConfig.CenterOfMassMode next =
            switch (current) {
                case DISABLED -> ClientConfig.CenterOfMassMode.ICON;
                case ICON -> ClientConfig.CenterOfMassMode.DETAILS;
                case DETAILS -> ClientConfig.CenterOfMassMode.DISABLED;
            };

        ClientConfig.setCenterOfMassMode(next);

        this.clearWidgets();
        this.init();
    }

    @Override
    public void render(
        @NotNull GuiGraphics guiGraphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.fill(
            panelX,
            panelY,
            panelX + panelWidth,
            panelY + panelHeight,
            0xA0101010
        );

        for(Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }




        guiGraphics.drawString(
            this.font,
            "Center of Mass",
            panelX + PANEL_PADDING,
            panelY + PANEL_PADDING + 6,
            0xFFFFFFFF
        );









    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
