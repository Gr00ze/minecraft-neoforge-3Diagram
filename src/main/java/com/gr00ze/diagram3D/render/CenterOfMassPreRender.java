package com.gr00ze.diagram3D.render;

import net.minecraft.client.Camera;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static com.gr00ze.diagram3D.render.RenderUtils.coloredText;

public class CenterOfMassPreRender {

    public static void prepareIcon(RenderPreparation renderPreparation) {
        for (RenderPreparation.PreparedDiagram preparedDiagram : renderPreparation.diagrams) {
            renderPreparation.icons.add(new RenderPreparation.IconDisplay(preparedDiagram.centerOfMass()));
        }
    }

    public static void prepareMassDetail(RenderPreparation renderPreparation, RenderPreparation.PreparedDiagram preparedDiagram) {
        Component text = coloredText("Center of mass",0xFFAA7733);
        Component value = coloredText(String.format("%.2f Kpg", preparedDiagram.mass()),0xFFFFFFFF);
        List<Component> components = new ArrayList<>();
        components.add(text);
        components.add(value);
        renderPreparation.information.add(new RenderPreparation.InformationDisplay(preparedDiagram.centerOfMass(), components));
    }

    public static void prepareMassDetails(RenderPreparation renderPreparation) {
        for (RenderPreparation.PreparedDiagram preparedDiagram : renderPreparation.diagrams) {

            prepareMassDetail(renderPreparation, preparedDiagram);
        }
    }

    public static void prepareLookingMassInfo(RenderPreparation renderPreparation, Camera camera) {

        for (RenderPreparation.PreparedDiagram preparedDiagram : renderPreparation.diagrams) {
            Vec3 centerOfMass = preparedDiagram.centerOfMass();

            if(RenderUtils.isPlayerLooking(camera, centerOfMass)) {
                prepareMassDetail(renderPreparation, preparedDiagram);

            }else {
                renderPreparation.icons.add(new RenderPreparation.IconDisplay(centerOfMass));
            }
        }
    }


}
