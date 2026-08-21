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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.gr00ze.diagram3D.config.ClientConfig.*;
import static com.gr00ze.diagram3D.data.DiagramDataManager.diagramDataCache;
import static com.gr00ze.diagram3D.render.RenderUtils.getScaledDelta;
import static com.gr00ze.diagram3D.render.RenderPreparation.PreparedForce;
import static com.gr00ze.diagram3D.render.RenderPreparation.PreparedGroup;
import static com.gr00ze.diagram3D.render.RenderPreparation.InformationDisplay;
import static com.gr00ze.diagram3D.render.RenderPreparation.IconDisplay;
import static com.gr00ze.diagram3D.render.RenderPreparation.LookingForce;
import static com.gr00ze.diagram3D.render.RenderPreparation.PreparedDiagram;


@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class WorldDiagramDataRenderer {

    public static final ResourceLocation INFO_BACKGROUND_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(
            Diagram3D.MOD_ID,
            "textures/info/background.png"
        );

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

        RenderPreparation renderPreparation = prepareData(camera);

        drawData(buffer, mc, pose, camera, renderPreparation);

        RenderSystem.enableDepthTest();

    }

    private static RenderPreparation prepareData(Camera camera) {

        RenderPreparation renderPreparation = new RenderPreparation();

        loadForceGroupConfig(renderPreparation);


        VectorMode displayVectors = VECTOR_MODE.get();
        switch (displayVectors) {
            case SEPARATED -> prepareSeparatedVectors(renderPreparation);
            case MERGED -> prepareMergedVectors(renderPreparation);
            case DISABLED -> {}
        }

        VectorInfoMode vMode = DISPLAY_VECTORS_INFO.get();
        switch (vMode) {
            case ALWAYS -> prepareAllVectorInfo(renderPreparation, camera);
            case ON_LOOK -> prepareLookingVectorInfo(renderPreparation, camera);
            case DISABLED -> {}
        }

        CenterOfMassMode cMode = DISPLAY_CENTER_OF_MASS.get();
        switch (cMode){
            case ICON -> prepareIcon(renderPreparation, camera);
            case DETAILS -> prepareMassDetails(renderPreparation, camera);
            case ICON_DETAILS_ONLOOK -> prepareLookingMassInfo(renderPreparation, camera);
            case DISABLED -> {}
        }


        return renderPreparation;
    }

    /**
     * Loads the force groups from the diagram cache and associates each forceGroup
     * with its display configuration.
     */
    private static void loadForceGroupConfig(
        RenderPreparation renderPreparation
    ) {
        for (DiagramDataCache cached : diagramDataCache.values()) {

            List<RenderPreparation.PreparedGroup> preparedGroups =
                new ArrayList<>();

            for (ResolvedForceGroup forceGroup : cached.groups()) {

                ForceGroupDisplayConfig config =
                    ClientConfig.getForceGroupConfig(
                        ConfigUtils.getForceGroupId(forceGroup)
                    );

                preparedGroups.add(
                    new RenderPreparation.PreparedGroup(
                        forceGroup,
                        config
                    )
                );
            }

            renderPreparation.diagrams.add(
                new RenderPreparation.PreparedDiagram(
                    cached.mass(),
                    cached.centerOfMass(),
                    preparedGroups
                )
            );
        }
    }

    private static void prepareMergedVectors(RenderPreparation renderPreparation) {

        final double DIRECTION_THRESHOLD = 0.999;

        for (PreparedDiagram diagram : renderPreparation.diagrams) {
            for (PreparedGroup preparedGroup : diagram.groups()) {

                int color = 0xFF000000 | preparedGroup.forceGroup().color();
                List<ResolvedForce> forces = preparedGroup.forceGroup().forces();

                Set<Integer> mergedIndices = new HashSet<>();

                for (int i = 0; i < forces.size(); i++) {

                    if (mergedIndices.contains(i)) {
                        continue;
                    }

                    ResolvedForce force = forces.get(i);
                    Vec3 direction = force.delta().normalize();

                    Vec3 positionSum = force.origin();
                    Vec3 deltaSum = force.delta();

                    int count = 1;
                    mergedIndices.add(i);

                    for (int j = i + 1; j < forces.size(); j++) {

                        if (mergedIndices.contains(j)) {
                            continue;
                        }

                        ResolvedForce other = forces.get(j);
                        Vec3 otherDirection = other.delta().normalize();

                        if (direction.dot(otherDirection)
                            >= DIRECTION_THRESHOLD) {

                            positionSum = positionSum.add(other.origin());
                            deltaSum = deltaSum.add(other.delta());

                            count++;
                            mergedIndices.add(j);
                        }
                    }

                    Vec3 averagePosition =
                        positionSum.scale(1.0 / count);

                    ResolvedForce mergedForce =
                        new ResolvedForce(
                            averagePosition,
                            deltaSum
                        );

                    renderPreparation.vectors.add(
                        new PreparedForce(
                            mergedForce.origin(),
                            mergedForce.delta(),
                            color,
                            preparedGroup.forceGroup().name(),
                            preparedGroup.config()
                        )
                    );
                }
            }
        }
    }

    private static void prepareSeparatedVectors(RenderPreparation renderPreparation) {

        for (RenderPreparation.PreparedDiagram preparedDiagram : renderPreparation.diagrams) {

            for (PreparedGroup forceGroup : preparedDiagram.groups()) {

                int color = 0xFF000000 | forceGroup.forceGroup().color();
                for (var force: forceGroup.forceGroup().forces()) {
                    renderPreparation.vectors.add(new PreparedForce(
                        force.origin(),
                        force.delta(),
                        color,
                        forceGroup.forceGroup().name(),
                        forceGroup.config()
                    ));
                }

            }
        }
    }

    private static void prepareAllVectorInfo(RenderPreparation renderPreparation, Camera camera) {
        for (PreparedForce prepared : renderPreparation.vectors) {

            ForceGroupDisplayConfig config =
                prepared.config();

            if (!config.info()) {
                continue;
            }

            List<Component> components = createVectorInfoComponents(prepared);

            renderPreparation.information.add(new InformationDisplay(prepared.origin().add(getScaledDelta(prepared.delta()).scale(0.5)), components));



        }
    }

    private static @NotNull List<Component> createVectorInfoComponents(PreparedForce prepared) {
        String name = prepared.name().getString();
        String defaultText = " force of ";

        Component nameComponent = coloredText(name, 0xFF000000 | prepared.color());

        String value = String.format("%.2f pN", prepared.delta().length());
        Component valueComponent = coloredText(defaultText + " ", 0xF7F0DD)
            .append(coloredText(value, 0xFFFFFF));


        List<Component> components = new ArrayList<>();
        components.add(nameComponent);
        components.add(valueComponent);
        return components;
    }

    private static void prepareLookingVectorInfo(RenderPreparation renderPreparation, Camera camera) {
        LookingForce bestForce = null;

        for (PreparedForce prepared : renderPreparation.vectors) {



                if (!prepared.config().info()) {
                    continue;
                }



                GeometryUtils.ClosestPoints closestPoints =
                    getLookingPoints(
                        camera,
                        prepared.origin(),
                        prepared.delta()
                    );

                if (closestPoints == null) {
                    continue;
                }

                LookingForce candidate = new LookingForce(
                    prepared,
                    closestPoints.pointB(),
                    closestPoints.parameterA()
                );

                if (bestForce == null
                    || candidate.rayParameter()
                    < bestForce.rayParameter()) {

                    bestForce = candidate;
                }


        }

        if (bestForce == null) {
            return;
        }

        List<Component> components = createVectorInfoComponents(bestForce.vector());

        renderPreparation.information.add(new InformationDisplay(bestForce.position(), components));

    }

    private static void prepareIcon(RenderPreparation renderPreparation, Camera camera) {
        for (PreparedDiagram preparedDiagram : renderPreparation.diagrams) {
            renderPreparation.icons.add(new IconDisplay(preparedDiagram.centerOfMass()));
        }
    }

    private static void prepareMassDetail(RenderPreparation renderPreparation, PreparedDiagram preparedDiagram) {
        Component text = coloredText("Center of mass",0xFFAA7733);
        Component value = coloredText(String.format("%.2f Kpg", preparedDiagram.mass()),0xFFFFFFFF);
        List<Component> components = new ArrayList<>();
        components.add(text);
        components.add(value);
        renderPreparation.information.add(new InformationDisplay(preparedDiagram.centerOfMass(), components));
    }

    private static void prepareMassDetails(RenderPreparation renderPreparation, Camera camera) {
        for (PreparedDiagram preparedDiagram : renderPreparation.diagrams) {

            prepareMassDetail(renderPreparation, preparedDiagram);
        }
    }

    private static void prepareLookingMassInfo(RenderPreparation renderPreparation, Camera camera) {

        for (PreparedDiagram preparedDiagram : renderPreparation.diagrams) {
            Vec3 centerOfMass = preparedDiagram.centerOfMass();

            if(isPlayerLooking(camera, centerOfMass)) {
                prepareMassDetail(renderPreparation, preparedDiagram);

            }else {
                renderPreparation.icons.add(new IconDisplay(centerOfMass));
            }


        }

    }

    private static @Nullable GeometryUtils.ClosestPoints getLookingPoints(
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


    private static void drawData(MultiBufferSource.BufferSource buffer, Minecraft mc, PoseStack pose, Camera camera, RenderPreparation RenderPreparation) {
        for (IconDisplay iconDisplay : RenderPreparation.icons) {
            drawCenterOfMassIcon(buffer, pose, camera, iconDisplay.position());
        }

        for (PreparedForce vectorDisplay : RenderPreparation.vectors) {
            if(!vectorDisplay.config().vectors()) continue;
            drawVector(
                buffer,
                pose,
                camera,
                vectorDisplay
            );
        }

        for (InformationDisplay informationDisplay : RenderPreparation.information) {
            drawInfoDisplay(
                buffer,
                mc.font,
                pose,
                camera,
                informationDisplay.position(),
                informationDisplay.components().toArray(new Component[0])
            );
        }


    }


    /// It draws an arrow with VertexFormat.Mode.LINES
    private static void drawVector(
        MultiBufferSource.BufferSource buffer,
        PoseStack poseStack,
        Camera camera,
        PreparedForce vectorDisplay
    ) {

        Vec3 origin = vectorDisplay.origin();
        Vec3 delta = vectorDisplay.delta();

        Vec3 cameraPosition = camera.getPosition();
        Vec3 cameraDirection = new Vec3(camera.getLookVector());

        poseStack.pushPose();

        poseStack.translate(
            -cameraPosition.x,
            -cameraPosition.y,
            -cameraPosition.z
        );

        Vec3 end = origin.add(getScaledDelta(delta));

        if (VECTOR_STYLE.get() == VectorStyle.SLIM) {
            ArrowStyles.drawSlimArrow(
                poseStack,
                buffer,
                origin,
                end,
                cameraDirection,
                vectorDisplay.color()
            );
        }else{
            ArrowStyles.drawTexturedArrow(
                poseStack,
                buffer,
                origin,
                end,
                cameraDirection,
                vectorDisplay.color()
            );
        }









        poseStack.popPose();
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
        VertexConsumer background;
        if(INFO_BACKGROUND_USE_TEXTURE.get())
        {
            background = buffer.getBuffer(RenderTypes.texturedBackground(INFO_BACKGROUND_TEXTURE));
            RenderUtils.drawTexturedCenteredQuad(
                background,
                matrix,
                width,
                height
                );
        }

        else{
            background = buffer.getBuffer(RenderTypes.BACKGROUND_QUADS);
            RenderUtils.drawUniformlyColoredCenteredQuad(
                background,
                matrix,
                width,
                height,
                ConfigUtils.getColor(INFO_BACKGROUND_COLOR),
                LightTexture.FULL_BRIGHT
            );
        }



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
        final float quarterSize = size * 0.25F;
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
        RenderUtils.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            0F,
            0F,
            size + outline,
            size + outline,
            white,
            LightTexture.FULL_BRIGHT
        );
        // top-left
        RenderUtils.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            -quarterSize,
            quarterSize,
            halfSize,
            halfSize,
            dark,
            LightTexture.FULL_BRIGHT
        );

        // top-right
        RenderUtils.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            quarterSize,
            quarterSize,
            halfSize,
            halfSize,
            light,
            LightTexture.FULL_BRIGHT
        );

        // bottom-left
        RenderUtils.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            -quarterSize,
            -quarterSize,
            halfSize,
            halfSize,
            light,
            LightTexture.FULL_BRIGHT
        );

        // bottom-right
        RenderUtils.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            quarterSize,
            -quarterSize,
            halfSize,
            halfSize,
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
