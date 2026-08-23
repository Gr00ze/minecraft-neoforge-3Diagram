package com.gr00ze.diagram3D;

import net.minecraft.world.phys.Vec3;

import static com.gr00ze.diagram3D.config.ClientConfig.*;

public class Utils {


    public static Vec3 getScaledDelta(Vec3 original) {
        double x = original.length();

        if (x == 0.0) {
            return Vec3.ZERO;
        }

        double targetLength =
            x * x * QUADRATIC_VECTOR_SCALING_FACTOR.get()
                + x * PROPORTIONAL_VECTOR_SCALING_FACTOR.get();

        return original.normalize().scale(targetLength);
    }


}
