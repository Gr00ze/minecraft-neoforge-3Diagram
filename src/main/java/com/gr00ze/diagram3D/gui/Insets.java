package com.gr00ze.diagram3D.gui;

public record Insets(float top, float bottom, float left, float right) {
    public Insets(float all) {
        this(all, all, all, all);
    }

    public Insets(float vertical, float horizontal) {
        this(vertical, vertical, horizontal, horizontal);
    }
}
