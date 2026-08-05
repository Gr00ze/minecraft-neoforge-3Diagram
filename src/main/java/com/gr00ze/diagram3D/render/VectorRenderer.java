package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.data.DiagramRecords.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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

        PoseStack pose = event.getPoseStack();

        Minecraft mc = Minecraft.getInstance();

        Vec3 camera = mc.gameRenderer
            .getMainCamera()
            .getPosition();

        pose.pushPose();

        pose.translate(
            -camera.x,
            -camera.y,
            -camera.z
        );

        MultiBufferSource.BufferSource buffer =
            mc.renderBuffers().bufferSource();

        VertexConsumer consumer =
            buffer.getBuffer(RenderTypes.FORCE_LINES);

        Vec3 look = mc.player.getLookAngle();

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
                        consumer,
                        pointForce.from(),
                        pointForce.to(),
                        look,
                        0xFF000000 | forcesGroup.color()
                    );

                }
            }
        }

        buffer.endBatch();

        pose.popPose();
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

    private static void drawVector(
        PoseStack poseStack,
        VertexConsumer consumer,
        Vec3 point,
        Vec3 force,
        Vec3 cameraLook,
        int color
    )
    {
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
            cameraLook,
            color
        );

        drawLine(
            poseStack,
            consumer,
            start,
            end,
            color
        );
    }

    private static void drawArrow(
        PoseStack poseStack,
        VertexConsumer consumer,
        Vec3 start,
        Vec3 end,
        Vec3 cameraLook,
        int color
    ){
        Vec3 direction = end.subtract(start).normalize();

        double headLength = 0.4;
        double headWidth = 0.25;


        // lateral vector
        Vec3 side = direction.cross(cameraLook).normalize();

        if (side.lengthSqr() < 0.001)
        {
            side = direction.cross(new Vec3(0, 1, 0)).normalize();
        }

        Vec3 back = end.subtract(
            direction.scale(headLength)
        );


        Vec3 tip1 = back.add(
            side.scale(headWidth)
        );

        Vec3 tip2 = back.subtract(
            side.scale(headWidth)
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

}
