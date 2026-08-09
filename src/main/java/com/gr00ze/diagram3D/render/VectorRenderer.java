package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.data.DiagramRecords.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.gui.RemovedGuiUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

import static com.gr00ze.diagram3D.ClientConfig.FORCE_VISUAL_SCALE;
import static com.gr00ze.diagram3D.data.DiagramDataManager.diagramDataCache;

@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class VectorRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event){
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER)
            return;

        Minecraft mc = Minecraft.getInstance();

        Player player = mc.player;
        if(player == null)
            return;

        PoseStack pose = event.getPoseStack();
        Camera camera = mc.gameRenderer.getMainCamera();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        //drawQuad(pose, buffer, camera);
        //buffer.endBatch(RenderType.debugQuads());




        for (DiagramDataCache cached : diagramDataCache.values())
        {
            for (ResolvedForceGroup forcesGroup : cached.groups())
            {
                int forceGroupColorARGB = 0xFF000000 | forcesGroup.color();
                for (ResolvedForce pointForce : forcesGroup.forces())
                {

                    drawVector(
                        pose,
                        buffer,
                        pointForce,
                        camera,
                        forceGroupColorARGB
                    );

                    drawInfo(
                        pose,
                        buffer,
                        mc.font,
                        pointForce,
                        camera,
                        forceGroupColorARGB,
                        forcesGroup.name()
                    );


                }
            }
            drawCenterOfMass(buffer, pose, camera, cached.centerOfMass(), cached.mass());
        }



    }

    private static void drawQuad(
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
    private static void drawTestArrow(Vec3 look, Vec3 eye){
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
    /// It draws an arrow with VertexFormat.Mode.LINES
    private static void drawVector(
        PoseStack poseStack,
        MultiBufferSource.BufferSource buffer,
        ResolvedForce forceData,
        Camera camera,
        int color
    )
    {

        Vec3 origin = forceData.origin();
        Vec3 delta = forceData.delta();

        Vec3 cameraPosition = camera.getPosition();
        Vec3 cameraDirection = new Vec3(camera.getLookVector());

        poseStack.pushPose();

        poseStack.translate(
            -cameraPosition.x,
            -cameraPosition.y,
            -cameraPosition.z
        );


        VertexConsumer consumer =
            buffer.getBuffer(RenderTypes.FORCE_LINES);



        Vec3 end = origin.add(
            delta.x() * FORCE_VISUAL_SCALE,
            delta.y() * FORCE_VISUAL_SCALE,
            delta.z() * FORCE_VISUAL_SCALE
        );

        drawArrow(
            poseStack,
            consumer,
            origin,
            end,
            cameraDirection,
            color
        );

        drawLine(
            poseStack,
            consumer,
            origin,
            end,
            color
        );

        poseStack.popPose();
    }

    private static void drawArrow(
        PoseStack poseStack,
        VertexConsumer consumer,
        Vec3 start,
        Vec3 end,
        Vec3 cameraLook,
        int color
    ){

        Vec3 delta = end.subtract(start);
        double distance = delta.length();

        if (distance < 0.001)
            return;


        Vec3 direction =  delta.scale(1.0 / distance);
        // lateral vector
        Vec3 side = direction.cross(cameraLook).normalize();

        double headSize = distance * 0.1;


        if (side.lengthSqr() < 0.001)
        {
            side = direction.cross(new Vec3(0, 1, 0)).normalize();
        }

        Vec3 back = end.subtract(
            direction.scale(headSize)
        );


        Vec3 tip1 = back.add(
            side.scale(headSize)
        );

        Vec3 tip2 = back.subtract(
            side.scale(headSize)
        );


        // origin V
        drawLine(
            poseStack,
            consumer,
            end,
            tip1,
            color
        );

        drawLine(
            poseStack,
            consumer,
            end,
            tip2,
            color
        );
    }

    private static void drawLine(
        PoseStack poseStack,
        VertexConsumer consumer,
        Vec3 from,
        Vec3 to,
        int color
    )
    {
        Matrix4f matrix = poseStack.last().pose();

        consumer.addVertex(
                matrix,
                (float)from.x,
                (float)from.y,
                (float)from.z
            )
            .setColor(color)
            .setNormal(
                poseStack.last(),
                0,
                1,
                0
            );


        consumer.addVertex(
                matrix,
                (float)to.x,
                (float)to.y,
                (float)to.z
            )
            .setColor(color)
            .setNormal(
                poseStack.last(),
                0,
                1,
                0
            );
    }


    private static void drawInfo(
        PoseStack pose,
        MultiBufferSource.BufferSource buffer,
        Font font,
        ResolvedForce forceData,
        Camera camera,
        int color,
        Component forceName
    ) {

        String name = forceName.getString();
        String defaultText = " force of ";
        String value = String.format("%.2f pN", forceData.delta().length());

        Component nameText = Component.literal(name)
            .withStyle(style -> style.withColor(color));

        Component valueText = Component.literal(defaultText + " ")
            .withStyle(style -> style.withColor(0xF7F0DD))
            .append(Component.literal(value)
                .withStyle(style -> style.withColor(0xFFFFFF)));

        float nameWidth = font.width(nameText);
        float valueWidth = font.width(valueText);

        Vec3 origin = forceData.origin(),
            delta = forceData.delta();
        Vec3 cameraPosition = camera.getPosition();

        // position = (end + start) / 2 = ((origin + delta)) + origin) / 2 = origin + delta / 2
        Vec3 position = origin.add(delta.scale(0.5 * FORCE_VISUAL_SCALE));

        pose.pushPose();

        float halfWidth = Math.max(nameWidth, valueWidth) * 0.5F + 4;
        float halfHeight = font.lineHeight  + 3;

        applyTransformations(pose, camera, position, cameraPosition);

        Matrix4f matrix = pose.last().pose();

        drawTextBackground(buffer, matrix, halfWidth, halfHeight);

        font.drawInBatch(
            nameText,
            -nameWidth * 0.5F,
            -font.lineHeight,
            0xFFFFFFFF,
            false,
            matrix,
            buffer,
            Font.DisplayMode.SEE_THROUGH,
            0,
            LightTexture.FULL_BRIGHT
        );

        font.drawInBatch(
            valueText,
            -valueWidth * 0.5F,
            0,
            0xFFFFFFFF,
            false,
            matrix,
            buffer,
            Font.DisplayMode.SEE_THROUGH,
            0,
            LightTexture.FULL_BRIGHT
        );

        pose.popPose();
    }

    private static void applyTransformations(PoseStack pose, Camera camera, Vec3 position, Vec3 cameraPosition) {
        pose.translate(
            position.x - cameraPosition.x,
            position.y - cameraPosition.y,
            position.z - cameraPosition.z
        );
        pose.mulPose(camera.rotation());
        pose.scale(
            0.025f,
            -0.025f,
            0.025f
        );
    }

    private static void drawTextBackground(MultiBufferSource.BufferSource buffer, Matrix4f matrix, float halfWidth, float halfHeight){
        VertexConsumer background = buffer.getBuffer(RenderTypes.BACKGROUND_QUADS);

        drawQuad(
            background,
            matrix,
            new float[][]{
                {-halfWidth, halfHeight},
                {halfWidth, halfHeight},
                {halfWidth, -halfHeight},
                {-halfWidth, -halfHeight}
            },
            0x993D3D3A,
            LightTexture.FULL_BRIGHT
        );
    }

    private static void drawQuad(VertexConsumer consumer, Matrix4f matrix, float[][] points, int argb,int packedLight){
        if (points.length != 4 || points[0].length != 2) return;

        for (int i = 0; i < 4; i++) {
            consumer.addVertex(matrix, points[i][0],  points[i][1], 0)
                .setColor(argb)
                .setLight(packedLight);
        }
    }


    private static void drawCenterOfMass(MultiBufferSource.BufferSource buffer, PoseStack pose, Camera camera, Vec3 centerOfMass, double mass) {

        pose.pushPose();
        Vec3 cameraPosition = camera.getPosition();
        applyTransformations(pose, camera, centerOfMass, cameraPosition);

        float halfWidth = + 4;
        float halfHeight =  + 3;

        Matrix4f matrix = pose.last().pose();

        VertexConsumer background = buffer.getBuffer(RenderTypes.BACKGROUND_QUADS);


        drawQuad(
            background,
            matrix,
            new float[][]{
                {-halfWidth, halfHeight},
                {halfWidth, halfHeight},
                {halfWidth, -halfHeight},
                {-halfWidth, -halfHeight}
            },
            0x993D3D3A,
            LightTexture.FULL_BRIGHT
        );
        pose.popPose();
    }
}
