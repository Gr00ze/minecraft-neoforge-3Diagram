package com.gr00ze.diagram3D.render;

import com.gr00ze.diagram3D.data.DiagramRecords;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForceVectorPreRender {

    public static void prepareMergedVectors(RenderPreparation renderPreparation) {

        final double DIRECTION_THRESHOLD = 0.999;

        for (RenderPreparation.PreparedDiagram diagram : renderPreparation.diagrams) {
            for (RenderPreparation.PreparedGroup preparedGroup : diagram.groups()) {
                preparedMergedVectors(renderPreparation, preparedGroup, DIRECTION_THRESHOLD);
            }
        }
    }

    private static void preparedMergedVectors(RenderPreparation renderPreparation, RenderPreparation.PreparedGroup preparedGroup, double directionThreshold) {
        int color = 0xFF000000 | preparedGroup.forceGroup().color();
        List<DiagramRecords.InWorldForce> forces = preparedGroup.forceGroup().forces();

        Set<Integer> mergedIndices = new HashSet<>();

        for (int i = 0; i < forces.size(); i++) {

            if (mergedIndices.contains(i)) {
                continue;
            }

            MergedForceData mergedForceData = mergeForces(directionThreshold, forces, i, mergedIndices);

            Vec3 averagePosition =
                mergedForceData.positionSum()
                    .scale(1.0 / mergedForceData.count());

            DiagramRecords.InWorldForce mergedForce =
                new DiagramRecords.InWorldForce(
                    averagePosition, mergedForceData.deltaSum()
                );

            renderPreparation.vectors.add(
                new RenderPreparation.PreparedForce(
                    mergedForce.origin(),
                    mergedForce.delta(),
                    color,
                    preparedGroup.forceGroup().name(),
                    preparedGroup.config()
                )
            );
        }
    }

    /**
     * Merges forces with similar directions starting from the given index.
     * Already merged forces are skipped to ensure each force belongs to only one group.
     */
    private static MergedForceData mergeForces(double directionThreshold, List<DiagramRecords.InWorldForce> forces, int i, Set<Integer> mergedIndices) {
        DiagramRecords.InWorldForce force = forces.get(i);
        Vec3 direction = force.delta().normalize();

        Vec3 positionSum = force.origin();
        Vec3 deltaSum = force.delta();

        int count = 1;
        mergedIndices.add(i);

        for (int j = i + 1; j < forces.size(); j++) {

            if (mergedIndices.contains(j)) {
                continue;
            }

            DiagramRecords.InWorldForce other = forces.get(j);
            Vec3 otherDirection = other.delta().normalize();

            if (direction.dot(otherDirection)
                >= directionThreshold) {

                positionSum = positionSum.add(other.origin());
                deltaSum = deltaSum.add(other.delta());

                count++;
                mergedIndices.add(j);
            }
        }
        return new MergedForceData(positionSum, deltaSum, count);
    }

    private record MergedForceData(Vec3 positionSum, Vec3 deltaSum, int count) {
    }

    public static void prepareSeparatedVectors(RenderPreparation renderPreparation) {

        for (RenderPreparation.PreparedDiagram preparedDiagram : renderPreparation.diagrams) {

            for (RenderPreparation.PreparedGroup forceGroup : preparedDiagram.groups()) {

                int color = 0xFF000000 | forceGroup.forceGroup().color();
                for (var force: forceGroup.forceGroup().forces()) {
                    renderPreparation.vectors.add(new RenderPreparation.PreparedForce(
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

}
