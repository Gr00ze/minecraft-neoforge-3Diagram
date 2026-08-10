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

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue RADIUS = BUILDER.defineInRange("radius", 20F, 0, 128);
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER.define("enabled", true);
    public static final ModConfigSpec.BooleanValue DISPLAY_INFO_TAG = BUILDER.define("display_info_tag", true);
    public static final ModConfigSpec.BooleanValue DISPLAY_CENTER_OF_MASS = BUILDER.define("display_center_of_mass", true);
    public static final ModConfigSpec.BooleanValue DISPLAY_VECTORS = BUILDER.define("display_vectors", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
    }

    public static void toggle(ModConfigSpec.BooleanValue config){
        config.set(!config.get());
    }

}
