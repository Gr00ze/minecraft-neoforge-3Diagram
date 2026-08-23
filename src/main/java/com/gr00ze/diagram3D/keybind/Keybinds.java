package com.gr00ze.diagram3D.keybind;

import com.gr00ze.diagram3D.config.ClientConfig;
import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.config.ConfigUtils;
import com.gr00ze.diagram3D.gui.DisplayConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
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


    private static long toggleKeyPressTime = 0;
    private static boolean toggleKeyWasDown = false;

    private static final long LONG_PRESS_DURATION_MS = 400;

    public static void checkKeys(){

        boolean keyDown = Keybinds.TOGGLE_KEY.isDown();

        if (keyDown && !toggleKeyWasDown) {
            toggleKeyPressTime = System.currentTimeMillis();
        }

        if (!keyDown && toggleKeyWasDown) {
            long duration = System.currentTimeMillis() - toggleKeyPressTime;

            if (duration >= LONG_PRESS_DURATION_MS) {

                Minecraft.getInstance().setScreen(new DisplayConfigScreen());

            } else {
                // Short press → toggle
                ConfigUtils.toggle(ClientConfig.ENABLED);

                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(
                        ClientConfig.ENABLED.get()
                            ? Component.translatable("diagram3d.messages.keypress.enabled")
                            .append("\n")
                            .append(
                                Component.translatable("diagram3d.messages.keypress.suggestion")
                                .withStyle(ChatFormatting.GRAY))

                            : Component.translatable("diagram3d.messages.keypress.disabled"),
                        false
                    );
                }
            }
        }

        toggleKeyWasDown = keyDown;
    }





}
