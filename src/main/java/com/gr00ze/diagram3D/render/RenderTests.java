package com.gr00ze.diagram3D.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

public class RenderTests {
    public static void drawQuad(
        PoseStack pose,
        MultiBufferSource.BufferSource buffer,
        Camera camera
    ) {
        Vec3 cameraPos = camera.getPosition();

        // posizione 3 blocchi davanti alla camera
        Vec3 pos = cameraPos.add(new Vec3(camera.getLookVector()).scale(3.0));

        pose.pushPose();

        // coordinate relative alla camera
        pose.translate(
            pos.x - cameraPos.x,
            pos.y - cameraPos.y,
            pos.z - cameraPos.z
        );
        pose.mulPose(camera.rotation());

        VertexConsumer vc = buffer.getBuffer(RenderType.textBackground());

        Matrix4f matrix = pose.last().pose();

        float size = 1.0f;

        vc.addVertex(matrix, -size, -size, 0)
            .setColor(0, 0, 10, 255)
            .setLight(255);

        vc.addVertex(matrix,  size, -size, 0)
            .setColor(0, 0, 10, 255)
            .setLight(255);

        vc.addVertex(matrix,  size,  size, 0)
            .setColor(0, 0, 10, 100)
            .setLight(255);

        vc.addVertex(matrix, -size,  size, 0)
            .setColor(0, 0, 10, 100)
            .setLight(255);

        pose.popPose();



    }

    public static void renderTest(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();

        List<Component> tooltip = List.of(
            Component.literal("Ciao")
        );

        int x = mc.getWindow().getGuiScaledWidth() / 2;
        int y = mc.getWindow().getGuiScaledHeight() / 2;

        RemovedGuiUtils.drawHoveringText(
            guiGraphics,
            tooltip,
            x,
            y,
            guiGraphics.guiWidth(),
            guiGraphics.guiHeight(),
            -1,
            0xF0100010,
            0x505000FF,
            0x28000000,
            mc.font
        );
    }
    public static void drawTestArrow(Vec3 look, Vec3 eye){
        //Section for testing purposes

        Vec3 origin = eye.add(look.scale(2.0));   // 2 blocks ahead

        origin = origin.subtract(0, 1.0, 0);      // lower

        Vector3dc start = new Vector3d(
            origin.x,
            origin.y,
            origin.z
        );

        Vector3dc force = new Vector3d(
            0,
            10,
            0
        );
        //Section for testing purposes end

    }
}
