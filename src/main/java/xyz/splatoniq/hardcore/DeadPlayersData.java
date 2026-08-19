package xyz.splatoniq.hardcore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeadPlayersData extends SavedData {
    private final Map<UUID, DeathPosition> deadPlayers = new HashMap<>();
    private final Set<UUID> pendingPlayers = new HashSet<>();

    public static final SavedData.Factory<DeadPlayersData> TYPE = new SavedData.Factory<>(
            DeadPlayersData::new,
            DeadPlayersData::load,
            null
    );

    public DeadPlayersData() {
        super();
    }

    public void addDeadPlayer(UUID uuid, ResourceKey<Level> dimension, BlockPos pos) {
        this.deadPlayers.put(uuid, new DeathPosition(dimension, pos.immutable()));
        this.setDirty();
    }

    public void removeDeadPlayer(UUID uuid) {
        if (this.deadPlayers.remove(uuid) != null) {
            this.setDirty();
        }
    }

    public boolean isPlayerDead(UUID uuid) {
        return this.deadPlayers.containsKey(uuid);
    }

    @Nullable
    public DeathPosition getDeathPosition(UUID uuid) {
        return this.deadPlayers.get(uuid);
    }

    public void addPendingPlayer(UUID uuid) {
        if (this.pendingPlayers.add(uuid)) {
            this.setDirty();
        }
    }

    public void removePendingPlayer(UUID uuid) {
        if (this.pendingPlayers.remove(uuid)) {
            this.setDirty();
        }
    }

    public boolean isPlayerPending(UUID uuid) {
        return this.pendingPlayers.contains(uuid);
    }

    public Set<UUID> getPendingPlayers() {
        return this.pendingPlayers;
    }

    public static DeadPlayersData load(CompoundTag tag, HolderLookup.Provider registries) {
        DeadPlayersData data = new DeadPlayersData();

        if (tag.contains("DeadPlayers", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("DeadPlayers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag entryTag = listTag.getCompound(i);
                UUID uuid = entryTag.getUUID("UUID");

                int x = entryTag.getInt("X");
                int y = entryTag.getInt("Y");
                int z = entryTag.getInt("Z");
                BlockPos pos = new BlockPos(x, y, z);

                ResourceLocation dimLoc = ResourceLocation.parse(entryTag.getString("Dimension"));
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimLoc);

                data.deadPlayers.put(uuid, new DeathPosition(dimension, pos));
            }
        }

        if (tag.contains("PendingPlayers", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("PendingPlayers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag entryTag = listTag.getCompound(i);
                UUID uuid = entryTag.getUUID("UUID");
                data.pendingPlayers.add(uuid);
            }
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag deadListTag = new ListTag();

        for (Map.Entry<UUID, DeathPosition> entry : this.deadPlayers.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", entry.getKey());

            DeathPosition position = entry.getValue();
            entryTag.putInt("X", position.pos().getX());
            entryTag.putInt("Y", position.pos().getY());
            entryTag.putInt("Z", position.pos().getZ());
            entryTag.putString("Dimension", position.dimension().location().toString());

            deadListTag.add(entryTag);
        }
        tag.put("DeadPlayers", deadListTag);

        ListTag pendingListTag = new ListTag();
        for (UUID uuid : this.pendingPlayers) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("UUID", uuid);
            pendingListTag.add(entryTag);
        }
        tag.put("PendingPlayers", pendingListTag);

        return tag;
    }
}