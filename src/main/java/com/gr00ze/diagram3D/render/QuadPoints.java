package com.gr00ze.diagram3D.render;

import org.joml.Vector3f;


public class QuadPoints {
    private static final int POINT_COUNT = 4;
    private final Vector3f[] points = new Vector3f[POINT_COUNT];
    private int count = 0;

    private QuadPoints() {}

    public static QuadPoints create() { return new QuadPoints(); }



    public QuadPoints add(float x, float y, float z) {
        if (count >= POINT_COUNT) {
            throw new IllegalStateException("A quad can contain only " + POINT_COUNT + " points.");
        }
        points[count++] = new Vector3f(x, y, z);
        return this;
    }

    public QuadPoints add(Vector3f point) {
        return add(point.x, point.y, point.z);
    }

    public boolean isComplete() { return count == POINT_COUNT; }

    public Vector3f[] getPoints() {
        if(!isComplete()) {
            throw new IllegalStateException("A quad can contain only " + POINT_COUNT + " points.");
        }
        return points;
    }

    /** Create a centered quad
     * Order:
     * 0 -------- 1
     * |          |
     * |          |
     * 3 -------- 2
     */
    public static QuadPoints centeredXY(float halfWidth, float halfHeight) {
        return QuadPoints.create()
            .add(-halfWidth, halfHeight, 0)
            .add( halfWidth, halfHeight, 0)
            .add( halfWidth, -halfHeight, 0)
            .add(-halfWidth, -halfHeight, 0);
    }

    public static QuadPoints centeredXY(
        float centerX,
        float centerY,
        float halfWidth,
        float halfHeight
    ) {
        return QuadPoints.create()
            .add(centerX - halfWidth, centerY + halfHeight, 0)
            .add(centerX + halfWidth, centerY + halfHeight, 0)
            .add(centerX + halfWidth, centerY - halfHeight, 0)
            .add(centerX - halfWidth, centerY - halfHeight, 0);
    }

    /// Shortcut to create a complete Quad
    public static QuadPoints of(Vector3f point1, Vector3f point2, Vector3f point3, Vector3f point4) {
        return QuadPoints.create()
            .add(point1)
            .add(point2)
            .add(point3)
            .add(point4);
    }




}
