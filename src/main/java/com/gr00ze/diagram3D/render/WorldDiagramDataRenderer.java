package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.config.ClientConfig;
import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.config.ConfigUtils;
import com.gr00ze.diagram3D.config.ForceGroupDisplayConfig;
import com.gr00ze.diagram3D.data.DiagramRecords.*;
import com.gr00ze.diagram3D.utils.GeometryUtils;
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


import java.util.ArrayList;
import java.util.List;

import static com.gr00ze.diagram3D.config.ClientConfig.*;
import static com.gr00ze.diagram3D.data.DiagramDataManager.diagramDataCache;
import static com.gr00ze.diagram3D.render.RenderUtils.getScaledDelta;

@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class WorldDiagramDataRenderer {

    private record InformationDisplay(
        Vec3 position,
        List<Component> components
    ) {}

    private record VectorDisplay(
        ResolvedForce force,
        int color
    ) {}
    private record IconDisplay(
        Vec3 position
    ) {}

    private record PreparedData(
        List<VectorDisplay> vectors,
        List<InformationDisplay> information,
        List<IconDisplay> icons
    ) {}

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

        PreparedData preparedData = prepareData(camera);

        drawData(buffer, mc, pose, camera, preparedData);

        RenderSystem.enableDepthTest();

    }

    private static PreparedData prepareData(Camera camera) {
        List<VectorDisplay> preparedVectors = new ArrayList<>();
        List<InformationDisplay> preparedDisplayInfo = new ArrayList<>();
        List<IconDisplay> preparedIcons = new ArrayList<>();

        for (DiagramDataCache cached : diagramDataCache.values()) {
            for (ResolvedForceGroup forceGroup : cached.groups()) {

                ForceGroupDisplayConfig config =
                    ClientConfig.getForceGroupConfig(
                        ConfigUtils.getForceGroupId(forceGroup)
                    );

                if (!config.vectors()) {
                    continue;
                }

                int color = 0xFF000000 | forceGroup.color();
                for (var force: forceGroup.forces()) {
                    preparedVectors.add(new VectorDisplay(force, color));
                }

            }
        }



        VectorInfoMode vMode = DISPLAY_VECTORS_INFO.get();
        switch (vMode) {
            case ALWAYS -> prepareAllVectorInfo(preparedDisplayInfo, camera);
            case ON_LOOK -> prepareLookingVectorInfo(preparedDisplayInfo, camera);
            case DISABLED -> {}
        }

        CenterOfMassMode cMode = DISPLAY_CENTER_OF_MASS.get();
        switch (cMode){
            case ICON -> prepareIcon(preparedDisplayInfo, camera);
            case DETAILS -> prepareMassDetails(preparedDisplayInfo, camera);
            case ICON_DETAILS_ONLOOK -> prepareLookingMassInfo(preparedDisplayInfo, preparedIcons, camera);
            case DISABLED -> {}
        }


        return new PreparedData(preparedVectors, preparedDisplayInfo, preparedIcons);
    }

    private static void prepareAllVectorInfo(List<InformationDisplay> preparedDisplayInfo, Camera camera) {
        for (DiagramDataCache cached : diagramDataCache.values()) {
            for (ResolvedForceGroup forceGroup : cached.groups()) {

                ForceGroupDisplayConfig config =
                    ClientConfig.getForceGroupConfig(
                        ConfigUtils.getForceGroupId(forceGroup)
                    );

                if (!config.info()) {
                    continue;
                }

                String name = forceGroup.name().getString();
                String defaultText = " force of ";

                Component nameComponent = coloredText(name, 0xFF000000 | forceGroup.color());

                for (var force: forceGroup.forces()) {

                    String value = String.format("%.2f pN", force.delta().length());
                    Component valueComponent = coloredText(defaultText + " ", 0xF7F0DD)
                        .append(coloredText(value, 0xFFFFFF));


                    List<Component> components = new ArrayList<>();
                    components.add(nameComponent);
                    components.add(valueComponent);

                    preparedDisplayInfo.add(new InformationDisplay(force.origin().add(getScaledDelta(force.delta()).scale(0.5)), components));
                }

            }
        }
    }

    private static void prepareLookingVectorInfo(List<InformationDisplay> preparedDisplayInfo, Camera camera) {
        LookingForce bestForce = null;

        for (DiagramDataCache cached : diagramDataCache.values()) {
            for (ResolvedForceGroup forceGroup : cached.groups()) {

                ForceGroupDisplayConfig config =
                    ClientConfig.getForceGroupConfig(
                        ConfigUtils.getForceGroupId(forceGroup)
                    );

                if (!config.info()) {
                    continue;
                }

                for (ResolvedForce force : forceGroup.forces()) {

                    GeometryUtils.ClosestPoints closestPoints =
                        getLookingPoints(
                            camera,
                            force.origin(),
                            force.delta()
                        );

                    if (closestPoints == null) {
                        continue;
                    }

                    LookingForce candidate = new LookingForce(
                        force,
                        forceGroup,
                        closestPoints.pointB(),
                        closestPoints.parameterA()
                    );

                    if (bestForce == null
                        || candidate.rayParameter()
                        < bestForce.rayParameter()) {

                        bestForce = candidate;
                    }
                }
            }
        }

        if (bestForce == null) {
            return;
        }

        String name = bestForce.group.name().getString();
        String defaultText = " force of ";
        String value = String.format("%.2f pN", bestForce.force().delta().length());
        Component nameComponent = coloredText(name, 0xFF000000 | bestForce.group.color());
        Component valueComponent = coloredText(defaultText + " ", 0xF7F0DD)
            .append(coloredText(value, 0xFFFFFF));

        List<Component> components = new ArrayList<>();
        components.add(nameComponent);
        components.add(valueComponent);

        preparedDisplayInfo.add(new InformationDisplay(bestForce.position, components));

    }

    private static void prepareIcon(List<InformationDisplay> preparedDisplayInfo, Camera camera) {

    }

    private static void prepareMassDetails(List<InformationDisplay> preparedDisplayInfo, Camera camera) {
        for (DiagramDataCache cached : diagramDataCache.values()) {

            Component text = coloredText("Center of mass",0xFFAA7733);
            Component value = coloredText(String.format("%.2f Kpg", cached.mass()),0xFFFFFFFF);
            List<Component> components = new ArrayList<>();
            components.add(text);
            components.add(value);
            preparedDisplayInfo.add(new InformationDisplay(cached.centerOfMass(), components));
        }
    }

    private static void prepareLookingMassInfo(List<InformationDisplay> preparedDisplayInfo, List<IconDisplay> preparedIcons, Camera camera) {

        for (DiagramDataCache cached : diagramDataCache.values()) {
            Vec3 centerOfMass = cached.centerOfMass();

            if(isPlayerLooking(camera, centerOfMass)) {
                Component text = coloredText("Center of mass",0xFFAA7733);
                Component value = coloredText(String.format("%.2f Kpg", cached.mass()),0xFFFFFFFF);
                List<Component> components = new ArrayList<>();
                components.add(text);
                components.add(value);

                preparedDisplayInfo.add(new InformationDisplay(centerOfMass, components));

            }else {
                preparedIcons.add(new IconDisplay(centerOfMass));
            }


        }

    }

    private static void drawData(MultiBufferSource.BufferSource buffer, Minecraft mc, PoseStack pose, Camera camera, PreparedData preparedData) {
        for (VectorDisplay vectorDisplay : preparedData.vectors) {
            drawVector(
                buffer,
                pose,
                camera,
                vectorDisplay.force(),
                vectorDisplay.color()
            );
        }

        for (InformationDisplay informationDisplay : preparedData.information) {
            drawInfoDisplay(
                buffer,
                mc.font,
                pose,
                camera,
                informationDisplay.position(),
                informationDisplay.components().toArray(new Component[0])
            );
        }

        for (IconDisplay iconDisplay : preparedData.icons) {
            drawCenterOfMassIcon(buffer, pose, camera, iconDisplay.position);
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



        Vec3 end = origin.add(getScaledDelta(delta));

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


    private static void drawVectorInfo(
        MultiBufferSource.BufferSource buffer,
        Font font,
        PoseStack pose,
        Camera camera
    ) {
        VectorInfoMode mode = DISPLAY_VECTORS_INFO.get();

        if (mode == VectorInfoMode.DISABLED) {
            return;
        }

        if (mode == VectorInfoMode.ALWAYS) {
            drawAllVectorInfo(
                buffer,
                font,
                pose,
                camera
            );
            return;
        }

        if (mode == VectorInfoMode.ON_LOOK) {
            drawLookingVectorInfo(
                buffer,
                font,
                pose,
                camera
            );
        }
    }


    private static void drawAllVectorInfo(
        MultiBufferSource.BufferSource buffer,
        Font font,
        PoseStack pose,
        Camera camera
    ) {
        for (DiagramDataCache cached : diagramDataCache.values()) {
            for (ResolvedForceGroup forceGroup : cached.groups()) {

                ForceGroupDisplayConfig config =
                    ClientConfig.getForceGroupConfig(
                        ConfigUtils.getForceGroupId(forceGroup)
                    );

                if (!config.vectors()) {
                    continue;
                }

                int color = 0xFF000000 | forceGroup.color();

                for (ResolvedForce force : forceGroup.forces()) {
                    drawVectorInfoDisplay(
                        buffer,
                        font,
                        pose,
                        camera,
                        force,
                        color,
                        forceGroup.name(),
                        force.origin().add(getScaledDelta(force.delta()).scale(0.5))
                    );
                }
            }
        }
    }

    private record LookingForce(
        ResolvedForce force,
        ResolvedForceGroup group,
        Vec3 position,
        double rayParameter
    ) {}

    private static void drawLookingVectorInfo(
        MultiBufferSource.BufferSource buffer,
        Font font,
        PoseStack pose,
        Camera camera
    ) {
        LookingForce bestForce = null;

        for (DiagramDataCache cached : diagramDataCache.values()) {
            for (ResolvedForceGroup forceGroup : cached.groups()) {

                ForceGroupDisplayConfig config =
                    ClientConfig.getForceGroupConfig(
                        ConfigUtils.getForceGroupId(forceGroup)
                    );

                if (!config.info()) {
                    continue;
                }

                for (ResolvedForce force : forceGroup.forces()) {

                    GeometryUtils.ClosestPoints closestPoints =
                        getLookingPoints(
                            camera,
                            force.origin(),
                            force.delta()
                        );

                    if (closestPoints == null) {
                        continue;
                    }

                    LookingForce candidate = new LookingForce(
                        force,
                        forceGroup,
                        closestPoints.pointB(),
                        closestPoints.parameterA()
                    );

                    if (bestForce == null
                        || candidate.rayParameter()
                        < bestForce.rayParameter()) {

                        bestForce = candidate;
                    }
                }
            }
        }

        if (bestForce == null) {
            return;
        }

        int color = 0xFF000000 | bestForce.group().color();

        drawVectorInfoDisplay(
            buffer,
            font,
            pose,
            camera,
            bestForce.force(),
            color,
            bestForce.group().name(),
            bestForce.position()
        );
    }

    private static GeometryUtils.ClosestPoints getLookingPoints(
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
        Component forceName,
        Vec3 position
    ) {
        String name = forceName.getString();
        String defaultText = " force of ";
        String value = String.format("%.2f pN", forceData.delta().length());

        Component nameComponent = coloredText(name, color);
        Component valueComponent = coloredText(defaultText + " ", 0xF7F0DD)
            .append(coloredText(value, 0xFFFFFF));

        drawInfoDisplay(
            buffer,
            font,
            pose,
            camera,
            position,
            nameComponent,
            valueComponent
        );
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


    private static void drawCenterOfMass(MultiBufferSource.BufferSource buffer, Font font, PoseStack pose, Camera camera){
        for(DiagramDataCache forceGroup: diagramDataCache.values()){
            drawCenterOfMass(
                buffer,
                font,
                pose,
                camera,
                forceGroup.centerOfMass(),
                forceGroup.mass()
            );
        }
    }

    private static void drawCenterOfMass(MultiBufferSource.BufferSource buffer, Font font, PoseStack pose, Camera camera, Vec3 centerOfMass, double mass) {
        CenterOfMassMode display_center_of_mass = DISPLAY_CENTER_OF_MASS.get();
        if (display_center_of_mass.equals(CenterOfMassMode.DISABLED) ) return;
        if (display_center_of_mass.equals(CenterOfMassMode.DETAILS) ) {
            drawCenterOfMassWithDetails(buffer, font, pose, camera, centerOfMass, mass);
            return;
        }
        if (display_center_of_mass.equals(CenterOfMassMode.ICON_DETAILS_ONLOOK)){
            if(isPlayerLooking(camera, centerOfMass)){
                drawCenterOfMassWithDetails(buffer, font, pose, camera, centerOfMass, mass);

            } else {
                drawCenterOfMassIcon(buffer, pose, camera, centerOfMass);
            }
            return;
        }
        drawCenterOfMassIcon(buffer, pose, camera, centerOfMass);

    }
    /**
     * Method to identify if a player is looking a specific point with a threshold of tolerance
     * **/
    private static boolean isPlayerLooking(Camera camera, Vec3 position) {
        Vec3 cameraPos = camera.getPosition();

        Vector3f look = camera.getLookVector().normalize();
        Vector3f direction = position.subtract(cameraPos).normalize().toVector3f();

        double distance = cameraPos.distanceTo(position);

        double angle = Math.atan(0.2 / Math.max(distance, 0.1));

        double threshold = Math.cos(angle);

        return look.dot(direction) >= threshold;
    }

    /**
     * Method to identify if a player is looking at force vector
     * **/
    private static boolean isPlayerLooking(
        Camera camera,
        Vec3 startPosition,
        Vec3 delta
    ) {
        final double MAX_LOOK_DISTANCE = 50.0;
        final double LOOK_THRESHOLD = 0.5;
        final double EPSILON = 1e-8;

        Vec3 cameraPosition = camera.getPosition();
        Vec3 lookDirection = new Vec3(camera.getLookVector().normalize());

        Vec3 playerRayCastLookVector =
            lookDirection.scale(MAX_LOOK_DISTANCE);

        Vec3 lineVector = getScaledDelta(delta);

        Vec3 rayCastEnd =
            cameraPosition.add(playerRayCastLookVector);

        Vec3 lineEnd =
            startPosition.add(lineVector);

        return GeometryUtils.segmentsIntersectWithin(
            cameraPosition,
            rayCastEnd,
            startPosition,
            lineEnd,
            LOOK_THRESHOLD,
            EPSILON
        );
    }

    private static Vec3 getLookingPoint(
        Camera camera,
        Vec3 startPosition,
        Vec3 delta
    ) {
        final double MAX_LOOK_DISTANCE = 50.0;
        final double LOOK_THRESHOLD = 0.5;
        final double EPSILON = 1e-8;

        Vec3 cameraPosition = camera.getPosition();
        Vec3 lookDirection = new Vec3(camera.getLookVector().normalize());

        Vec3 rayCastLookVector =
            lookDirection.scale(MAX_LOOK_DISTANCE);

        Vec3 lineVector = getScaledDelta(delta);

        Vec3 rayCastEnd =
            cameraPosition.add(rayCastLookVector);

        Vec3 lineEnd =
            startPosition.add(lineVector);

        return GeometryUtils.closestPointsWithin(
            cameraPosition,
            rayCastEnd,
            startPosition,
            lineEnd,
            LOOK_THRESHOLD,
            EPSILON
        ).map(GeometryUtils.ClosestPoints::pointB).orElse(null);
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
