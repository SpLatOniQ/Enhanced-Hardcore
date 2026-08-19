package xyz.splatoniq.hardcore;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record DeathPosition(ResourceKey<Level> dimension, BlockPos pos) { }
