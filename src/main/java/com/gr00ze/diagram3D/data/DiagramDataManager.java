package com.gr00ze.diagram3D.data;

import com.gr00ze.diagram3D.Diagram3D;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.entities.diagram.screen.DiagramScreen;
import dev.simulated_team.simulated.network.packets.contraption_diagram.DiagramDataPacket;
import dev.simulated_team.simulated.network.packets.contraption_diagram.RequestDiagramDataPacket;
import foundry.veil.api.network.VeilPacketManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.gr00ze.diagram3D.ClientConfig.*;
import static com.gr00ze.diagram3D.data.DiagramDataResolver.convertDiagramData;

import com.gr00ze.diagram3D.data.DiagramRecords.*;

@EventBusSubscriber(modid = Diagram3D.MOD_ID, value = Dist.CLIENT)
public class DiagramDataManager {

    public static final Queue<UUID> diagramRequestQueue = new ArrayDeque<>();

    private static boolean waitingForResponse = false;
    private static int ticksWithoutUpdate = 0;

    public static final Map<UUID, DiagramDataCache> diagramDataCache = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        LocalPlayer player = mc.player;
        Screen screen = mc.screen;

        if(screen instanceof DiagramScreen){
            reset();
            return;
        }

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

//        if (diagramRequestQueue.size() == 1)
//        {
//            diagramRequestQueue.clear();
//            waitingForResponse = false;
//            return;
//
//        }

        //Still elaborating requests
        if (!diagramRequestQueue.isEmpty())
            return;

        //It's time!
        ticksWithoutUpdate = 0;

        Iterable<SubLevel> subLevels = getSubLevelInRange(level, player, RADIUS);

        for (SubLevel subLevel : subLevels)
        {
            diagramRequestQueue.add(subLevel.getUniqueId());
        }
        //Send the first packet
        sendNextRequest();
    }

    private static Iterable<SubLevel> getSubLevelInRange(@NonNull Level level,@NonNull Player player, double radius){
        return Sable.HELPER.getAllIntersecting(level, new BoundingBox3d(
            player.getX() - radius,
            player.getY() - radius,
            player.getZ() - radius,
            player.getX() + radius,
            player.getY() + radius,
            player.getZ() + radius
        ));
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

        Diagram3D.LOGGER.debug(
            "Received diagram for {} ({} to groups)",
            completed,
            serverData.forces().size()
        );


        sendNextRequest();
    }


    public static void reset() {
        diagramDataCache.clear();
        diagramRequestQueue.clear();
        waitingForResponse = false;
        ticksWithoutUpdate = UPDATE_REQUEST_INTERVAL;
    }
}
