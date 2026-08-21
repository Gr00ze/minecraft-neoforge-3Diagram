package com.gr00ze.diagram3D.gui;

import com.gr00ze.diagram3D.config.ClientConfig;
import com.gr00ze.diagram3D.config.ConfigUtils;
import com.gr00ze.diagram3D.config.ForceGroupDisplayConfig;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ForceGroupRow {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int ACTIVE_COLOR = 0xFF55FF55;
    private static final int INACTIVE_COLOR = 0xFF888888;

    private final ForceGroup forceGroup;
    private final int rowHeight;
    private final int rowPadding;

    public ForceGroupRow(ForceGroup forceGroup, int rowHeight) {
        this.forceGroup = forceGroup;
        this.rowHeight = rowHeight;
        this.rowPadding = 1;
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

        ResourceLocation id = getId();


        ForceGroupDisplayConfig config =
            ClientConfig.getForceGroupConfig(id);

        int fontYOffset = (int)(minecraft.font.lineHeight * 0.5);
        guiGraphics.drawString(
            minecraft.font,
            forceGroup.name(),
            x + 8,
            y + fontYOffset,
            forceGroup.color()
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
        int centerX,
        int y,
        boolean active,
        int mouseX,
        int mouseY
    ) {
        String text = active ? "✓" : "✕";

        boolean hovered = mouseOver(mouseX, mouseY, centerX, y);

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
            centerX - getXPadding(),
            y,
            centerX + getXPadding(),
            y + rowHeight,
            hovered ? 0x55AAAAAA : 0x55555555
        );

        Font font = Minecraft.getInstance().font;
        int fontYOffset = (int)(font.lineHeight * 0.5);
        guiGraphics.drawCenteredString(
            Minecraft.getInstance().font,
            text,
            centerX,
            y + fontYOffset,
            color
        );
    }

    public boolean mouseClicked(
        double mouseX,
        double mouseY,
        int x,
        int width,
        int button
    ) {
        if (button != 0) {
            return false;
        }

        int vectorsX = x + ForceGroupTable.getVectorRightOffset(width);
        int infoX = x + ForceGroupTable.getDetailsRightOffset(width);

        if (
            mouseX >= vectorsX - getXPadding()
            && mouseX <= vectorsX + getXPadding()

        ) {
            ConfigUtils.toggleVectors(getId());
            return true;
        }

        if (
            mouseX >= infoX - getXPadding()
            && mouseX <= infoX + getXPadding()

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
        return mouseX >= centerX - getXPadding()
            && mouseX <= centerX + getXPadding()
            && mouseY >= y + this.rowPadding
            && mouseY <= y + rowHeight - this.rowPadding;
    }


    private int getXPadding() {
        return 8;
    }

    private ResourceLocation getId() {
        return ForceGroups.REGISTRY.getKey(forceGroup);
    }
}
