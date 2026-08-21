package com.gr00ze.diagram3D.gui;

import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.config.ClientConfig;
import com.gr00ze.diagram3D.config.ConfigUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import static com.gr00ze.diagram3D.config.ClientConfig.GUI_BACKGROUND_COLOR;

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

    private FixedPanel fixedPanel;

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

        fixedPanel = new FixedPanel(panelX, panelY, panelWidth, panelHeight, PANEL_PADDING);

        fixedPanel.addRow(
            new StringWidget(
                Component.translatable("diagram3d.configuration.display_center_of_mass"),
                font
            ),
            Button.builder(
                    getCenterOfMassModeLabel(),
                    button -> cycleCenterOfMass()
                )
                .bounds(0, 0, 85, 20)
                .build()
        );

        fixedPanel.addRow(
            new StringWidget(
                Component.translatable("diagram3d.configuration.display_vector_mode"),
                font
            ),
            Button.builder(
                    getVectorModeLabel(),
                    button -> cycleVectorMode()
                )
                .bounds(0, 0, 85, 20)
                .build()
        );

        fixedPanel.addTable( new ForceGroupTable(
            0,
            0,
            0,
            0,
            12
        ));

        this.addRenderableWidget(fixedPanel);

        Button moreSettings = Button.builder(
            Component.literal("⚙"),
            this::onPressMoreSettings
        ).bounds(
            this.width - 24,
            2,
            20,
            20
        ).build();

        moreSettings.setTooltip(
            Tooltip.create(
                Component.translatable(
                    "diagram3d.config_screen.more_settings"
                )
            )
        );

        this.addRenderableWidget(moreSettings);
    }
    private int currentY;

    private <W extends AbstractWidget> W addRenderableWidgetBelow(W widget, int height, int margin){
        widget.setY(currentY);
        this.addRenderableWidget(widget);
        currentY += height + margin;
        return widget;

    }



    private Component getCenterOfMassModeLabel() {
        return ClientConfig.DISPLAY_CENTER_OF_MASS.get().getTranslatedName();
    }
    private Component getVectorModeLabel() {
        return ClientConfig.VECTOR_MODE.get().getTranslatedName();
    }

    private void cycleVectorMode() {
        ClientConfig.VectorMode current =
            ConfigUtils.getVectorsMode();

        ClientConfig.VectorMode next =
            switch (current) {
                case DISABLED -> ClientConfig.VectorMode.SEPARATED;
                case SEPARATED -> ClientConfig.VectorMode.MERGED;
                case MERGED ->  ClientConfig.VectorMode.DISABLED;
            };

        ConfigUtils.setVectorsMode(next);

        this.clearWidgets();
        this.init();
    }

    private void cycleCenterOfMass() {
        ClientConfig.CenterOfMassMode current =
            ConfigUtils.getCenterOfMassMode();

        ClientConfig.CenterOfMassMode next =
            switch (current) {
                case DISABLED -> ClientConfig.CenterOfMassMode.ICON;
                case ICON -> ClientConfig.CenterOfMassMode.DETAILS;
                case DETAILS -> ClientConfig.CenterOfMassMode.ICON_DETAILS_ONLOOK;
                case ICON_DETAILS_ONLOOK ->  ClientConfig.CenterOfMassMode.DISABLED;
            };

        ConfigUtils.setCenterOfMassMode(next);

        this.clearWidgets();
        this.init();
    }

    private void onPressMoreSettings(Button button) {
        if (this.minecraft == null) {
            return;
        }

        ModContainer modContainer = ModList.get()
            .getModContainerById(Diagram3D.MOD_ID)
            .orElseThrow();

        this.minecraft.setScreen(
            new ConfigurationScreen(modContainer, this)
        );
    }

    @Override
    public void render(
        @NotNull GuiGraphics guiGraphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {

        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);



        for(Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
