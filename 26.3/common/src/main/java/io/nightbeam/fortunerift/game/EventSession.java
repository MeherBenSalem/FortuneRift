package io.nightbeam.fortunerift.game;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EventSession {
    public final ServerLevel level;
    public final BlockPos anchor;
    public final long startedAt;
    private long hardEnd;
    private boolean ended;
    private final List<Restore> restores = new ArrayList<>();
    private final List<UUID> entities = new ArrayList<>();
    private final List<Runnable> endTasks = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();

    public EventSession(ServerLevel level, BlockPos anchor) {
        this.level = level;
        this.anchor = anchor.immutable();
        this.startedAt = level.getGameTime();
        this.hardEnd = startedAt + 20;
    }

    public void lifetime(int ticks) {
        this.hardEnd = startedAt + ticks;
    }

    public void restore(BlockPos pos, BlockState previous) {
        restores.add(new Restore(pos.immutable(), previous));
    }

    public void track(Entity entity) {
        entities.add(entity.getUUID());
    }

    public void onEnd(Runnable task) {
        endTasks.add(task);
    }

    public void schedule(int delayTicks, Runnable task) {
        tasks.add(new Task(startedAt + delayTicks, task));
        if (startedAt + delayTicks + 1 > hardEnd) {
            hardEnd = startedAt + delayTicks + 1;
        }
    }

    public void tick(long gameTime) {
        if (ended) {
            return;
        }
        for (Task task : List.copyOf(tasks)) {
            if (task.when <= gameTime) {
                tasks.remove(task);
                task.action.run();
            }
        }
        if (gameTime >= hardEnd) {
            end();
        }
    }

    public void end() {
        if (ended) {
            return;
        }
        ended = true;
        for (UUID id : entities) {
            Entity entity = level.getEntity(id);
            if (entity != null) {
                entity.discard();
            }
        }
        for (Restore restore : restores) {
            level.setBlock(restore.pos, restore.state, Block.UPDATE_ALL);
        }
        for (Runnable task : endTasks) {
            task.run();
        }
    }

    public boolean ended() {
        return ended;
    }

    private record Restore(BlockPos pos, BlockState state) {
    }

    private record Task(long when, Runnable action) {
    }
}
