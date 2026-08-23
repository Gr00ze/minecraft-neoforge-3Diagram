package com.gr00ze.diagram3D.gui;

import com.gr00ze.diagram3D.config.ClientConfig;
import com.gr00ze.diagram3D.config.ConfigUtils;
import com.gr00ze.diagram3D.config.ForceGroupDisplayConfig;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.ResourceLocation;

public class ForceGroupRow {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int ACTIVE_COLOR = 0xFF55FF55;
    private static final int INACTIVE_COLOR = 0xFF888888;
    private static final int ACTIVE_BACKGROUND_COLOR = 0x55AAAAAA;
    private static final int INACTIVE_BACKGROUND_COLOR = 0xCC101010;

    private final ForceGroup forceGroup;
    private final int rowHeight;
    private final Insets buttonBox;

    public ForceGroupRow(ForceGroup forceGroup, int rowHeight) {
        this.forceGroup = forceGroup;
        this.rowHeight = rowHeight;

        buttonBox = new Insets(5, 6, 8,8);
    }

    public void render(
        GuiGraphics guiGraphics,
        int x,
        int y,
        int width,
        int mouseX,
        int mouseY
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font  = minecraft.font;

        ResourceLocation id = getId();


        ForceGroupDisplayConfig config =
            ClientConfig.getForceGroupConfig(id);

        float fontYOffset = font.lineHeight * 0.5F;

        guiGraphics.drawString(
            font,
            forceGroup.name().getVisualOrderText(),
            x + 8F,
            y + fontYOffset,
            forceGroup.color(),
            true
        );

        int vectorsX = x + ForceGroupTable.getVectorRightOffset(width);
        int infoX = x + ForceGroupTable.getDetailsRightOffset(width);

        renderToggle(
            guiGraphics,
            vectorsX,
            y,
            config.vectors(),
            mouseX,
            mouseY
        );

        renderToggle(
            guiGraphics,
            infoX,
            y,
            config.info(),
            mouseX,
            mouseY
        );
    }

    private void renderToggle(
        GuiGraphics guiGraphics,
        int x,
        int y,
        boolean active,
        int mouseX,
        int mouseY
    ) {


        boolean hovered = mouseOver(mouseX, mouseY, x, y);

        int color;

        if (hovered) {
            color = active
                ? 0xFFFFFFFF
                : 0xFFAAAAAA;
        } else {
            color = active
                ? ACTIVE_COLOR
                : INACTIVE_COLOR;
        }


        guiGraphics.fill(
            (int) (x - this.buttonBox.left() ),
            (int) (y + this.rowHeight * 0.5 -  this.buttonBox.top()),
            (int) (x + this.buttonBox.right()),
            (int) (y + this.rowHeight * 0.5 + this.buttonBox.bottom() ),
            hovered ? ACTIVE_BACKGROUND_COLOR : INACTIVE_BACKGROUND_COLOR
        );

        String text = active ? "✓" : "✕";
        Font font = Minecraft.getInstance().font;


        float textX = x - font.width(text) * 0.5F;
        float textY = y + (rowHeight - font.lineHeight)  * 0.5F;
        guiGraphics.drawString(
            font,
            text,
            textX,
            textY,
            color,
            false
        );
    }

    public boolean mouseClicked(
        double mouseX,
        double mouseY,
        int x,
        int y,
        int tableWidth,
        int button) {
        if (button != 0) {
            return false;
        }


        int vectorsX = x + ForceGroupTable.getVectorRightOffset(tableWidth);
        int infoX = x + ForceGroupTable.getDetailsRightOffset(tableWidth);

        if (
            mouseX >= vectorsX - this.buttonBox.left()
            && mouseX <= vectorsX + this.buttonBox.right()

        ) {
            ConfigUtils.toggleVectors(getId());
            return true;
        }

        if (
            mouseX >= infoX - this.buttonBox.left()
            && mouseX <= infoX + this.buttonBox.right()

        ) {
            ConfigUtils.toggleInfo(getId());
            return true;
        }

        return false;
    }

    private boolean mouseOver(
        double mouseX,
        double mouseY,
        int centerX,
        int y
    ) {
        return mouseX >= centerX - this.buttonBox.left()
            && mouseX <= centerX + this.buttonBox.right()
            && mouseY >= y + this.rowHeight * 0.5 - this.buttonBox.top()
            && mouseY <= y + this.rowHeight * 0.5 + this.buttonBox.bottom();
    }




    private ResourceLocation getId() {
        return ForceGroups.REGISTRY.getKey(forceGroup);
    }
}
