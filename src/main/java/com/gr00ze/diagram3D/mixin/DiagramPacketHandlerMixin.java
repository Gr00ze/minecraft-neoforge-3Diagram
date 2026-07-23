package com.gr00ze.diagram3D.mixin;


import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
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
        System.out.println("HOOK RUOTE ATTIVO");

    }
}
