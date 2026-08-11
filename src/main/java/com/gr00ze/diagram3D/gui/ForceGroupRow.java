package com.gr00ze.diagram3D.gui;

import com.gr00ze.diagram3D.ClientConfig;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ForceGroupRow {

    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int ACTIVE_COLOR = 0xFF55FF55;
    private static final int INACTIVE_COLOR = 0xFF888888;

    private final ForceGroup forceGroup;

    public ForceGroupRow(ForceGroup forceGroup) {
        this.forceGroup = forceGroup;
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


        ClientConfig.ForceGroupDisplayConfig config =
            ClientConfig.getForceGroupConfig(id);

        guiGraphics.drawString(
            minecraft.font,
            forceGroup.name(),
            x + 8,
            y + 7,
            forceGroup.color()
        );

        int vectorsX = x + width - 100;
        int infoX = x + width - 45;

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

        guiGraphics.drawCenteredString(
            Minecraft.getInstance().font,
            text,
            centerX,
            y + 7,
            color
        );
    }

    public boolean mouseClicked(
        double mouseX,
        int x,
        int width,
        int button
    ) {
        if (button != 0) {
            return false;
        }

        int vectorsX = x + width - 100;
        int infoX = x + width - 45;

        if (mouseX >= vectorsX - 12 && mouseX <= vectorsX + 12) {
            ClientConfig.toggleVectors(getId());
            return true;
        }

        if (mouseX >= infoX - 12 && mouseX <= infoX + 12) {
            ClientConfig.toggleInfo(getId());
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
        return mouseX >= centerX - 12
            && mouseX <= centerX + 12
            && mouseY >= y
            && mouseY <= y + 12;
    }

    private ResourceLocation getId() {
        return ForceGroups.REGISTRY.getKey(forceGroup);
    }
}
