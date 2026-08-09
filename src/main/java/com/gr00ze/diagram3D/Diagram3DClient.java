package com.gr00ze.diagram3D;

import com.gr00ze.diagram3D.commands.DevCommands;
import com.gr00ze.diagram3D.keybind.Keybinds;
import com.gr00ze.diagram3D.render.RenderTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(value = Diagram3D.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class Diagram3DClient {
    public Diagram3DClient(ModContainer container) {

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        Diagram3D.LOGGER.info("Configs registered");

    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
//        Diagram3D.LOGGER.info("HELLO FROM CLIENT SETUP");
//        Diagram3D.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        Diagram3D.LOGGER.info("Mod initialized");
        RenderTypes.init();
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        DevCommands.register(event.getDispatcher());
        Diagram3D.LOGGER.info("Commands registered");
    }

    @SubscribeEvent
    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        Keybinds.register(event);
        Diagram3D.LOGGER.info("Keybinds registered");
    }
}
