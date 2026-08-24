package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.data.DiagramRecords;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record ConvertedDiagramData (
    List<DiagramRecords.InWorldForceGroup> groups,
    Vec3 massPosition,
    double massValue
){
}
