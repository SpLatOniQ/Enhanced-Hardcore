package xyz.splatoniq.hardcore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class HeadRevive {
    public HeadRevive() {}

    public static void placeHead(Level level, BlockPos playerPosition, Player player) {
        level.setBlock(
                playerPosition,
                Blocks.PLAYER_HEAD.defaultBlockState().
                        setValue(SkullBlock.ROTATION, 0),
                3
        );

        if (level.getBlockEntity(playerPosition) instanceof SkullBlockEntity skullBlockEntity) {
            skullBlockEntity.setOwner(new ResolvableProfile(player.getGameProfile()));
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractionBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();

        if (level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockState state = event.getLevel().getBlockState(event.getPos());
        BlockPos pos = event.getPos();

        if (!state.is(Blocks.PLAYER_WALL_HEAD) && !state.is(Blocks.PLAYER_HEAD)) return;
        if (!(level.getBlockEntity(pos) instanceof SkullBlockEntity head)) return;

        String playerName = head.getOwnerProfile().name().orElse("");

        if (playerName.isEmpty()) return;

        ItemStack heldItem = event.getItemStack();
        String configItem = Config.REVIVAL_ITEM.get();

        if (configItem == null || configItem.trim().isEmpty()) return;

        ResourceLocation itemLocation = ResourceLocation.tryParse(configItem.toLowerCase());

        if (itemLocation == null) return;
        if (heldItem.getItem() != BuiltInRegistries.ITEM.get(itemLocation)) return;

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
