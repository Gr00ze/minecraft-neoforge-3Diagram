package com.gr00ze.diagram3D;

import com.gr00ze.diagram3D.data.DiagramRecords;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

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


    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue RADIUS = BUILDER.defineInRange("radius", 20F, 0, 128);
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER.define("enabled", true);
    public static final ModConfigSpec.BooleanValue DISPLAY_INFO_TAG = BUILDER.define("display_info_tag", true);
    public static final ModConfigSpec.EnumValue<CenterOfMassMode> DISPLAY_CENTER_OF_MASS = BUILDER.defineEnum("display_center_of_mass", CenterOfMassMode.DETAILS);
    //public static final ModConfigSpec.BooleanValue DISPLAY_CENTER_OF_MASS = BUILDER.define("display_center_of_mass", true);
    public static final ModConfigSpec.BooleanValue DISPLAY_VECTORS = BUILDER.define("display_vectors", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static final Map<ResourceLocation, ForceGroupDisplayConfig>
        FORCE_GROUP_CONFIGS = new HashMap<>();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
    }

    public static void toggle(ModConfigSpec.BooleanValue config){
        config.set(!config.get());
    }

    public static final class ForceGroupDisplayConfig {

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


    public static CenterOfMassMode getCenterOfMassMode() {
        return DISPLAY_CENTER_OF_MASS.get();
    }

    public static void setCenterOfMassMode(CenterOfMassMode mode) {
        DISPLAY_CENTER_OF_MASS.set(mode);
    }

    public static ForceGroupDisplayConfig getForceGroupConfig(
        ResourceLocation id
    ) {
        return FORCE_GROUP_CONFIGS.computeIfAbsent(
            id,
            ignored -> new ForceGroupDisplayConfig(true, true)
        );
    }

    public static void toggleVectors(ResourceLocation id) {
        getForceGroupConfig(id).toggleVectors();
    }

    public static void toggleInfo(ResourceLocation id) {
        getForceGroupConfig(id).toggleInfo();
    }

    public static @Nullable ResourceLocation getForceGroupId(
        DiagramRecords.ResolvedForceGroup forceGroup
    ) {
        if (!(forceGroup.name().getContents()
            instanceof TranslatableContents contents)) {
            return null;
        }

        String key = contents.getKey();

        if (!key.startsWith("force_group.")) {
            return null;
        }

        String value = key.substring("force_group.".length());

        int separator = value.indexOf('.');

        if (separator <= 0 || separator >= value.length() - 1) {
            return null;
        }

        return ResourceLocation.tryParse(
            value.substring(0, separator)
                + ":"
                + value.substring(separator + 1)
        );
    }
}
