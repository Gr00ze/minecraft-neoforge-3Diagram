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
    public enum VectorInfoMode implements TranslatableEnum {
        DISABLED,
        ALWAYS,
        ON_LOOK;

        @Override
        public @NotNull Component getTranslatedName() {
            return Component.translatable(
                "diagram3d.configuration.display_vectors_info." + name().toLowerCase(Locale.ROOT)
            );
        }
    }
    public enum VectorMode implements TranslatableEnum {
        DISABLED,
        SEPARATED,
        MERGED;

        @Override
        public @NotNull Component getTranslatedName() {
            return Component.translatable(
                "diagram3d.configuration.display_vector_mode." + name().toLowerCase(Locale.ROOT)
            );
        }
    }
    public enum VectorStyle implements TranslatableEnum {
        SLIM,
        TEXTURED;

        @Override
        public @NotNull Component getTranslatedName() {
            return Component.translatable(
                "diagram3d.configuration.vector_style." + name().toLowerCase(Locale.ROOT)
            );
        }
    }



    static final Map<ResourceLocation, ForceGroupDisplayConfig>
        FORCE_GROUP_CONFIGS = new ConcurrentHashMap<>();


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue RADIUS;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.EnumValue<VectorMode> VECTOR_MODE;
    public static final ModConfigSpec.BooleanValue DISPLAY_VECTOR_OUTLINE;
    public static final ModConfigSpec.EnumValue<VectorInfoMode> DISPLAY_VECTORS_INFO;
    public static final ModConfigSpec.EnumValue<CenterOfMassMode> DISPLAY_CENTER_OF_MASS;
    public static final ModConfigSpec.ConfigValue<Config> FORCE_GROUP_CONFIGS_VALUE;
    public static final ModConfigSpec.DoubleValue PROPORTIONAL_VECTOR_SCALING_FACTOR;
    public static final ModConfigSpec.DoubleValue QUADRATIC_VECTOR_SCALING_FACTOR;
    public static final ModConfigSpec.ConfigValue<String> INFO_BACKGROUND_COLOR;
    public static final ModConfigSpec.ConfigValue<String> GUI_BACKGROUND_COLOR;
    public static final ModConfigSpec.EnumValue<VectorStyle> VECTOR_STYLE;
    public static final ModConfigSpec.BooleanValue INFO_BACKGROUND_USE_TEXTURE;
    public static final ModConfigSpec.BooleanValue GUI_BACKGROUND_USE_TEXTURE;

    static {
        ENABLED = BUILDER
            .comment("Enable the entire mod")
            .define("enabled", true);

        RADIUS = BUILDER
            .comment("The scan radius for which the player will ask information for sublevels to the server ")
            .defineInRange("radius", 20F, 0, 128);

        BUILDER.push("Vectors");

        VECTOR_MODE = BUILDER
            .comment("Enable the vector rendering")
            .defineEnum("display_vector_mode", VectorMode.SEPARATED);

        DISPLAY_VECTOR_OUTLINE = BUILDER
            .comment("Enable a white outline for all vectors")
            .define("display_vectors_outline", true);

        PROPORTIONAL_VECTOR_SCALING_FACTOR = BUILDER.defineInRange(
            "proportional_vector_scaling_factor",
            0.01,
            0,
            2
        );
        QUADRATIC_VECTOR_SCALING_FACTOR = BUILDER.defineInRange(
            "quadratic_vector_scaling_factor",
            0.00001,
            0,
            2
        );


        VECTOR_STYLE = BUILDER
            .comment("Option to select in which way to render the vector")
            .defineEnum("vector_style", VectorStyle.SLIM);



        BUILDER.pop();

        BUILDER.push("Info display");

            DISPLAY_VECTORS_INFO = BUILDER
                .comment("Display vector info rendering mode")
                .defineEnum("display_vectors_info", VectorInfoMode.ON_LOOK);

            DISPLAY_CENTER_OF_MASS = BUILDER
                .comment("Display center of the mass mode")
                .defineEnum(
                    "display_center_of_mass",
                    CenterOfMassMode.ICON_DETAILS_ONLOOK
                );
            INFO_BACKGROUND_COLOR = BUILDER.define(
                "info_background_color",
                "0x993D3D3A",
                ConfigUtils::isValidColor
            );

            INFO_BACKGROUND_USE_TEXTURE = BUILDER.define(
                "info_background_use_texture",
                false
            );
        BUILDER.pop();

        GUI_BACKGROUND_COLOR = BUILDER.define(
            "gui_background_color",
            "0xA0101010",
            ConfigUtils::isValidColor
        );

        GUI_BACKGROUND_USE_TEXTURE = BUILDER.define(
            "gui_background_use_texture",
            false
        );

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
