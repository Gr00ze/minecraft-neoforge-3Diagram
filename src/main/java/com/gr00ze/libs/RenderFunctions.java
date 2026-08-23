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

    public static void drawUniformlyColoredCenteredQuad(
        VertexConsumer buffer,
        Matrix4f pose,
        float centerX,
        float centerY,
        float width,
        float height,
        int color
    ) {
        float halfWidth = width / 2.0F;
        float halfHeight = height / 2.0F;

        int numberRowInformation = 2;
        int numberOfVertex = 4;

        float[] vertices = new float[]{
            centerX - halfWidth, centerY + halfHeight,
            centerX + halfWidth, centerY + halfHeight,
            centerX + halfWidth, centerY - halfHeight,
            centerX - halfWidth, centerY - halfHeight,
        };

        for (int i = 0; i < numberOfVertex * numberRowInformation; i+=numberRowInformation) {
            buffer.addVertex(pose, vertices[i],  vertices[i + 1], 0)
                .setColor(color);
        }
    }

    public static void drawUniformlyColoredCenteredQuad(
        VertexConsumer buffer,
        Matrix4f pose,
        float width,
        float height,
        int color
    ) {
        float halfWidth = width / 2.0F;
        float halfHeight = height / 2.0F;

        int numberRowInformation = 2;
        int numberOfVertex = 4;

        float[] vertices = new float[]{
            -halfWidth,  halfHeight,
            halfWidth,  halfHeight,
            halfWidth, -halfHeight,
            -halfWidth, -halfHeight
        };

        for (int i = 0; i < numberOfVertex * numberRowInformation; i+=numberRowInformation) {
            buffer.addVertex(pose, vertices[i],  vertices[i + 1], 0)
                .setColor(color);
        }
    }

    public static void drawTexturedCenteredQuad(
        VertexConsumer buffer,
        Matrix4f pose,
        float width,
        float height
    ) {
        float halfWidth = width / 2.0F;
        float halfHeight = height / 2.0F;

        int numberRowInformation = 4;
        int numberOfVertex = 4;

        float[] vertices = new float[]{
            -halfWidth,  halfHeight, 0, 0,
            halfWidth,  halfHeight, 1, 0,
            halfWidth, -halfHeight, 1, 1,
            -halfWidth, -halfHeight, 0, 1
        };

        for (int i = 0; i < numberOfVertex * numberRowInformation; i+= numberRowInformation) {
            buffer
                .addVertex(pose, vertices[i],  vertices[i + 1], 0)
                .setUv(vertices[i + 2], vertices[i + 3]);
        }
    }
    //It stretches the texture if repeatTexture is false
    public static void drawTexturedRect(
        VertexConsumer buffer,
        Matrix4f pose,
        float startX,
        float startY,
        float width,
        float height,
        boolean repeatTexture
    ) {

        float uMax = 1, vMax = 1;

        if (repeatTexture) {
            uMax = width / 16;
            vMax = height / 16;
        }

        int numberRowInformation = 4;
        int numberOfVertex = 4;



        float[] vertices = new float[]{
            startX        ,  startY, 0, 0,
            startX        ,  startY + height, 0, vMax,
            startX + width,  startY + height, uMax, vMax,
            startX + width,  startY, uMax, 0,
        };

        for (int i = 0; i < numberOfVertex * numberRowInformation; i+= numberRowInformation) {
            buffer
                .addVertex(pose, vertices[i],  vertices[i + 1], 0)
                .setUv(vertices[i + 2], vertices[i + 3]);
        }
    }
}
