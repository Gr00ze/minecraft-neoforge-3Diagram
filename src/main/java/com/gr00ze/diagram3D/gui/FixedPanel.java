package com.gr00ze.diagram3D.gui;

import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.config.ConfigUtils;
import com.gr00ze.diagram3D.render.RenderTypes;
import com.gr00ze.libs.RenderFunctions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.gr00ze.diagram3D.config.ClientConfig.GUI_BACKGROUND_COLOR;
import static com.gr00ze.diagram3D.config.ClientConfig.GUI_BACKGROUND_USE_TEXTURE;

public class FixedPanel extends AbstractContainerEventHandler
    implements Renderable, NarratableEntry {

    public static final ResourceLocation BACKGROUND_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(
            Diagram3D.MOD_ID,
            "textures/gui/background.png"
        );

    private final List<GuiEventListener> children = new ArrayList<>();

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private int rowCursorY;

    private int panelPadding;


    public FixedPanel(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.rowCursorY = y;
    }

    public FixedPanel(int x, int y, int width, int height, int panelPadding) {
        this(x, y,  width, height);
        this.panelPadding = panelPadding;
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return children;
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry>
    T add(T widget) {
        children.add(widget);
        return widget;
    }

    @Override
    public void render(
        @NotNull GuiGraphics guiGraphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {




        if(GUI_BACKGROUND_USE_TEXTURE.get()){
            VertexConsumer buffer = guiGraphics.bufferSource().getBuffer(
                RenderTypes.texturedBackground(BACKGROUND_TEXTURE)
            );
            RenderFunctions.drawTexturedRect(
                buffer,
                guiGraphics.pose().last().pose(),
                x,
                y,
                width,
                height,
                true
            );
        }else{
            guiGraphics.fill(
                this.x,
                this.y,
                this.x + this.width,
                this.y + this.height,
                ConfigUtils.getColor(GUI_BACKGROUND_COLOR)
            );
        }


        for (GuiEventListener child : children) {
            if (child instanceof Renderable renderable) {
                renderable.render(guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    @Override
    public @NotNull NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(
        @NotNull NarrationElementOutput narrationElementOutput
    ) {
    }

    public void addRow(
        AbstractWidget left,
        AbstractWidget right
    ) {
        int rowY = rowCursorY + panelPadding;

        int rowHeight = Math.max(
            left.getHeight(),
            right.getHeight()
        );

        left.setPosition(
            x + panelPadding,
            rowY + (rowHeight - left.getHeight()) / 2
        );

        right.setPosition(
            x + width / 2,
            rowY + (rowHeight - right.getHeight()) / 2
        );

        children.add(left);
        children.add(right);

        rowCursorY += rowHeight + panelPadding;
    }

    public ForceGroupTable addTable(ForceGroupTable forceGroupTable) {
        forceGroupTable.setPosition(this.x + this.panelPadding, this.rowCursorY + panelPadding);
        forceGroupTable.setWidth(width - this.panelPadding * 2);

        int tableHeight =
            (y + height) - rowCursorY - 2 * panelPadding;
        forceGroupTable.setHeight(tableHeight);

        this.rowCursorY = tableHeight;

        return this.add(forceGroupTable);
    }
}
