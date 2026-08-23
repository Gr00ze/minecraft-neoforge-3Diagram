package com.gr00ze.libs;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderFunctions {
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
        Vector3f dir = point2.sub(point1, new Vector3f());


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

    public static void drawTexturedTriangle(
        VertexConsumer consumer,
        PoseStack poseStack,
        Vec3 a,
        Vec3 b,
        Vec3 c,
        int color
    ) {
        Matrix4f matrix = poseStack.last().pose();

        consumer.addVertex(matrix,
                (float) a.x,
                (float) a.y,
                (float) a.z)
            .setColor(color)
            .setUv(0.5f, 0.0f)
            .setUv2(0, 0);

        consumer.addVertex(matrix,
                (float) b.x,
                (float) b.y,
                (float) b.z)
            .setColor(color)
            .setUv(0.0f, 1.0f)
            .setUv2(0, 0);

        consumer.addVertex(matrix,
                (float) c.x,
                (float) c.y,
                (float) c.z)
            .setColor(color)
            .setUv(1.0f, 1.0f)
            .setUv2(0, 0);
    }
}
