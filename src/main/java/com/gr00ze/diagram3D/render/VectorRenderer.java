package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.data.DiagramRecords.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
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
        Vec3 cameraPosition = camera.getPosition();
        Vec3 cameraDirection = new Vec3(camera.getLookVector());

        MultiBufferSource.BufferSource buffer =
            mc.renderBuffers().bufferSource();



        Vec3 position = player.position();
        pose.pushPose();

        pose.translate(
            -cameraPosition.x,
            -cameraPosition.y,
            -cameraPosition.z
        );
//        pose.mulPose(
//            Minecraft.getInstance()
//                .gameRenderer
//                .getMainCamera()
//                .rotation()
//        );

//        pose.scale(
//            -10.25f,
//            -10.25f,
//            10.25f
//        );
        mc.font.drawInBatch(
            "name",
            -mc.font.width("name") / 2.0f,
            0,
            0xFFFFFFFF,
            false,
            pose.last().pose(),
            buffer,
            Font.DisplayMode.SEE_THROUGH,
            0xAAFFFFFF,
            LightTexture.FULL_BRIGHT
        );

        pose.popPose();


        for (DiagramDataCache cached : diagramDataCache.values())
        {
            for (ResolvedForceGroup forcesGroup : cached.groups())
            {
                for (ResolvedForce pointForce : forcesGroup.forces())
                {
//                    Diagram3D.LOGGER.info(
//                        "Point {} Force {}",
//                        pointForce.from(),
//                        pointForce.to()
//                    );
                    drawVector(
                        pose,
                        buffer,
                        pointForce.from(),
                        pointForce.to(),
                        cameraPosition,
                        cameraDirection,
                        0xFF000000 | forcesGroup.color()
                    );

                    drawInfo(
                        pose,
                        buffer,
                        mc.font,
                        pointForce.from(),
                        pointForce.to(),
                        cameraPosition,
                        0xFF000000 | forcesGroup.color(),
                        forcesGroup.name()
                    );


                }
            }
        }

        buffer.endBatch();

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
        Vec3 point,
        Vec3 force,
        Vec3 cameraPosition,
        Vec3 cameraDirection,
        int color
    )
    {
        poseStack.pushPose();

        poseStack.translate(
            -cameraPosition.x,
            -cameraPosition.y,
            -cameraPosition.z
        );


        VertexConsumer consumer =
            buffer.getBuffer(RenderTypes.FORCE_LINES);

        Vec3 start = new Vec3(
            point.x(),
            point.y(),
            point.z()
        );

        Vec3 end = start.add(
            force.x() * FORCE_VISUAL_SCALE,
            force.y() * FORCE_VISUAL_SCALE,
            force.z() * FORCE_VISUAL_SCALE
        );

        drawArrow(
            poseStack,
            consumer,
            start,
            end,
            cameraDirection,
            color
        );

        drawLine(
            poseStack,
            consumer,
            start,
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


        // from V
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


    private static void drawInfo(PoseStack pose, MultiBufferSource.BufferSource buffer, Font font, Vec3 from, Vec3 to, Vec3 cameraPosition, int color, Component name) {

        Vec3 position = from.add(to).scale(0.5);
        pose.pushPose();

        pose.translate(
            position.x,
            position.y,
            position.z
        );
//        pose.mulPose(
//            Minecraft.getInstance()
//                .gameRenderer
//                .getMainCamera()
//                .rotation()
//        );

        pose.scale(
            -0.25f,
            -0.25f,
            0.25f
        );
        font.drawInBatch(
            name,
            -font.width(name) / 2.0f,
            0,
            color,
            false,
            pose.last().pose(),
            buffer,
            Font.DisplayMode.SEE_THROUGH,
            0,
            LightTexture.FULL_BRIGHT
        );

        pose.popPose();
    }
}
