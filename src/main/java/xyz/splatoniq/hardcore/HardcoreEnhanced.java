package xyz.splatoniq.hardcore;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import xyz.splatoniq.hardcore.Compat.CorpsesCompat;
import xyz.splatoniq.hardcore.Compat.GravestonesCompat;
import xyz.splatoniq.hardcore.Compat.RagdollsCompat;
import xyz.splatoniq.hardcore.SavedData.DeadPlayersData;

import java.util.UUID;

@Mod(HardcoreEnhanced.MODID)
public class HardcoreEnhanced {
    private enum CompatMod {
        BUILTIN, GRAVESTONES, CORPSES, RAGDOLLS
    }

    public static final String MODID = "enh_hardcore";
    public static MinecraftServer server;
    private static CompatMod compatMod = CompatMod.BUILTIN;

    public HardcoreEnhanced(ModContainer container) {
        NeoForge.EVENT_BUS.register(HardcoreEnhanced.class);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);

        if (ModList.get().isLoaded("ragdoll_corpse")) {
            compatMod = CompatMod.RAGDOLLS;
            NeoForge.EVENT_BUS.register(RagdollsCompat.class);
            return;
        }

        if (ModList.get().isLoaded("corpse")) {
            compatMod = CompatMod.CORPSES;
            NeoForge.EVENT_BUS.register(CorpsesCompat.class);
            return;
        }

        if (ModList.get().isLoaded("gravestone")) {
            compatMod = CompatMod.GRAVESTONES;
            NeoForge.EVENT_BUS.register(GravestonesCompat.class);
            return;
        }

        NeoForge.EVENT_BUS.register(HeadRevive.class);
    }

    @SubscribeEvent
    public static void onWorldLoad(ServerStartedEvent event) {
        server = event.getServer();
        server.overworld().getGameRules().getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).set(true, server);
    }

    public static boolean revivePlayer(ServerPlayer player) {
        DeadPlayersData data = getDeadPlayers();
        UUID playerID = player.getUUID();

        if (!data.isPlayerDead(playerID)) return false;

        if (player != null) {
            DeathPosition targetPos = new DeathPosition(server.overworld().dimension(), server.overworld().getSharedSpawnPos());

            if (Config.RESPAWN_AT_GRAVE.getAsBoolean()) {
                targetPos = data.getDeathPosition(playerID);
            }
            else {
                if (player.getRespawnPosition() != null && player.getRespawnDimension() != null) {
                    targetPos = new DeathPosition(player.getRespawnDimension(), player.getRaidOmenPosition());
                }
            }

            player.teleport(new TeleportTransition(
                    server.getLevel(targetPos.dimension()),
                    Vec3.atCenterOf(targetPos.pos()),
                    Vec3.ZERO,
                    0.0f,
                    0.0f,
                    TeleportTransition.DO_NOTHING
            ));

            player.setGameMode(GameType.SURVIVAL);

            if (compatMod == CompatMod.BUILTIN) {
                DeathPosition headPos = data.getDeathPosition(playerID);

                if (headPos != null)
                {
                    server.getLevel(headPos.dimension()).setBlock(
                            headPos.pos(),
                            Blocks.AIR.defaultBlockState(),
                            3
                    );
                }
            }

            data.removeDeadPlayer(playerID);
            return true;
        }
        else {
            return false;
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide()) return;
        if (!(entity instanceof Player player)) return;
        if (!level.getLevelData().isHardcore()) return;

        BlockPos playerPosition = player.blockPosition();

        if (compatMod == CompatMod.BUILTIN) {
            HeadRevive.placeHead(level, playerPosition, player);
        }

        DeadPlayersData data = getDeadPlayers();
        data.addDeadPlayer(player.getUUID(), player.level().dimension(), playerPosition);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        DeadPlayersData data = getDeadPlayers();
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID playerID = player.getUUID();

        if (!data.isPlayerPending(playerID)) return;

        revivePlayer(player);
        data.removePendingPlayer(playerID);
    }

    public static DeadPlayersData getDeadPlayers() {
        return server.overworld().getDataStorage().computeIfAbsent(DeadPlayersData.TYPE, "hardcore_dead_players");
    }

//    public static void sendChatMessage(String message) {
//        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
//    }
}
