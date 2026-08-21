package com.gr00ze.diagram3D.gui;

import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ForceGroupTable extends AbstractScrollWidget {

    private final List<ForceGroupRow> rows = new ArrayList<>();

    private final int rowHeight;

    public ForceGroupTable(
        int x,
        int y,
        int width,
        int height,
        int rowHeight
    ) {
        super(x, y, width, height, Component.empty());

        this.rowHeight = rowHeight;

        rebuild();
    }

    private void rebuild() {
        rows.clear();

        for (ForceGroup forceGroup : ForceGroups.REGISTRY) {
            rows.add(new ForceGroupRow(forceGroup, rowHeight));
        }
    }

    @Override
    protected double scrollRate() {
        return rowHeight;
    }

    @Override
    protected void renderContents(
        @NotNull GuiGraphics guiGraphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        int y = this.getY();
        renderHeader(guiGraphics, y);
        y += rowHeight;//To change if the header must be increased
        guiGraphics.hLine(this.getX() + 5  ,this.getX() + width - 5, y, 0x99CCCCCC);

        for (ForceGroupRow row : rows) {

            if (y + rowHeight < this.getY()) {
                y += rowHeight;
                continue; //Don't draw hidden upper row
            }

            if (y > this.getY() + this.height) {
                break;//Don't draw hidden bottom rows
            }

            row.render(
                guiGraphics,
                this.getX(),
                y,
                this.width,
                mouseX,
                mouseY
            );
            guiGraphics.hLine(this.getX() + 5  ,this.getX() + width - 5, y, 0x99999999);
            y += rowHeight;
        }
    }

    private void renderHeader(
        GuiGraphics guiGraphics,
        int y
    ) {
        Font font = Minecraft.getInstance().font;
        int fontOffset = (int)(font.lineHeight * 0.5);
        guiGraphics.drawString(
            font,
            Component.translatable("diagram3d.config_screen.force_table.header.force_group"),
            this.getX() + 8,
            y + fontOffset,
            0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
            font,
            Component.translatable("diagram3d.config_screen.force_table.header.vectors"),
            this.getX() + getVectorRightOffset(this.width),
            y + fontOffset,
            0xFFFFFFFF
        );

        guiGraphics.drawCenteredString(
            font,
            Component.translatable("diagram3d.config_screen.force_table.header.force_info"),
            this.getX() + getDetailsRightOffset(this.width),
            y + fontOffset,
            0xFFFFFFFF
        );


    }

    @Override
    protected void renderBackground(
        GuiGraphics guiGraphics
    ) {
        guiGraphics.renderOutline(
            getX(),
            getY(),
            width,
            height,
            0x80FFFFFF
        );
    }

    @Override
    protected int getInnerHeight() {
        return (rows.size() + 1) * rowHeight;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }

        double contentY = mouseY - this.getY() + this.scrollAmount();

        int rowIndex = (int) (contentY / rowHeight) - 1; // -1 for header

        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return false;
        }

        ForceGroupRow row = rows.get(rowIndex);

        if (row.mouseClicked(
            mouseX,
            mouseY,
            this.getX(),
            this.getWidth(),
            button
        )) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public static int getVectorRightOffset(int width){
        return width - 100;
    }
    public static int getDetailsRightOffset(int width){
        return width - 45;
    }
}