package com.gr00ze.libs;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Optional;

public class GeometryUtils {


    private GeometryUtils() {
        // Utility class
    }

    /**
     * Checks whether two 3D line segments come within a given distance
     * of each other.
     *
     * <pre>
     * Segment A:
     *
     *     startA ●────────────────────● endA
     *                  vectorA
     *
     * Segment B:
     *
     *     startB ●────────────────────● endB
     *                  vectorB
     *
     * The method finds the two closest points on the segments:
     *
     *     startA ●────────────●────────● endA
     *                          ↑
     *                   closestPointA
     *
     *     startB ●────────●────────────● endB
     *                    ↑
     *             closestPointB
     *
     * If the distance between the two closest points is less than or
     * equal to {@code threshold}, the segments are considered to
     * intersect within the specified tolerance.
     *
     * @return {@code true} if the minimum distance between the segments
     *         is less than or equal to {@code threshold}
     */
    public static boolean segmentsIntersectWithin(
        Vec3 startA,
        Vec3 endA,
        Vec3 startB,
        Vec3 endB,
        double threshold,
        double epsilon
    ) {
        return closestPointsWithin(
            startA,
            endA,
            startB,
            endB,
            threshold,
            epsilon
        ).isPresent();
    }

    /**
     * Finds the closest points between two 3D line segments.
     *
     * <pre>
     *     Segment A
     *
     *     A ●──────────────────●
     *                    ↑
     *              closestPointA
     *
     *     B ●────────●───────────
     *              ↑
     *        closestPointB
     *
     * </pre>
     *
     * The result is present only when the distance between the two
     * closest points is less than or equal to {@code threshold}.
     *
     * @return the pair of closest points if they are within
     *         {@code threshold}, otherwise an empty Optional
     */
    public static Optional<ClosestPoints> closestPointsWithin(
        Vec3 startA,
        Vec3 endA,
        Vec3 startB,
        Vec3 endB,
        double threshold,
        double epsilon
    ) {
        Vec3 vectorA = endA.subtract(startA);
        Vec3 vectorB = endB.subtract(startB);
        Vec3 startBToStartA = startA.subtract(startB);

        double lengthASquared = vectorA.lengthSqr();
        double lengthBSquared = vectorB.lengthSqr();

        double projectionB = vectorB.dot(startBToStartA);

        double parameterA;
        double parameterB;

        if (lengthASquared <= epsilon && lengthBSquared <= epsilon) {
            if (startA.distanceToSqr(startB) > threshold * threshold) {
                return Optional.empty();
            }

            return Optional.of(new ClosestPoints(startA, startB, 0.0, 0.0));
        }

        if (lengthBSquared <= epsilon) {
            parameterB = 0.0;
            parameterA = Math.clamp(
                -startBToStartA.dot(vectorA) / lengthASquared,
                0.0,
                1.0
            );
        } else {
            double projectionA = vectorA.dot(startBToStartA);

            if (lengthASquared <= epsilon) {
                parameterA = 0.0;
                parameterB = Math.clamp(
                    projectionB / lengthBSquared,
                    0.0,
                    1.0
                );
            } else {
                double directionProjection = vectorA.dot(vectorB);

                double denominator =
                    lengthASquared * lengthBSquared
                        - directionProjection * directionProjection;

                if (Math.abs(denominator) > epsilon) {
                    parameterA = Math.clamp(
                        (directionProjection * projectionB
                            - projectionA * lengthBSquared)
                            / denominator,
                        0.0,
                        1.0
                    );
                } else {
                    parameterA = 0.0;
                }

                parameterB = (
                    directionProjection * parameterA
                        + projectionB
                ) / lengthBSquared;

                if (parameterB < 0.0) {
                    parameterB = 0.0;
                    parameterA = Math.clamp(
                        -projectionA / lengthASquared,
                        0.0,
                        1.0
                    );
                } else if (parameterB > 1.0) {
                    parameterB = 1.0;
                    parameterA = Math.clamp(
                        (directionProjection - projectionA)
                            / lengthASquared,
                        0.0,
                        1.0
                    );
                }
            }
        }

        Vec3 closestPointA = startA.add(vectorA.scale(parameterA));
        Vec3 closestPointB = startB.add(vectorB.scale(parameterB));

        if (closestPointA.distanceToSqr(closestPointB)
            > threshold * threshold) {
            return Optional.empty();
        }

        return Optional.of(
            new ClosestPoints(
                closestPointA,
                closestPointB,
                parameterA,
                parameterB
            )
        );
    }

    /**
     * Represents the closest points found on two line segments.
     *
     * @param pointA closest point on the first segment
     * @param pointB closest point on the second segment
     */
    public record ClosestPoints(
        Vec3 pointA,
        Vec3 pointB,
        double parameterA,
        double parameterB
    ) {
    }


}


