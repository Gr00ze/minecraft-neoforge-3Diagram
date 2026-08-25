package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.config.ClientConfig;
import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.config.ConfigUtils;
import com.gr00ze.diagram3D.config.ForceGroupDisplayConfig;
import com.gr00ze.diagram3D.data.DiagramDataResolver;
import com.gr00ze.diagram3D.data.DiagramRecords.*;
import com.gr00ze.diagram3D.render.RenderPreparation.*;
import com.gr00ze.libs.RenderFunctions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import static com.gr00ze.diagram3D.config.ClientConfig.*;
import static com.gr00ze.diagram3D.data.DiagramDataManager.diagramDataCache;
import static com.gr00ze.diagram3D.Utils.getScaledDelta;


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

        ClientLevel level = mc.level;
        Player player = mc.player;
        if(player == null || level == null)
            return;

        PoseStack pose = event.getPoseStack();
        Camera camera = event.getCamera();

        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        RenderSystem.disableDepthTest();

        RenderPreparation renderPreparation = prepareData(level, camera);

        drawData(buffer, mc, pose, camera, renderPreparation);

        RenderSystem.enableDepthTest();

    }

    private static RenderPreparation prepareData(ClientLevel level, Camera camera) {


        List<ConvertedDiagramData> convertedDiagramDataList = new ArrayList<>();
        prepareDiagramData(level, convertedDiagramDataList);

        RenderPreparation renderPreparation = new RenderPreparation();
        loadForceGroupConfig(convertedDiagramDataList, renderPreparation);


        VectorMode displayVectors = VECTOR_MODE.get();
        switch (displayVectors) {
            case SEPARATED -> ForceVectorPreRender.prepareSeparatedVectors(renderPreparation);
            case MERGED -> ForceVectorPreRender.prepareMergedVectors(renderPreparation);
            case DISABLED -> {}
        }

        VectorInfoMode vMode = DISPLAY_VECTORS_INFO.get();
        switch (vMode) {
            case ALWAYS -> ForceDisplayPreRender.prepareAllVectorInfo(renderPreparation);
            case ON_LOOK -> ForceDisplayPreRender.prepareLookingVectorInfo(renderPreparation, camera);
            case DISABLED -> {}
        }

        CenterOfMassMode cMode = DISPLAY_CENTER_OF_MASS.get();
        switch (cMode){
            case ICON -> CenterOfMassPreRender.prepareIcon(renderPreparation);
            case DETAILS -> CenterOfMassPreRender.prepareMassDetails(renderPreparation);
            case ICON_DETAILS_ONLOOK -> CenterOfMassPreRender.prepareLookingMassInfo(renderPreparation, camera);
            case DISABLED -> {}
        }


        return renderPreparation;
    }

    private static void prepareDiagramData(ClientLevel level, List<ConvertedDiagramData> convertedDiagramDataList) {
        var container = SubLevelContainer.getContainer(level);
        if (container == null) return;

        for (DiagramDataCache cached : diagramDataCache.values()) {
            DiagramDataPacket cachedPacked = cached.serverData();
            UUID sublevelID = cached.completed();
            long lastUpdate = cached.lastUpdate();


            SubLevel sublevel  = container.getSubLevel(sublevelID);
            if(sublevel == null) continue;

            ConvertedDiagramData convertedDiagramData =
                new ConvertedDiagramData(
                    DiagramDataResolver
                        .convertDiagramData(
                            sublevelID,
                            cachedPacked
                        ),
                    DiagramDataResolver
                        .getInWorldMassPosition(sublevelID),
                    cachedPacked.mass()
                );

            convertedDiagramDataList.add(convertedDiagramData);




        }
        //diagramDataCache.clear();
    }

    /**
     * Loads the force groups from the diagram cache and associates each forceGroup
     * with its display configuration.
     */
    private static void loadForceGroupConfig(List<ConvertedDiagramData> convertedDiagramDataList, RenderPreparation renderPreparation
    ) {
        for (ConvertedDiagramData data : convertedDiagramDataList) {

            List<RenderPreparation.PreparedGroup> preparedGroups =
                new ArrayList<>();

            for (InWorldForceGroup forceGroup : data.groups()) {

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
                    data.massValue(),
                    data.massPosition(),
                    preparedGroups
                )
            );
        }
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
        RenderUtils.applyTransformations(pose, camera, position, cameraPosition);
        Matrix4f matrix = pose.last().pose();
        RenderUtils.drawTextBackground(buffer, matrix, maxWidth + 4, backgroundHeight);

        float rowCount = 0;
        for (Component component : components){
            RenderUtils.drawText(
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

    public static void drawCenterOfMassIcon(
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

        RenderUtils.applyTransformations(
            pose,
            camera,
            centerOfMass,
            cameraPosition
        );

        Matrix4f matrix = pose.last().pose();

        // outline/background
        RenderFunctions.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            0F,
            0F,
            size + outline,
            size + outline,
            white);
        // top-left
        RenderFunctions.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            -quarterSize,
            quarterSize,
            halfSize,
            halfSize,
            dark);

        // top-right
        RenderFunctions.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            quarterSize,
            quarterSize,
            halfSize,
            halfSize,
            light);

        // bottom-left
        RenderFunctions.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            -quarterSize,
            -quarterSize,
            halfSize,
            halfSize,
            light);

        // bottom-right
        RenderFunctions.drawUniformlyColoredCenteredQuad(
            background,
            matrix,
            quarterSize,
            -quarterSize,
            halfSize,
            halfSize,
            dark);

        pose.popPose();
    }










}
