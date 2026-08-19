package xyz.splatoniq.hardcore;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ReviveHelper {
    public static ItemStack isHoldingReviver(ServerPlayer player)
    {
        String configItemID = Config.REVIVAL_ITEM.get();

        if (configItemID == null || configItemID.trim().isEmpty()) return null;

        ResourceLocation configItemLocation = ResourceLocation.parse(configItemID);
        var configItem = BuiltInRegistries.ITEM.getOptional(configItemLocation);
        ItemStack heldItem = player.getMainHandItem();

        if (!heldItem.is(configItem.get())) return null;

        return heldItem;
    }
}
