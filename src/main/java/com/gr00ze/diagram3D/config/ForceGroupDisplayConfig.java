package com.gr00ze.diagram3D.config;

public class ForceGroupDisplayConfig {

    private boolean vectors;
    private boolean info;

    public ForceGroupDisplayConfig(boolean vectors, boolean info) {
        this.vectors = vectors;
        this.info = info;
    }

    public boolean vectors() {
        return vectors;
    }

    public boolean info() {
        return info;
    }

    public void toggleVectors() {
        vectors = !vectors;
    }

    public void toggleInfo() {
        info = !info;
    }
}
