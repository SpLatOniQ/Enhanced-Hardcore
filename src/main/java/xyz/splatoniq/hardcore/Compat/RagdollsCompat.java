package xyz.splatoniq.hardcore.Compat;

import dev.leo.ragdollcorpse.corpse.CorpseSavedData;
import dev.leo.sableplayerragdoll.api.RagdollInteractEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import xyz.splatoniq.hardcore.Config;
import xyz.splatoniq.hardcore.HardcoreEnhanced;
import xyz.splatoniq.hardcore.ReviveHelper;

import java.util.UUID;

public class RagdollsCompat {
    @SubscribeEvent
    public static void onRagdollInteract(RagdollInteractEvent event) {
        ServerLevel level = event.level();

        if (level.isClientSide() || event.player().getUsedItemHand() != InteractionHand.MAIN_HAND) return;

        UUID headId = event.rootId();
        ItemStack heldItem = ReviveHelper.isHoldingReviver(event.player());

        if (heldItem == null) return;

        MinecraftServer server = level.getServer();

        if (server == null) return;

        CorpseSavedData corpseData = CorpseSavedData.get(level);
        String playerName = corpseData.getOwnerName(headId);
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);

        if (player == null) return;
        if (!HardcoreEnhanced.getDeadPlayers().isPlayerDead(player.getUUID())) return;

        if (HardcoreEnhanced.revivePlayer(player)) {
            heldItem.shrink(1);
            event.setCanceled(true);
        }
    }
}