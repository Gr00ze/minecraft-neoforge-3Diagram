package com.gr00ze.diagram3D.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.gr00ze.diagram3D.data.DiagramRecords;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.gr00ze.diagram3D.config.ClientConfig.*;

public class ConfigUtils {

    public static void toggle(ModConfigSpec.BooleanValue config){
        config.set(!config.get());
    }


    public static ClientConfig.VectorMode getVectorsMode() {
        return VECTOR_MODE.get();
    }

    public static void setVectorsMode(ClientConfig.VectorMode mode) {
        VECTOR_MODE.set(mode);
    }

    public static ClientConfig.CenterOfMassMode getCenterOfMassMode() {
        return DISPLAY_CENTER_OF_MASS.get();
    }

    public static void setCenterOfMassMode(ClientConfig.CenterOfMassMode mode) {
        DISPLAY_CENTER_OF_MASS.set(mode);
    }



    public static void toggleVectors(ResourceLocation id) {

        getForceGroupConfig(id).toggleVectors();
        saveForceGroupConfigs();
    }

    public static void toggleInfo(ResourceLocation id) {
        getForceGroupConfig(id).toggleInfo();
        saveForceGroupConfigs();
    }

    public static @Nullable ResourceLocation getForceGroupId(
        DiagramRecords.ResolvedForceGroup forceGroup
    ) {
        return getForceGroupId(forceGroup.name());
    }

    public static @Nullable ResourceLocation getForceGroupId(
        Component forceGroupName
    ) {
        if (!(forceGroupName.getContents()
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

    private static void saveForceGroupConfigs()
    {
        Config configs = Config.of(
            LinkedHashMap::new,
            InMemoryFormat.withUniversalSupport()
        );

        for (Map.Entry<ResourceLocation, ForceGroupDisplayConfig> entry
            : FORCE_GROUP_CONFIGS.entrySet()) {

            Config config = Config.of(
                LinkedHashMap::new,
                InMemoryFormat.withUniversalSupport()
            );

            ForceGroupDisplayConfig value = entry.getValue();

            config.set("vectors", value.vectors());
            config.set("info", value.info());

            configs.set(entry.getKey().toString(), config);
        }

        FORCE_GROUP_CONFIGS_VALUE.set(configs);
        FORCE_GROUP_CONFIGS_VALUE.save();
    }


    public static void loadForceGroupConfigs() {
        FORCE_GROUP_CONFIGS.clear();

        Config configs = FORCE_GROUP_CONFIGS_VALUE.get();

        for (UnmodifiableConfig.Entry entry : configs.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());

            if (id == null) {
                continue;
            }

            Object value = entry.getValue();

            if (!(value instanceof UnmodifiableConfig config)) {
                continue;
            }

            boolean vectors = config.getOrElse("vectors", true);
            boolean info = config.getOrElse("info", true);

            FORCE_GROUP_CONFIGS.put(
                id,
                new ForceGroupDisplayConfig(vectors, info)
            );
        }
    }

    public static boolean isValidColor(Object value) {
        if (!(value instanceof String s)) {
            return false;
        }

        try {
            long color = Long.decode(s);
            return color >= 0 && color <= 0xFFFFFFFFL;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int getColor(ModConfigSpec.ConfigValue<String> color){
        return (int) Long.decode(color.get()).longValue();
    }
}
