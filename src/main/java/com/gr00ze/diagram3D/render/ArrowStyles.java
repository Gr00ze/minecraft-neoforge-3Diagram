package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.libs.RenderFunctions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import static com.gr00ze.diagram3D.config.ClientConfig.DISPLAY_VECTOR_OUTLINE;

public class ArrowStyles {

    public static final ResourceLocation VECTOR_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(
            Diagram3D.MOD_ID,
            "textures/vector/template.png"
        );

    public static void drawSlimArrow(PoseStack poseStack, MultiBufferSource.BufferSource buffer, Vec3 origin, Vec3 end, Vec3 cameraDirection, int color

    ){

        if(DISPLAY_VECTOR_OUTLINE.get()){
            VertexConsumer consumer =
                buffer.getBuffer(RenderTypes.FORCE_OUT_LINES);
            ArrowStyles.drawSlimHead(
                poseStack,
                consumer,
                origin,
                end,
                cameraDirection,
                0xFFFFFFFF
            );

            RenderFunctions.drawPositionColorLine(
                consumer,
                poseStack,
                origin,
                end,
                0xFFFFFFFF
            );
        }

        VertexConsumer consumer =
            buffer.getBuffer(RenderTypes.FORCE_LINES);

        drawSlimHead(
            poseStack,
            consumer,
            origin,
            end,
            cameraDirection,
            color
        );

        RenderFunctions.drawPositionColorLine(
            consumer,
            poseStack,
            origin,
            end,
            color
        );
    }
    public static void drawSlimHead(
        PoseStack poseStack,
        VertexConsumer consumer,
        Vec3 start,
        Vec3 end,
        Vec3 cameraLook,
        int color
    ){

        Vec3 delta = end.subtract(start);
        double distance = delta.length();

        if (distance < 0.01)
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
        RenderFunctions.drawPositionColorLine(
            consumer,
            poseStack,
            end,
            tip1,
            color
        );

        RenderFunctions.drawPositionColorLine(
            consumer,
            poseStack,
            end,
            tip2,
            color
        );
    }

    public static void drawTexturedArrow(
        PoseStack poseStack,
        MultiBufferSource.BufferSource buffer,
        Vec3 origin,
        Vec3 end,
        Vec3 cameraDirection,
        int color
    ) {
        if (DISPLAY_VECTOR_OUTLINE.get()) {
            VertexConsumer outlineConsumer =
                buffer.getBuffer(
                    RenderTypes.texturedArrow(VECTOR_TEXTURE)
                );

            drawTexturedArrowTriangle(
                poseStack,
                outlineConsumer,
                origin,
                end,
                cameraDirection,
                0xFFFFFFFF,
                1.15
            );
        }

        VertexConsumer consumer =
            buffer.getBuffer(
                RenderTypes.texturedArrow(VECTOR_TEXTURE)
            );

        drawTexturedArrowTriangle(
            poseStack,
            consumer,
            origin,
            end,
            cameraDirection,
            color,
            1.0
        );
    }

    private static void drawTexturedArrowTriangle(
        PoseStack poseStack,
        VertexConsumer consumer,
        Vec3 start,
        Vec3 end,
        Vec3 cameraLook,
        int color,
        double widthScale
    ) {
        Vec3 delta = end.subtract(start);
        double distance = delta.length();

        if (distance < 0.01)
            return;

        Vec3 direction = delta.scale(1.0 / distance);

        Vec3 side = direction.cross(cameraLook);

        if (side.lengthSqr() < 0.001) {
            side = direction.cross(new Vec3(0, 1, 0));

            if (side.lengthSqr() < 0.001) {
                side = direction.cross(new Vec3(1, 0, 0));
            }
        }

        side = side.normalize();

        double headSize = distance * 0.5 * widthScale;

        Vec3 tip1 = start.add(
            side.scale(headSize)
        );

        Vec3 tip2 = start.subtract(
            side.scale(headSize)
        );

        RenderFunctions.drawTexturedTriangle(
            consumer,
            poseStack,
            end,
            tip1,
            tip2,
            color
        );
    }


}
