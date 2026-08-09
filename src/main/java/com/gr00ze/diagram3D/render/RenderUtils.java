package com.gr00ze.diagram3D.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderUtils {

    public static void drawPositionColorLine(
        VertexConsumer consumer,
        PoseStack poseStack,
        Vec3 point1,
        Vec3 point2,
        int color
    ){
        drawPositionColorLine(consumer, poseStack, point1.toVector3f(), point2.toVector3f(), color);
    }
    public static void drawPositionColorLine(
        VertexConsumer consumer,
        PoseStack poseStack,
        Vector3f point1,
        Vector3f point2,
        int color
    )
    {
        Matrix4f matrix = poseStack.last().pose();
        Vector3f dir = point2.sub(point1);


        consumer
            .addVertex(matrix, point1.x, point1.y, point1.z)
            .setColor(color)
            .setNormal(poseStack.last(), dir.x, dir.y, dir.z);

        dir.negate();

        consumer
            .addVertex(matrix, point2.x, point2.y, point2.z)
            .setColor(color)
            .setNormal(poseStack.last(), dir.x, dir.y, dir.z);
    }

    public static void drawQuad(VertexConsumer consumer, Matrix4f matrix, QuadPoints quad, int argb,int packedLight){

        for (Vector3f point : quad.getPoints())
        {
            consumer.addVertex(matrix, point.x,  point.y, point.z)
                .setColor(argb)
                .setLight(packedLight);
        }
    }
}
