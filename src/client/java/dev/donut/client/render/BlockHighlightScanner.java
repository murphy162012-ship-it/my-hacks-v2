package dev.donut.client.render;

import dev.donut.client.config.BlockHighlightSettings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class BlockHighlightScanner {
    private static final int REFRESH_TICKS = 10;
    private ClientLevel level;
    private List<Highlight> highlights = List.of();
    private int ticks;

    public List<Highlight> snapshot() {
        return highlights;
    }

    public void tick(Minecraft client, BlockHighlightSettings settings) {
        if (!EntityEspRules.throughWallsAllowed(client)) {
            settings.storageEsp = false;
            settings.spawnerEsp = false;
        }
        if (client.level == null || client.player == null
                || !settings.storageEnabled && !settings.spawnerEnabled
                && !settings.storageEsp && !settings.spawnerEsp) {
            level = null;
            highlights = List.of();
            ticks = 0;
            return;
        }
        if (client.level != level || ++ticks >= REFRESH_TICKS) {
            level = client.level;
            ticks = 0;
            scan(client.level, client.player.blockPosition(), settings);
        }
    }

    private void scan(ClientLevel world, BlockPos center, BlockHighlightSettings settings) {
        int range = settings.range;
        int chunkRadius = (range + 15) / 16;
        double rangeSquared = (double) range * range;
        List<Highlight> found = new ArrayList<>();
        for (int chunkX = (center.getX() >> 4) - chunkRadius; chunkX <= (center.getX() >> 4) + chunkRadius; chunkX++) {
            for (int chunkZ = (center.getZ() >> 4) - chunkRadius; chunkZ <= (center.getZ() >> 4) + chunkRadius; chunkZ++) {
                LevelChunk chunk = world.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                for (var blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (pos.distToCenterSqr(center.getX() + .5, center.getY() + .5, center.getZ() + .5) > rangeSquared) continue;
                        if ((settings.storageEnabled || settings.storageEsp) && (blockEntity instanceof ChestBlockEntity
                                || blockEntity instanceof EnderChestBlockEntity)) {
                            found.add(new Highlight(pos.immutable(), Kind.CHEST));
                        } else if ((settings.storageEnabled || settings.storageEsp) && blockEntity instanceof ShulkerBoxBlockEntity) {
                            found.add(new Highlight(pos.immutable(), Kind.SHULKER));
                        } else if ((settings.storageEnabled || settings.storageEsp) && blockEntity instanceof BarrelBlockEntity) {
                            found.add(new Highlight(pos.immutable(), Kind.STORAGE));
                    } else if ((settings.spawnerEnabled || settings.spawnerEsp) && blockEntity instanceof SpawnerBlockEntity) {
                        found.add(new Highlight(pos.immutable(), Kind.SPAWNER));
                    }
                }
            }
        }
        highlights = List.copyOf(found);
    }

    public enum Kind { CHEST, SHULKER, STORAGE, SPAWNER }
    public record Highlight(BlockPos pos, Kind kind) { }
}