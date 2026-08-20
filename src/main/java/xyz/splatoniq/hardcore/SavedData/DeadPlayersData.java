package xyz.splatoniq.hardcore.SavedData;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;
import xyz.splatoniq.hardcore.DeathPosition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeadPlayersData extends SavedData {
    private record DeadPlayerEntry(UUID uuid, DeathPosition position) {
        public static final Codec<DeadPlayerEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        UUIDUtil.CODEC.fieldOf("UUID").forGetter(DeadPlayerEntry::uuid),
                        BlockPos.CODEC.fieldOf("Pos").forGetter(e -> e.position().pos()),
                        ResourceKey.codec(Registries.DIMENSION).fieldOf("Dimension").forGetter(e -> e.position().dimension())
                ).apply(instance, (uuid, pos, dim) -> new DeadPlayerEntry(uuid, new DeathPosition(dim, pos)))
        );
    }

    public static final Codec<DeadPlayersData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    DeadPlayerEntry.CODEC.listOf().fieldOf("DeadPlayers").forGetter(data ->
                            data.deadPlayers.entrySet().stream()
                                    .map(e -> new DeadPlayerEntry(e.getKey(), e.getValue()))
                                    .toList()
                    ),
                    UUIDUtil.CODEC.listOf().fieldOf("PendingPlayers").forGetter(data ->
                            data.pendingPlayers.stream().toList()
                    )
            ).apply(instance, (deadList, pendingList) -> {
                DeadPlayersData data = new DeadPlayersData();

                for (DeadPlayerEntry entry : deadList) {
                    data.deadPlayers.put(entry.uuid(), entry.position());
                }

                data.pendingPlayers.addAll(pendingList);
                return data;
            })
    );

    public static final SavedDataType<DeadPlayersData> TYPE = new SavedDataType<>(
            "hardcore_dead_players",
            DeadPlayersData::new,
            CODEC,
            null
    );

    private final Map<UUID, DeathPosition> deadPlayers = new HashMap<>();
    private final Set<UUID> pendingPlayers = new HashSet<>();

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
}