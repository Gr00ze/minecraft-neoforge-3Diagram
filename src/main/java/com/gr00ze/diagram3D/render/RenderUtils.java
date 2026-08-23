package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.config.ConfigUtils;
import com.gr00ze.libs.GeometryUtils;
import com.gr00ze.libs.RenderFunctions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nullable;

import static com.gr00ze.diagram3D.Utils.getScaledDelta;
import static com.gr00ze.diagram3D.config.ClientConfig.INFO_BACKGROUND_COLOR;
import static com.gr00ze.diagram3D.config.ClientConfig.INFO_BACKGROUND_USE_TEXTURE;
import static com.gr00ze.diagram3D.render.WorldDiagramDataRenderer.INFO_BACKGROUND_TEXTURE;

public class RenderUtils {
    /**
     * Method to identify if a player is looking a specific point with a threshold of tolerance
     * **/
    public static boolean isPlayerLooking(Camera camera, Vec3 position) {
        Vec3 cameraPos = camera.getPosition();

        Vector3f look = camera.getLookVector().normalize();
        Vector3f direction = position.subtract(cameraPos).normalize().toVector3f();

        double distance = cameraPos.distanceTo(position);

        double angle = Math.atan(0.2 / Math.max(distance, 0.1));

        double threshold = Math.cos(angle);

        return look.dot(direction) >= threshold;
    }

    public static @Nullable GeometryUtils.ClosestPoints getLookingPoints(
        Camera camera,
        Vec3 startPosition,
        Vec3 delta
    ) {
        final double MAX_LOOK_DISTANCE = 50.0;
        final double LOOK_THRESHOLD = 0.5;
        final double EPSILON = 1e-8;

        Vec3 cameraPosition = camera.getPosition();
        Vec3 lookDirection = new Vec3(
            camera.getLookVector().normalize()
        );

        Vec3 rayCastEnd = cameraPosition.add(
            lookDirection.scale(MAX_LOOK_DISTANCE)
        );

        Vec3 lineEnd = startPosition.add(
            getScaledDelta(delta)
        );

        return GeometryUtils.closestPointsWithin(
            cameraPosition,
            rayCastEnd,
            startPosition,
            lineEnd,
            LOOK_THRESHOLD,
            EPSILON
        ).orElse(null);
    }

    public static MutableComponent coloredText(String text, int argb){
        return Component.literal(text)
            .withStyle(style -> style.withColor(argb));
    }

    public static void drawText(MultiBufferSource.BufferSource buffer, Font font, Matrix4f matrix, Component text, float x, float y){
        font.drawInBatch(
            text,
            x,
            y,
            0xFFFFFFFF,
            false,
            matrix,
            buffer,
            Font.DisplayMode.SEE_THROUGH,
            0,
            LightTexture.FULL_BRIGHT
        );
    }

    public static void applyTransformations(PoseStack pose, Camera camera, Vec3 position, Vec3 cameraPosition) {
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

    public static void drawTextBackground(MultiBufferSource.BufferSource buffer, Matrix4f matrix, float width, float height){
        VertexConsumer background;
        if(INFO_BACKGROUND_USE_TEXTURE.get())
        {
            background = buffer.getBuffer(RenderTypes.texturedBackground(INFO_BACKGROUND_TEXTURE));
            RenderFunctions.drawTexturedCenteredQuad(
                background,
                matrix,
                width,
                height
            );
        }

        else{
            background = buffer.getBuffer(RenderTypes.BACKGROUND_QUADS);
            RenderFunctions.drawUniformlyColoredCenteredQuad(
                background,
                matrix,
                width,
                height,
                ConfigUtils.getColor(INFO_BACKGROUND_COLOR));
        }

    }


}
