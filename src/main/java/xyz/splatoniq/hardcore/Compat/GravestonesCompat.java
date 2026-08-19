package xyz.splatoniq.hardcore.Compat;

import de.maxhenkel.gravestone.tileentity.GraveStoneTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import xyz.splatoniq.hardcore.Config;
import xyz.splatoniq.hardcore.HardcoreEnhanced;
import xyz.splatoniq.hardcore.ReviveHelper;

public class GravestonesCompat {
    @SubscribeEvent
    public static void onPlayerInteractionBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();

        if (level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        BlockPos pos = event.getPos();

        Block graveBlock = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath("gravestone", "gravestone")
        ).orElse(null);

        if (graveBlock == null || !state.is(graveBlock)) return;
        if (!(level.getBlockEntity(pos) instanceof GraveStoneTileEntity gravestone)) return;

        String playerName = gravestone.getDeath().getPlayerName();

        if (playerName == null || playerName.isEmpty()) return;

        ItemStack heldItem = ReviveHelper.isHoldingReviver((ServerPlayer) event.getEntity());

        if (heldItem == null) return;

        ServerPlayer player = HardcoreEnhanced.server.getPlayerList().getPlayerByName(playerName);

        if (player == null) return;
        if (!HardcoreEnhanced.getDeadPlayers().isPlayerDead(player.getUUID())) return;

        if (HardcoreEnhanced.revivePlayer(player)) {
            heldItem.shrink(1);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}