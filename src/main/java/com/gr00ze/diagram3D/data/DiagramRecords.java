package com.gr00ze.diagram3D.data;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DiagramRecords {

    public record DiagramDataCache(
        List<ResolvedForceGroup> groups,
        long lastUpdate
    ) {}


    public record ResolvedForceGroup(
        Component name,
        @Nullable Component description,
        int color,
        boolean defaultDisplayed,
        List<ResolvedForce> forces
    ) {}

    public record ResolvedForce(
        Vec3 from,
        Vec3 to
    ) {}
}
