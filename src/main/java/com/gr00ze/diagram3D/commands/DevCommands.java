package com.gr00ze.diagram3D.commands;

import com.gr00ze.diagram3D.Diagram3D;
import com.gr00ze.diagram3D.data.DiagramDataManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class DevCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher
            .register(Commands.literal(Diagram3D.MOD_ID)
                .then(Commands.literal("debug")
                    .then(Commands.literal("reset")
                        .executes(context -> {
                            DiagramDataManager.reset();
                            return 1;
                        }))));
    }
}
