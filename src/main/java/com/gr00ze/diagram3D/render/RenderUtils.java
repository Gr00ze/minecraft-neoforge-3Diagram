package com.gr00ze.diagram3D.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import static com.gr00ze.diagram3D.config.ClientConfig.*;

public class RenderUtils {



//    public static void drawUniformlyColoredQuad(VertexConsumer consumer, Matrix4f matrix, QuadPoints quad, int argb,int packedLight){
//
//        for (Vector3f point : quad.getPoints())
//        {
//            consumer.addVertex(matrix, point.x,  point.y, point.z)
//                .setColor(argb)
//                .setLight(packedLight);
//        }
//    }

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

//    public static void drawTexturedQuad(VertexConsumer consumer, Matrix4f matrix, TexturedQuadPoints quad){
//
//        for (var vertex : quad.getVertices())
//        {
//            consumer
//                .addVertex(
//                matrix,
//                vertex.position().x,
//                vertex.position().y,
//                vertex.position().z
//                )
//                .setUv(vertex.u(), vertex.v());
//        }
//    }

    public static Vec3 getScaledDelta(Vec3 original) {
        double x = original.length();

        if (x == 0.0) {
            return Vec3.ZERO;
        }

        double targetLength =
            x * x * QUADRATIC_VECTOR_SCALING_FACTOR.get()
                + x * PROPORTIONAL_VECTOR_SCALING_FACTOR.get();

        return original.normalize().scale(targetLength);
    }


}
