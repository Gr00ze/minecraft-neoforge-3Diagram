package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.config.ClientConfig;
import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.config.ConfigUtils;
import com.gr00ze.diagram3D.config.ForceGroupDisplayConfig;
import com.gr00ze.diagram3D.data.DiagramRecords.*;
import com.mojang.blaze3d.systems.RenderSystem;
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
import org.joml.Vector3f;

import static com.gr00ze.diagram3D.config.ClientConfig.*;
import static com.gr00ze.diagram3D.data.DiagramDataManager.diagramDataCache;

@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class WorldDiagramDataRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event){

        //System.out.println(event.getStage() + ":"+GL11.glGetInteger(GL11.GL_DEPTH_FUNC) + " " + GL11.glIsEnabled(GL11.GL_DEPTH_TEST));
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER)
            return;

        Minecraft mc = Minecraft.getInstance();

        Player player = mc.player;
        if(player == null)
            return;

        PoseStack pose = event.getPoseStack();
        Camera camera = mc.gameRenderer.getMainCamera();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        RenderSystem.disableDepthTest();

        drawData(buffer, mc, pose, camera);

        RenderSystem.enableDepthTest();

    }

    private static void drawData(MultiBufferSource.BufferSource buffer, Minecraft mc, PoseStack pose, Camera camera) {
        for (DiagramDataCache cached : diagramDataCache.values())
        {
            drawCenterOfMass(buffer, mc.font, pose, camera, cached.centerOfMass(), cached.mass());
            for (ResolvedForceGroup forceGroup : cached.groups())
            {
                int forceGroupColorARGB = 0xFF000000 | forceGroup.color();

                ForceGroupDisplayConfig config =
                    ClientConfig.getForceGroupConfig(ConfigUtils.getForceGroupId(forceGroup));


                for (ResolvedForce pointForce : forceGroup.forces())
                {
                    if(config.info())
                        drawVectorInfoDisplay(buffer, mc.font, pose, camera,
                            pointForce, forceGroupColorARGB, forceGroup.name()
                        );

                    if(config.vectors())
                        drawVector(buffer, pose, camera,
                            pointForce, forceGroupColorARGB
                        );




                }
            }

        }
    }


    /// It draws an arrow with VertexFormat.Mode.LINES
    private static void drawVector(MultiBufferSource.BufferSource buffer, PoseStack poseStack, Camera camera, ResolvedForce forceData, int color
    )
    {
        if (!DISPLAY_VECTORS.get()) return;
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


    private static void drawInfoDisplay(
        MultiBufferSource.BufferSource buffer,
        Font font,
        PoseStack pose,
        Camera camera,
        Vec3 position,
        Component ...components
        ){

        float maxWidth = 0;
        for (Component component : components){
            maxWidth = Math.max(maxWidth, font.width(component));
        }
        float rowHeight =  font.lineHeight  + 3; //3 of margin
        float backgroundHeight = rowHeight * components.length;

        Vec3 cameraPosition = camera.getPosition();
        pose.pushPose();
        applyTransformations(pose, camera, position, cameraPosition);
        Matrix4f matrix = pose.last().pose();
        drawTextBackground(buffer, matrix, maxWidth + 4, backgroundHeight);

        float rowCount = 0;
        for (Component component : components){
            drawText(
                buffer,
                font,
                matrix,
                component,
                -font.width(component)/2F,
                font.lineHeight * (rowCount - 1) //Add a constant will move down all rows
            );
            rowCount+=1.2F;//Increase this will increase the row gap
        }
        pose.popPose();


    }

    private static void drawVectorInfoDisplay(
        MultiBufferSource.BufferSource buffer,
        Font font,
        PoseStack pose,
        Camera camera,
        ResolvedForce forceData,
        int color,
        Component forceName
    ) {
        if (!DISPLAY_VECTORS_INFO.get()) return;
        String name = forceName.getString();
        String defaultText = " force of ";
        String value = String.format("%.2f pN", forceData.delta().length());

        Component nameComponent = coloredText(name, color);
        Component valueComponent = coloredText(defaultText + " ", 0xF7F0DD)
            .append(coloredText(value, 0xFFFFFF));

        Vec3 origin = forceData.origin(),
            delta = forceData.delta();
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
        drawInfoDisplay(buffer, font, pose, camera, position, nameComponent, valueComponent);

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

    private static void drawTextBackground(MultiBufferSource.BufferSource buffer, Matrix4f matrix, float width, float height){
        VertexConsumer background = buffer.getBuffer(RenderTypes.BACKGROUND_QUADS);

        RenderUtils.drawQuad(
            background,
            matrix,
            QuadPoints.centeredXY(width, height),
            0x993D3D3A,
            LightTexture.FULL_BRIGHT
        );
    }




    private static void drawCenterOfMass(MultiBufferSource.BufferSource buffer, Font font, PoseStack pose, Camera camera, Vec3 centerOfMass, double mass) {
        CenterOfMassMode display_center_of_mass = DISPLAY_CENTER_OF_MASS.get();
        if (display_center_of_mass.equals(CenterOfMassMode.DISABLED) ) return;
        if (display_center_of_mass.equals(CenterOfMassMode.DETAILS) ) {
            drawCenterOfMassWithDetails(buffer, font, pose, camera, centerOfMass, mass);
            return;
        }
        drawCenterOfMassIcon(buffer, font, pose, camera, centerOfMass);
        drawCenterOfMassIcon(buffer, pose, camera, centerOfMass);

    }

    private static void drawCenterOfMassWithDetails(MultiBufferSource.BufferSource buffer, Font font, PoseStack pose, Camera camera, Vec3 centerOfMass, double mass){
        Component text = coloredText("Center of mass",0xFFAA7733);
        Component value = coloredText(String.format("%.2f Kpg", mass),0xFFFFFFFF);
        drawInfoDisplay(buffer, font, pose, camera, centerOfMass, text, value);

    }
    private static void drawCenterOfMassIcon(
        MultiBufferSource.BufferSource buffer,
        PoseStack pose,
        Camera camera,
        Vec3 centerOfMass
    ) {
        VertexConsumer background =
            buffer.getBuffer(RenderTypes.BACKGROUND_QUADS);

        Vec3 cameraPosition = camera.getPosition();

        float size = 8F;
        final float halfSize = size * 0.5F;
        final float outline = 1F;

        int dark = 0xFF4A2F18;
        int light = 0xFF704A27;
        int white = 0xFFFFFFFF;

        pose.pushPose();

        applyTransformations(
            pose,
            camera,
            centerOfMass,
            cameraPosition
        );

        Matrix4f matrix = pose.last().pose();

        // outline/background
        RenderUtils.drawQuad(
            background,
            matrix,
            QuadPoints.centeredXY(
                0F,
                0F,
                size + outline,
                size + outline
            ),
            white,
            LightTexture.FULL_BRIGHT
        );
        // top-left
        RenderUtils.drawQuad(
            background,
            matrix,
            QuadPoints.centeredXY(
                -halfSize,
                halfSize,
                halfSize,
                halfSize
            ),
            dark,
            LightTexture.FULL_BRIGHT
        );

        // top-right
        RenderUtils.drawQuad(
            background,
            matrix,
            QuadPoints.centeredXY(
                halfSize,
                halfSize,
                halfSize,
                halfSize
            ),
            light,
            LightTexture.FULL_BRIGHT
        );

        // bottom-left
        RenderUtils.drawQuad(
            background,
            matrix,
            QuadPoints.centeredXY(
                -halfSize,
                -halfSize,
                halfSize,
                halfSize
            ),
            light,
            LightTexture.FULL_BRIGHT
        );

        // bottom-right
        RenderUtils.drawQuad(
            background,
            matrix,
            QuadPoints.centeredXY(
                halfSize,
                -halfSize,
                halfSize,
                halfSize
            ),
            dark,
            LightTexture.FULL_BRIGHT
        );

        pose.popPose();
    }
    public static MutableComponent coloredText(String text, int argb){
        return Component.literal(text)
            .withStyle(style -> style.withColor(argb));
    }

    public static void drawCenteredText(MultiBufferSource.BufferSource buffer, Font font, Matrix4f matrix, Component text, float width, float height){
        drawText(
            buffer,
            font,
            matrix,
            text,
            -width * 0.5F,
            -height * 0.5F
        );

    }
    public static void drawText(MultiBufferSource.BufferSource buffer, Font font, Matrix4f matrix, Component text, float width, float height){
        font.drawInBatch(
            text,
            width,
            height,
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
