package com.gr00ze.diagram3D.data;

import com.gr00ze.diagram3D.Diagram3D;
import dev.ryanhcode.sable.api.physics.force.ForceGroup;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DiagramDataResolver {

    public static List<DiagramRecords.ResolvedForceGroup> convertDiagramData(
        UUID subLevelId,
        DiagramDataPacket packet
    )
    {
        List<DiagramRecords.ResolvedForceGroup> result = new ArrayList<>();

        for (Map.Entry<ForceGroup, List<QueuedForceGroup.PointForce>> entry
            : packet.forces().entrySet())
        {
            ForceGroup group = entry.getKey();

            List<DiagramRecords.ResolvedForce> renderForces = new ArrayList<>();

            for (QueuedForceGroup.PointForce vector : entry.getValue())
            {
                renderForces.add(
                    convertSubLevelToLevel(
                        subLevelId,
                        vector.point(),
                        vector.force()
                    )

                );
            }

            result.add(
                new DiagramRecords.ResolvedForceGroup(
                    group.name(),
                    group.description(),
                    group.color(),
                    group.defaultDisplayed(),
                    renderForces
                )
            );
        }

        return result;
    }
    public static DiagramRecords.ResolvedForce convertSubLevelToLevel(UUID subLevelId, Vector3dc localFrom, Vector3dc localTo)
    {
        Minecraft mc = Minecraft.getInstance();

        Level level = mc.level;
        if (level == null)
            return new DiagramRecords.ResolvedForce(
                new Vec3(localFrom.x(), localFrom.y(), localFrom.z()),
                new Vec3(localTo.x(), localTo.y(), localTo.z())
            ); //Return the original

        SubLevelContainer container = SubLevelContainer.getContainer(level);

        if (container == null)
            return new DiagramRecords.ResolvedForce(
                new Vec3(localFrom.x(), localFrom.y(), localFrom.z()),
                new Vec3(localTo.x(), localTo.y(), localTo.z())
            );  //Return the original

        SubLevel subLevel = container.getSubLevel(subLevelId);

        if (subLevel == null)
            return new DiagramRecords.ResolvedForce(
                new Vec3(localFrom.x(), localFrom.y(), localFrom.z()),
                new Vec3(localTo.x(), localTo.y(), localTo.z())
            );;  //Return the original


        Pose3dc pose = subLevel.logicalPose(); //try render pose
        Vector3d from = pose.transformPosition(localFrom, new Vector3d());
        Vector3d to = pose.transformNormal(localTo, new Vector3d());


        return new DiagramRecords.ResolvedForce(new Vec3(from.x, from.y, from.z),new Vec3(to.x, to.y, to.z));
    }

    public static Vec3 getCenterOfMass(Level level, UUID completed) {
        SubLevelContainer container = SubLevelContainer.getContainer(level);

        if (container == null) return null;

        SubLevel subLevel = container.getSubLevel(completed);

        if (subLevel == null) return null;

        Vector3d rotationPointLocal = subLevel.logicalPose().rotationPoint();


        Pose3dc pose = subLevel.logicalPose(); //try render pose
        Vector3d rotationGlobal = pose.transformPosition(rotationPointLocal, new Vector3d());


        return new Vec3(
            rotationGlobal.x(),
            rotationGlobal.y(),
            rotationGlobal.z()
        );

    }
}
