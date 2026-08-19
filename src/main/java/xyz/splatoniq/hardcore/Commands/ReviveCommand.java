package xyz.splatoniq.hardcore.Commands;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import xyz.splatoniq.hardcore.HardcoreEnhanced;
import xyz.splatoniq.hardcore.SavedData.DeadPlayersData;

import java.util.Collection;

@EventBusSubscriber(modid = HardcoreEnhanced.MODID)
public class ReviveCommand {
    @SubscribeEvent
    public static void onCommandRegistration(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("revive")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(3))
                .then(Commands.argument("player", EntityArgument.players())
                        .executes(context -> {
                            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "player");
                            DeadPlayersData data = HardcoreEnhanced.server.overworld().getDataStorage().computeIfAbsent(DeadPlayersData.TYPE, "hardcore_dead_players");

                            int revivedCount = 0;

                            for (ServerPlayer player : players) {
                                if (!HardcoreEnhanced.revivePlayer(player)) continue;

                                revivedCount++;
                            }

                            if (revivedCount == 0) {
                                context.getSource().sendFailure(Component.literal("None of the players are dead."));
                                return 0;
                            }

                            final int count = revivedCount;
                            context.getSource().sendSuccess(() -> Component.literal("Revived " + count + " player(s)"), true);

                            return revivedCount;
                        })
                )
        );
    }
}
