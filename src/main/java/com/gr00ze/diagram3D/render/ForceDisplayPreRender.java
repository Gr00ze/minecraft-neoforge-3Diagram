package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.config.ForceGroupDisplayConfig;
import com.gr00ze.libs.GeometryUtils;
import net.minecraft.client.Camera;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.gr00ze.diagram3D.Utils.getScaledDelta;
import static com.gr00ze.diagram3D.render.RenderUtils.coloredText;

public class ForceDisplayPreRender {

    public static void prepareAllVectorInfo(RenderPreparation renderPreparation) {
        for (RenderPreparation.PreparedForce prepared : renderPreparation.vectors) {

            ForceGroupDisplayConfig config =
                prepared.config();

            if (!config.info()) {
                continue;
            }

            List<Component> components = createVectorInfoComponents(prepared);

            renderPreparation.information.add(new RenderPreparation.InformationDisplay(prepared.origin().add(getScaledDelta(prepared.delta()).scale(0.5)), components));

        }
    }

    private static @NotNull List<Component> createVectorInfoComponents(RenderPreparation.PreparedForce prepared) {
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

    public static void prepareLookingVectorInfo(RenderPreparation renderPreparation, Camera camera) {
        RenderPreparation.LookingForce bestForce = null;

        for (RenderPreparation.PreparedForce prepared : renderPreparation.vectors) {

            if (!prepared.config().info()) {
                continue;
            }



            GeometryUtils.ClosestPoints closestPoints =
                RenderUtils.getLookingPoints(
                    camera,
                    prepared.origin(),
                    prepared.delta()
                );

            if (closestPoints == null) {
                continue;
            }

            RenderPreparation.LookingForce candidate = new RenderPreparation.LookingForce(
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

        renderPreparation.information.add(new RenderPreparation.InformationDisplay(bestForce.position(), components));

    }
}
