package com.gr00ze.diagram3D.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.gr00ze.diagram3D.Diagram3D;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Diagram3D.MOD_ID)
public class ClientConfig
{

    public static final int UPDATE_REQUEST_TICK_INTERVAL = 1;
    public static final long TIMEOUT_RENDER_TICKS = 20;

    public enum CenterOfMassMode implements TranslatableEnum {
        DISABLED,
        ICON,
        DETAILS,
        ICON_DETAILS_ONLOOK;

        @Override
        public @NotNull Component getTranslatedName() {
            return Component.translatable(
                "diagram3d.configuration.display_center_of_mass." + name().toLowerCase(Locale.ROOT)
            );
        }
    }
    public enum VectorInfoMode {
        DISABLED,
        ALWAYS,
        ON_LOOK
    }
    public enum VectorMode {
        DISABLED,
        SEPARATED,
        MERGED

    }



    static final Map<ResourceLocation, ForceGroupDisplayConfig>
        FORCE_GROUP_CONFIGS = new ConcurrentHashMap<>();


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue RADIUS = BUILDER
        .comment("The scan radius for which the player will ask information for sublevels to the server ")
        .defineInRange("radius", 20F, 0, 128);
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER
        .comment("Enable the entire mod")
        .define("enabled", true);
    public static final ModConfigSpec.EnumValue<VectorMode> DISPLAY_VECTORS;
    public static final ModConfigSpec.BooleanValue DISPLAY_VECTOR_OUTLINE;
    public static final ModConfigSpec.EnumValue<VectorInfoMode> DISPLAY_VECTORS_INFO;
    public static final ModConfigSpec.EnumValue<CenterOfMassMode> DISPLAY_CENTER_OF_MASS;
    public static final ModConfigSpec.ConfigValue<Config> FORCE_GROUP_CONFIGS_VALUE;
    public static final ModConfigSpec.DoubleValue PROPORTIONAL_VECTOR_SCALING_FACTOR;
    public static final ModConfigSpec.DoubleValue QUADRATIC_VECTOR_SCALING_FACTOR;

    static {
        BUILDER.push("Rendering");

        DISPLAY_VECTORS = BUILDER
            .comment("Enable the vector rendering")
            .defineEnum("display_vectors", VectorMode.SEPARATED);

        DISPLAY_VECTOR_OUTLINE = BUILDER
            .comment("Enable a white outline for all vectors")
            .define("display_vectors_outline", true);

        DISPLAY_VECTORS_INFO = BUILDER
            .comment("Display vector info rendering mode")
            .defineEnum("display_vectors_info", VectorInfoMode.ON_LOOK);

        DISPLAY_CENTER_OF_MASS = BUILDER
            .comment("Display center of the mass mode")
            .defineEnum(
                "display_center_of_mass",
                CenterOfMassMode.ICON_DETAILS_ONLOOK
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

        PROPORTIONAL_VECTOR_SCALING_FACTOR = BUILDER.defineInRange("proportional_vector_scaling_factor", 0.01F, 0F, 2F);
        QUADRATIC_VECTOR_SCALING_FACTOR = BUILDER.defineInRange("quadratic_vector_scaling_factor", 0.00001F, 0F, 2F);
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
