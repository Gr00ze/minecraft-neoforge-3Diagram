package com.gr00ze.diagram3D;

import com.gr00ze.diagram3D.data.DiagramDataManager;
import com.gr00ze.diagram3D.keybind.Keybinds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class Events {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Keybinds.checkKeys();
        DiagramDataManager.onClientTick();

    }
}
