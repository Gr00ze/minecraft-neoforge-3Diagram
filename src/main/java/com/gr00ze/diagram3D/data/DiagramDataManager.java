package com.gr00ze.diagram3D.data;

import com.gr00ze.diagram3D.Diagram3D;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import dev.simulated_team.simulated.network.packets.contraption_diagram.RequestDiagramDataPacket;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.gr00ze.diagram3D.ClientConfig.*;
import static com.gr00ze.diagram3D.data.DiagramDataResolver.convertDiagramData;

import com.gr00ze.diagram3D.data.DiagramRecords.*;

@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class DiagramDataManager {

    private static final Queue<UUID> diagramRequestQueue = new ArrayDeque<>();

    private static boolean waitingForResponse = false;
    private static int ticksWithoutUpdate = 0;

    public static final Map<UUID, DiagramDataCache> diagramDataCache = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null)
            return;


        long now = level.getGameTime();

        diagramDataCache.entrySet().removeIf(entry ->
            now - entry.getValue().lastUpdate() > TIMEOUT_RENDER_TICKS
        );

        ticksWithoutUpdate++;

        //Not the time yet
        if (ticksWithoutUpdate <= UPDATE_REQUEST_INTERVAL)
            return;

        //Still elaborating requests
        if (!diagramRequestQueue.isEmpty())
            return;

        //It's time!
        ticksWithoutUpdate = 0;

        Iterable<SubLevel> subLevels = Sable.HELPER.getAllIntersecting(level, new BoundingBox3d(
            player.getX() - RADIUS,
            player.getY() - RADIUS,
            player.getZ() - RADIUS,
            player.getX() + RADIUS,
            player.getY() + RADIUS,
            player.getZ() + RADIUS
        ));

        for (SubLevel subLevel : subLevels)
        {
            diagramRequestQueue.add(subLevel.getUniqueId());
        }
        //Send the first packet
        sendNextRequest();
    }

    private static void sendNextRequest()
    {
        if (waitingForResponse)
            return;

        UUID id = diagramRequestQueue.peek();

        if (id == null) //Should never happen
            return;


        waitingForResponse = true;

        VeilPacketManager.server()
            .sendPacket(new RequestDiagramDataPacket(id));
    }



    public static void handleDiagramDataPacket(@Nullable DiagramDataPacket serverData){
        if (diagramRequestQueue.isEmpty())
            return;


        UUID completed = diagramRequestQueue.poll();
        Level level = Minecraft.getInstance().level;

        if(serverData == null || level == null){ return;}

        diagramDataCache.put(completed, new DiagramDataCache(
            convertDiagramData(completed, serverData),
            level.getGameTime()
        ));


        waitingForResponse = false;

        Diagram3D.LOGGER.info(
            "Received diagram for {} ({} to groups)",
            completed,
            serverData.forces().size()
        );


        sendNextRequest();
    }


}
