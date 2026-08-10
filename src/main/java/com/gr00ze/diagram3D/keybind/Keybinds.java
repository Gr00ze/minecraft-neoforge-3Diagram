package com.gr00ze.diagram3D.keybind;

import com.gr00ze.diagram3D.ClientConfig;
import com.gr00ze.diagram3D.Diagram3D;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static KeyMapping createMapping(
        String name,
        com.mojang.blaze3d.platform.InputConstants.Type type,
        int keyCode
    ) {
        return new KeyMapping(
            String.format("key.%s.%s", Diagram3D.MOD_ID, name),
            type,
            keyCode,
            String.format("key.categories.%s", Diagram3D.MOD_ID)
        );
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_KEY);
    }

    public static final KeyMapping TOGGLE_KEY = createMapping(
        "toggle",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V
    );

    public static void checkKeys(){

        while (Keybinds.TOGGLE_KEY.consumeClick()) {
            ClientConfig.toggle(ClientConfig.ENABLED);
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(
                    Component.literal(
                        ClientConfig.ENABLED.get() ? "Enabled" : "Disabled"
                    ),
                    true
                );
            }
        }
    }





}
