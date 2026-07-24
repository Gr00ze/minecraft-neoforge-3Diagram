package com.gr00ze.diagram3D;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import dev.simulated_team.simulated.network.packets.contraption_diagram.RequestDiagramDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import dev.ryanhcode.sable.sublevel.SubLevel;
import org.jetbrains.annotations.Nullable;
import foundry.veil.api.network.VeilPacketManager;



import static dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen.UPDATE_REQUEST_INTERVAL;

@Mod(value = Diagram3D.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class Diagram3DClient {
    public Diagram3DClient(ModContainer container) {

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        Diagram3D.LOGGER.info("HELLO FROM CLIENT SETUP");
        Diagram3D.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    private static float ticksWithoutUpdate = 0;
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null)
            return;

        Iterable<SubLevel> subLevels = Sable.HELPER.getAllIntersecting(mc.level, new BoundingBox3d());

        for (SubLevel subLevel : subLevels)
        {
            if (ticksWithoutUpdate++ > UPDATE_REQUEST_INTERVAL) {
                ticksWithoutUpdate = 0;
                VeilPacketManager.server().sendPacket(new CustomPacketPayload[]{new RequestDiagramDataPacket(subLevel.getUniqueId())});
            }

        }
    }


    public static void handleDiagramDataPacket(@Nullable DiagramDataPacket serverData){

    }
}
