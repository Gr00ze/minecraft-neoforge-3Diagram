package com.gr00ze.libs;

import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class ConversionUtils {
    public static Vec3 toVec3(Vector3dc vector) {
        return new Vec3(vector.x(), vector.y(), vector.z());
    }

    public record OriginVector(Vec3 origin, Vec3 delta) {}

    public static OriginVector convertSubLevelToLevel(
        @NotNull ClientSubLevel subLevel,
        @NotNull Vector3dc localOrigin,
        @NotNull Vector3dc localDelta
    ){
        Pose3dc pose = subLevel.renderPose();
        Vector3d origin = pose.transformPosition(localOrigin, new Vector3d());
        Vector3d delta = pose.transformNormal(localDelta, new Vector3d());

        return new OriginVector(
            toVec3(origin),
            toVec3(delta));
    }

    public static Vector3dc convertSubLevelToLevel(
        @NotNull ClientSubLevel subLevel,
        @NotNull Vector3dc localPoint
    ){
        Pose3dc pose = subLevel.renderPose();
        return pose.transformPosition(localPoint, new Vector3d());
    }

    public static Vec3 convertSubLevelToLevelVec3(
        @NotNull ClientSubLevel subLevel,
        @NotNull Vector3dc localPoint
    ){
        return toVec3(convertSubLevelToLevel(subLevel, localPoint));
    }
}
