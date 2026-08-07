package com.gr00ze.diagram3D.mixin;

import com.gr00ze.diagram3D.data.DiagramDataManager;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(DiagramDataPacket.class)
public class DiagramPacketHandlerMixin {

    @Inject(
        method = "handle(Ldev/simulated_team/simulated/network/packets/contraption_diagram/DiagramDataPacket;)V",
        at = @At("TAIL")
    )
    private static void saveData(DiagramDataPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;
        if (!(screen instanceof DiagramScreen)) {
            DiagramDataManager.handleDiagramDataPacket(packet);
        }

    }
}
