package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.config.ForceGroupDisplayConfig;
import com.gr00ze.diagram3D.data.DiagramRecords;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RenderPreparation {
    public record InformationDisplay(
        Vec3 position,
        List<Component> components
    ) {}

    public record PreparedForce(
        Vec3 origin,
        Vec3 delta,
        int color,
        Component name,
        ForceGroupDisplayConfig config
    ) {}
    public record IconDisplay(
        Vec3 position
    ) {}

    public record PreparedGroup(
        DiagramRecords.InWorldForceGroup forceGroup,
        ForceGroupDisplayConfig config
    ) {}

    public record PreparedDiagram(
        double mass,
        Vec3 centerOfMass,
        List<PreparedGroup> groups
    ) {}

    public record LookingForce(
        PreparedForce vector,
        Vec3 position,
        double rayParameter
    ) {}

    public final List<PreparedForce> vectors = new ArrayList<>();
    public final List<InformationDisplay> information = new ArrayList<>();
    public final List<IconDisplay> icons = new ArrayList<>();
    public final List<PreparedDiagram> diagrams = new ArrayList<>();
}
