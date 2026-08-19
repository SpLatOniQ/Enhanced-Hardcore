package xyz.splatoniq.hardcore.Compat;

import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import xyz.splatoniq.hardcore.Config;
import xyz.splatoniq.hardcore.HardcoreEnhanced;
import xyz.splatoniq.hardcore.ReviveHelper;

import java.util.UUID;

public class CorpsesCompat {

    @SubscribeEvent
    public static void  onPlayerInteractionEntity(PlayerInteractEvent.EntityInteract event) {
        Level level = event.getLevel();

        if (level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;

        Entity eventEntity = event.getTarget();
        if (!(eventEntity instanceof CorpseEntity corpse)) return;

        ItemStack heldItem = ReviveHelper.isHoldingReviver((ServerPlayer) event.getEntity());

        if (heldItem == null) return;

        MinecraftServer server = level.getServer();

        if (server == null) return;

        UUID playerID = corpse.getCorpseUUID().orElse(null);

        if (playerID == null) return;

        ServerPlayer player = server.getPlayerList().getPlayer(playerID);

        if (player == null) {
            player = server.getPlayerList().getPlayerByName(corpse.getScoreboardName());
        }

        if (player == null) return;
        if (!HardcoreEnhanced.getDeadPlayers().isPlayerDead(playerID)) return;

        if (HardcoreEnhanced.revivePlayer(player)) {
            heldItem.shrink(1);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}