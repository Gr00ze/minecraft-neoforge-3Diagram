package com.gr00ze.diagram3D;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Diagram3D.MOD_ID)
public class ClientConfig
{

    public static final int UPDATE_REQUEST_TICK_INTERVAL = 1;
    public static final long TIMEOUT_RENDER_TICKS = 20;
    public static final double FORCE_VISUAL_SCALE = 0.05;

    private static final double DEFAULT_RADIUS = 20;
    private static final boolean DEFAULT_ENABLED = true;
    private static final boolean DEFAULT_DISPLAY_INFO_TAG = true;
    private static final boolean DEFAULT_DISPLAY_CENTER_OF_MASS = true;
    private static final boolean DEFAULT_DISPLAY_VECTORS = true;

    public static double RADIUS = DEFAULT_RADIUS;
    public static boolean ENABLED = DEFAULT_ENABLED;
    public static boolean DISPLAY_INFO_TAG = DEFAULT_DISPLAY_INFO_TAG;
    public static boolean DISPLAY_CENTER_OF_MASS = DEFAULT_DISPLAY_CENTER_OF_MASS;
    public static boolean DISPLAY_VECTORS = DEFAULT_DISPLAY_VECTORS;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue RADIUS_CONFIG = BUILDER.defineInRange("radius", DEFAULT_RADIUS, 0, 128);
    private static final ModConfigSpec.BooleanValue ENABLED_CONFIG = BUILDER.define("enabled", DEFAULT_ENABLED);
    private static final ModConfigSpec.BooleanValue DISPLAY_INFO_TAG_CONFIG = BUILDER.define("display_info_tag", DEFAULT_DISPLAY_INFO_TAG);
    private static final ModConfigSpec.BooleanValue DISPLAY_CENTER_OF_MASS_CONFIG = BUILDER.define("display_center_of_mass", DEFAULT_DISPLAY_CENTER_OF_MASS);
    private static final ModConfigSpec.BooleanValue DISPLAY_VECTORS_CONFIG = BUILDER.define("display_vectors", DEFAULT_DISPLAY_VECTORS);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() == SPEC) {
            RADIUS = RADIUS_CONFIG.get();
            ENABLED = ENABLED_CONFIG.getAsBoolean();
            DISPLAY_INFO_TAG = DISPLAY_INFO_TAG_CONFIG.getAsBoolean();
            DISPLAY_CENTER_OF_MASS = DISPLAY_CENTER_OF_MASS_CONFIG.getAsBoolean();
            DISPLAY_VECTORS = DISPLAY_VECTORS_CONFIG.getAsBoolean();
        }
    }

}
