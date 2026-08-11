package com.gr00ze.diagram3D.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.gr00ze.diagram3D.Diagram3D;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

@EventBusSubscriber(modid = Diagram3D.MOD_ID)
public class ClientConfig
{

    public static final int UPDATE_REQUEST_TICK_INTERVAL = 1;
    public static final long TIMEOUT_RENDER_TICKS = 20;
    public static final double FORCE_VISUAL_SCALE = 0.05;

    public enum CenterOfMassMode {
        DISABLED,
        ICON,
        DETAILS
    }

    static final Map<ResourceLocation, ForceGroupDisplayConfig>
        FORCE_GROUP_CONFIGS = new HashMap<>();


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue RADIUS = BUILDER
        .comment("The scan radius for which the player will ask information for sublevels to the server ")
        .defineInRange("radius", 20F, 0, 128);
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
        .comment("Enable the entire mod")
        .define("enabled", true);
    public static final ModConfigSpec.BooleanValue DISPLAY_VECTORS;
    public static final ModConfigSpec.BooleanValue DISPLAY_VECTORS_INFO;
    public static final ModConfigSpec.EnumValue<CenterOfMassMode> DISPLAY_CENTER_OF_MASS;
    public static final ModConfigSpec.ConfigValue<Config> FORCE_GROUP_CONFIGS_VALUE;

    static {
        BUILDER.push("Rendering");

        DISPLAY_VECTORS = BUILDER
            .comment("Enable the vector rendering")
            .define("display_vectors", true);

        DISPLAY_VECTORS_INFO = BUILDER
            .comment("Enable the display vector info rendering")
            .define("display_vectors_info", true);

        DISPLAY_CENTER_OF_MASS = BUILDER
            .comment("The display center of the mass mode")
            .defineEnum(
                "display_center_of_mass",
                CenterOfMassMode.DETAILS
            );
        BUILDER.pop();
        BUILDER.push("Custom");
        FORCE_GROUP_CONFIGS_VALUE = BUILDER
            .define(
                "force_group_configs",
                () -> Config.of(
                    LinkedHashMap::new,
                    InMemoryFormat.withUniversalSupport()
                ),
                value -> value instanceof Config
            );

        BUILDER.pop();
    }



    public static final ModConfigSpec SPEC = BUILDER.build();



    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        ConfigUtils.loadForceGroupConfigs();


    }

    public static ForceGroupDisplayConfig getForceGroupConfig(
        ResourceLocation id
    ) {
        return FORCE_GROUP_CONFIGS.computeIfAbsent(
            id,
            ignored -> new ForceGroupDisplayConfig(true, true)
        );
    }

}
