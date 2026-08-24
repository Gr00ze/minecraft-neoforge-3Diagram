package com.gr00ze.diagram3D.data;

import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DiagramRecords {

    public record DiagramDataCache(
        UUID completed,
        DiagramDataPacket serverData,
        long lastUpdate
    ) {}


    public record InWorldForceGroup(
        Component name,
        @Nullable Component description,
        int color,
        boolean defaultDisplayed,
        List<InWorldForce> forces
    ) {}

    public record InWorldForce(
        Vec3 origin,
        Vec3 delta
    ) {}
}
