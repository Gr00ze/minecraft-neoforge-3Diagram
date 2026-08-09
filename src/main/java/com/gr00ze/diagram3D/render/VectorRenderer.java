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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

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

        for (DiagramDataCache cached : diagramDataCache.values())
        {
            for (ResolvedForceGroup forcesGroup : cached.groups())
            {
                int forceGroupColorARGB = 0xFF000000 | forcesGroup.color();
                for (ResolvedForce pointForce : forcesGroup.forces())
                {

                    drawVector(
                        buffer,
                        pose, camera,
                        pointForce, forceGroupColorARGB
                    );

                    drawInfo(
                        buffer, mc.font,
                        pose, camera,
                        pointForce, forceGroupColorARGB, forcesGroup.name()
                    );


                }
            }
            drawCenterOfMass(buffer, mc.font, pose, camera, cached.centerOfMass(), cached.mass());
        }



    }


    /// It draws an arrow with VertexFormat.Mode.LINES
    private static void drawVector(MultiBufferSource.BufferSource buffer, PoseStack poseStack, Camera camera, ResolvedForce forceData, int color
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

        RenderUtils.drawPositionColorLine(
            consumer,
            poseStack,
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
        RenderUtils.drawPositionColorLine(
            consumer,
            poseStack,
            end,
            tip1,
            color
        );

        RenderUtils.drawPositionColorLine(
            consumer,
            poseStack,
            end,
            tip2,
            color
        );
    }




    private static void drawInfo(MultiBufferSource.BufferSource buffer, Font font, PoseStack pose, Camera camera, ResolvedForce forceData, int color,
        Component forceName
    ) {

        String name = forceName.getString();
        String defaultText = " force of ";
        String value = String.format("%.2f pN", forceData.delta().length());

        Component nameText = coloredText(name, color);
        Component valueText = coloredText(defaultText + " ", 0xF7F0DD)
            .append(coloredText(value, 0xFFFFFF));

//        Component nameText = Component.literal(name)
//            .withStyle(style -> style.withColor(color));
//
//        Component valueText = Component.literal(defaultText + " ")
//            .withStyle(style -> style.withColor(0xF7F0DD))
//            .append(Component.literal(value)
//                .withStyle(style -> style.withColor(0xFFFFFF)));

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

        drawText(
            buffer,
            font,
            matrix,
            nameText,
            nameWidth,
            font.lineHeight * 2
        );

        drawText(
            buffer,
            font,
            matrix,
            valueText,
            valueWidth,
            0
        );
//        font.drawInBatch(
//            nameText,
//            -nameWidth * 0.5F,
//            -font.lineHeight,
//            0xFFFFFFFF,
//            false,
//            matrix,
//            buffer,
//            Font.DisplayMode.SEE_THROUGH,
//            0,
//            LightTexture.FULL_BRIGHT
//        );
//
//        font.drawInBatch(
//            valueText,
//            -valueWidth * 0.5F,
//            0,
//            0xFFFFFFFF,
//            false,
//            matrix,
//            buffer,
//            Font.DisplayMode.SEE_THROUGH,
//            0,
//            LightTexture.FULL_BRIGHT
//        );

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

        RenderUtils.drawQuad(
            background,
            matrix,
            QuadPoints.centeredXY(halfWidth, halfHeight),
            0x993D3D3A,
            LightTexture.FULL_BRIGHT
        );
    }




    private static void drawCenterOfMass(MultiBufferSource.BufferSource buffer, Font font, PoseStack pose, Camera camera, Vec3 centerOfMass, double mass) {
        VertexConsumer background = buffer.getBuffer(RenderTypes.BACKGROUND_QUADS);
        Vec3 cameraPosition = camera.getPosition();

        Component text = coloredText("Center of mass",0xFFAAAA00);
        Component value = coloredText(String.format("%.2f Kpg", mass),0xFFFFFFFF);

        float width = font.width(text) ;

        pose.pushPose();
        applyTransformations(pose, camera, centerOfMass, cameraPosition);
        Matrix4f matrix = pose.last().pose();
        RenderUtils.drawQuad(
            background,
            matrix,
            QuadPoints.centeredXY(width * 0.5F + 4, font.lineHeight + 4),
            0x993D3D3A,
            LightTexture.FULL_BRIGHT
        );



        drawText(
            buffer,
            font,
            matrix,
            text,
            width,
            font.lineHeight * 2
        );
        drawText(
            buffer,
            font,
            matrix,
            value,
            width,
            0
            );


        pose.popPose();
    }

    public static MutableComponent coloredText(String text, int argb){
        return Component.literal(text)
            .withStyle(style -> style.withColor(argb));
    }

    public static void drawText(MultiBufferSource.BufferSource buffer, Font font, Matrix4f matrix, Component text, float width, float height){
        font.drawInBatch(
            text,
            -width * 0.5F,
            -height * 0.5F,
            0xFFFFFFFF,
            false,
            matrix,
            buffer,
            Font.DisplayMode.SEE_THROUGH,
            0,
            LightTexture.FULL_BRIGHT
        );
    }
}
