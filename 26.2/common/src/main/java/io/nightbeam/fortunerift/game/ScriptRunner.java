package io.nightbeam.fortunerift.game;

import io.nightbeam.fortunerift.config.FortuneConfig;
import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.EventRegistry;
import io.nightbeam.fortunerift.event.RiftVariant;
import io.nightbeam.fortunerift.fortune.FortuneState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.cubemob.Slime;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.Random;

public final class ScriptRunner {
    private static final Random RANDOM = new Random();

    private ScriptRunner() {
    }

    public static void run(EventDefinition event, ServerLevel level, BlockPos pos, ServerPlayer owner, EventSession session) {
        String behavior = event.params.getOrDefault("behavior", "loot:fortunerift:common:1");
        String[] parts = behavior.split(":");
        switch (parts[0]) {
            case "loot" -> RiftActions.loot(level, owner, pos, parts[1] + ":" + parts[2], Integer.parseInt(parts[3]));
            case "effect" -> RiftActions.effect(owner, parts[1] + ":" + parts[2], Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
            case "effect_nearby" -> nearbyEffect(level, pos, parts[1] + ":" + parts[2], Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
            case "mob" -> spawnCount(level, session, pos, parts[1] + ":" + parts[2], Integer.parseInt(parts[3]), 0);
            case "rain" -> RiftActions.rain(level, pos, parts[1] + ":" + parts[2], Integer.parseInt(parts[3]), false);
            case "fountain" -> RiftActions.rain(level, pos, parts[1] + ":" + parts[2], Integer.parseInt(parts[3]), true);
            case "xp" -> { if (owner != null) owner.giveExperiencePoints(Integer.parseInt(parts[1])); }
            case "chest" -> chest(level, pos.above(), parts[1] + ":" + parts[2], false);
            case "room" -> room(level, session, pos, owner);
            case "ally" -> timedMob(level, session, pos, parts[1] + ":" + parts[2], Integer.parseInt(parts[3]));
            case "ore" -> ore(level, session, pos);
            case "books" -> RiftActions.rain(level, pos, "minecraft:enchanted_book", 4, false);
            case "horse" -> spawnCount(level, session, pos, "minecraft:horse", 1, 0);
            case "creepers" -> creepers(level, session, pos);
            case "phantoms" -> phantoms(level, session, pos);
            case "spiders" -> spiders(level, session, pos);
            case "drowned" -> drowned(level, session, pos);
            case "spawn_temp" -> timedMob(level, session, pos, parts[1] + ":" + parts[2], Integer.parseInt(parts[4]));
            case "tnt" -> tnt(level, pos, owner, 40);
            case "anvil" -> anvil(level, pos);
            case "webs" -> box(level, session, pos, 1, Blocks.COBWEB.defaultBlockState(), 15 * 20);
            case "lightning" -> lightning(level, session, pos);
            case "pit" -> pit(level, session, pos);
            case "lava" -> lava(level, session, pos);
            case "scramble" -> scramble(level, session, owner);
            case "fake_tnt" -> fakeTnt(level, pos, owner);
            case "ice" -> floor(level, session, pos, Blocks.ICE.defaultBlockState(), 10 * 20);
            case "arrows" -> arrows(level, pos);
            case "sheep" -> sheep(level, pos);
            case "slime" -> slime(level, session, pos);
            case "shuffle" -> shuffle(level, session, pos);
            case "launch" -> { if (owner != null) owner.setDeltaMovement(0, 1.2, 0); }
            case "meteor" -> meteor(level, session, pos);
            case "armed" -> armed(level, session, pos);
            case "shadow" -> shadow(level, session, pos);
            case "cloud" -> spawnCount(level, session, pos, "minecraft:area_effect_cloud", 1, 0);
            case "fireworks" -> fireworks(level, pos);
            case "notes" -> RiftActions.message(owner, "A tune spills out of the rift.");
            case "dungeon", "vault", "sky", "library", "shrine", "camp", "cache" -> structure(level, session, pos, owner, parts[0]);
            case "gateway" -> RiftActions.message(owner, "A gateway flickers, then refuses you.");
            case "celestial" -> celestial(level, session, pos);
            case "favor" -> favor(level, pos);
            case "storm" -> storm(level, session);
            case "aurora" -> nearbyEffect(level, pos, "minecraft:luck", 20, 0);
            case "ore_rain" -> {
                RiftActions.rain(level, pos, "minecraft:raw_iron", 8, false);
                RiftActions.rain(level, pos, "minecraft:raw_gold", 4, false);
            }
            case "chorus" -> RiftActions.rain(level, pos, "minecraft:chorus_fruit", 6, true);
            case "armor" -> armor(level, owner, pos);
            case "second" -> second(level, pos, owner);
            case "wheel" -> wheel(level, pos, owner);
            case "double" -> RiftActions.loot(level, owner, pos, "fortunerift:rare", 1);
            case "hunt" -> hunt(level, session, pos, owner);
            case "spleef", "parkour", "hot", "archery", "arena" -> structure(level, session, pos, owner, parts[0]);
            case "merchant", "adventurer", "allay", "parade" -> npc(level, session, pos, parts[0]);
            case "sage" -> sage(level, owner);
            case "mimic" -> spawnCount(level, session, pos, "minecraft:slime", 1, 0);
            case "cataclysm" -> cataclysm(level, session, pos, owner);
            case "temple" -> structure(level, session, pos, owner, "temple");
            case "whisper" -> whisper(level, owner);
            case "reflection" -> reflection(level, session, pos, owner);
            default -> RiftActions.loot(level, owner, pos, "fortunerift:common", 1);
        }
        RiftActions.message(owner, event.name);
    }

    private static void spawnCount(ServerLevel level, EventSession session, BlockPos pos, String id, int count, double y) {
        for (int i = 0; i < count; i++) {
            Entity entity = RiftActions.spawn(level, pos.offset(i % 3 - 1, 0, i / 3 - 1), id, y);
            if (entity != null) {
                session.track(entity);
            }
        }
        session.lifetime(20 * 45);
    }

    private static void timedMob(ServerLevel level, EventSession session, BlockPos pos, String id, int seconds) {
        spawnCount(level, session, pos, id, partsCount(id), 0);
        session.lifetime(seconds * 20);
    }

    private static int partsCount(String ignored) {
        return 1;
    }

    private static void nearbyEffect(ServerLevel level, BlockPos pos, String effect, int seconds, int amp) {
        for (ServerPlayer player : RiftActions.nearby(level, pos, FortuneConfig.get().eventRadius)) {
            RiftActions.effect(player, effect, seconds, amp);
        }
    }

    private static void chest(ServerLevel level, BlockPos pos, String table, boolean tracked) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, io.nightbeam.fortunerift.registry.RiftIds.parse(table));
            chest.setLootTable(key);
        }
    }

    private static void room(ServerLevel level, EventSession session, BlockPos pos, ServerPlayer owner) {
        if (!FortuneConfig.get().allowTerrainModification) {
            RiftActions.loot(level, owner, pos, "fortunerift:rare", 1);
            return;
        }
        BlockPos origin = pos.below(4);
        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos cursor = origin.offset(x, y, z);
                    boolean shell = Math.abs(x) == 2 || Math.abs(z) == 2 || y == 0 || y == 3;
                    RiftActions.set(session, level, cursor, shell ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
        chest(level, origin.offset(0, 1, 0), "fortunerift:rare", true);
        session.lifetime(20);
    }

    private static void ore(ServerLevel level, EventSession session, BlockPos pos) {
        var ores = List.of(Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.COPPER_ORE, Blocks.GOLD_ORE);
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    if (x * x + y * y + z * z > 9) {
                        continue;
                    }
                    BlockPos cursor = pos.offset(x, y, z);
                    if (level.getBlockState(cursor).is(Blocks.STONE) || level.getBlockState(cursor).is(Blocks.DEEPSLATE)) {
                        RiftActions.set(session, level, cursor, ores.get(RANDOM.nextInt(ores.size())).defaultBlockState());
                    }
                }
            }
        }
        session.lifetime(20);
    }

    private static void creepers(ServerLevel level, EventSession session, BlockPos pos) {
        String id = FortuneConfig.get().allowTntEvents ? "minecraft:creeper" : "minecraft:zombie";
        spawnCount(level, session, pos, id, 4, 0);
    }

    private static void phantoms(ServerLevel level, EventSession session, BlockPos pos) {
        boolean night = level.getGameTime() % 24000L > 12500L;
        spawnCount(level, session, pos, night ? "minecraft:phantom" : "minecraft:husk", 3, night ? 6 : 0);
    }

    private static void spiders(ServerLevel level, EventSession session, BlockPos pos) {
        box(level, session, pos, 1, Blocks.COBWEB.defaultBlockState(), 30 * 20);
        spawnCount(level, session, pos, "minecraft:cave_spider", 4, 0);
    }

    private static void drowned(ServerLevel level, EventSession session, BlockPos pos) {
        spawnCount(level, session, pos, level.isWaterAt(pos) ? "minecraft:drowned" : "minecraft:husk", 3, 0);
    }

    private static void tnt(ServerLevel level, BlockPos pos, ServerPlayer owner, int fuse) {
        if (!FortuneConfig.get().allowTntEvents) {
            return;
        }
        PrimedTnt tnt = new PrimedTnt(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, owner);
        tnt.setFuse(fuse);
        level.addFreshEntity(tnt);
    }

    private static void anvil(ServerLevel level, BlockPos pos) {
        BlockPos above = pos.above(8);
        level.setBlock(above, Blocks.ANVIL.defaultBlockState(), 3);
        var falling = net.minecraft.world.entity.item.FallingBlockEntity.fall(level, above, Blocks.ANVIL.defaultBlockState());
        if (falling != null) {
            falling.time = 1;
        }
    }

    private static void box(ServerLevel level, EventSession session, BlockPos pos, int radius, net.minecraft.world.level.block.state.BlockState state, int ticks) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius * 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius || Math.abs(z) == radius || y == radius * 2) {
                        BlockPos cursor = pos.offset(x, y, z);
                        if (level.getBlockState(cursor).isAir()) {
                            RiftActions.set(session, level, cursor, state);
                        }
                    }
                }
            }
        }
        session.lifetime(ticks);
    }

    private static void lightning(ServerLevel level, EventSession session, BlockPos pos) {
        for (int i = 0; i < 4; i++) {
            BlockPos strike = pos.offset((i % 2 == 0 ? 4 : -4), 0, (i < 2 ? 4 : -4));
            var bolt = net.minecraft.world.entity.EntityTypes.LIGHTNING_BOLT.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (bolt != null) {
                bolt.setPos(strike.getX(), strike.getY(), strike.getZ());
                level.addFreshEntity(bolt);
            }
        }
        session.lifetime(40);
    }

    private static void pit(ServerLevel level, EventSession session, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y < 6; y++) {
                    BlockPos cursor = pos.below(y).offset(x, 0, z);
                    RiftActions.set(session, level, cursor, y == 5 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
        session.lifetime(20 * 20);
    }

    private static void lava(ServerLevel level, EventSession session, BlockPos pos) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    RiftActions.set(session, level, pos.offset(x, 0, z), Blocks.LAVA.defaultBlockState());
                }
            }
        }
        session.lifetime(10 * 20);
    }

    private static void scramble(ServerLevel level, EventSession session, ServerPlayer owner) {
        if (owner == null) {
            return;
        }
        var inventory = owner.getInventory();
        int size = Math.min(36, inventory.getContainerSize());
        for (int i = 0; i < size; i++) {
            int swap = RANDOM.nextInt(size);
            ItemStack a = inventory.getItem(i);
            inventory.setItem(i, inventory.getItem(swap));
            inventory.setItem(swap, a);
        }
        session.schedule(15 * 20, () -> RiftActions.message(owner, "Your pack settles back into place."));
        session.lifetime(15 * 20 + 5);
    }

    private static void fakeTnt(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        if (FortuneConfig.get().allowTntEvents) {
            tnt(level, pos, owner, 30);
        } else {
            RiftActions.give(level, owner, pos, new ItemStack(Items.DIAMOND_BLOCK));
        }
    }

    private static void floor(ServerLevel level, EventSession session, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, int ticks) {
        BlockPos ground = pos.below();
        RiftActions.set(session, level, ground, state);
        session.lifetime(ticks);
    }

    private static void arrows(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 12; i++) {
            var arrow = net.minecraft.world.entity.EntityTypes.ARROW.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (arrow != null) {
                arrow.setPos(pos.getX() + RANDOM.nextDouble() * 4 - 2, pos.getY() + 8, pos.getZ() + RANDOM.nextDouble() * 4 - 2);
                arrow.setDeltaMovement(0, -0.6, 0);
                level.addFreshEntity(arrow);
            }
        }
    }

    private static void sheep(ServerLevel level, BlockPos pos) {
        for (Sheep sheep : level.getEntitiesOfClass(Sheep.class, new net.minecraft.world.phys.AABB(pos).inflate(FortuneConfig.get().eventRadius))) {
            sheep.setColor(net.minecraft.world.item.DyeColor.byId(RANDOM.nextInt(16)));
        }
    }

    private static void slime(ServerLevel level, EventSession session, BlockPos pos) {
        Entity entity = RiftActions.spawn(level, pos, "minecraft:slime", 8);
        if (entity instanceof Slime slime) {
            slime.setSize(8, true);
            session.track(slime);
        }
        session.lifetime(20 * 60);
    }

    private static void shuffle(ServerLevel level, EventSession session, BlockPos pos) {
        for (int i = 0; i < 8; i++) {
            BlockPos a = pos.offset(RANDOM.nextInt(9) - 4, RANDOM.nextInt(3) - 1, RANDOM.nextInt(9) - 4);
            BlockPos b = pos.offset(RANDOM.nextInt(9) - 4, RANDOM.nextInt(3) - 1, RANDOM.nextInt(9) - 4);
            if (level.getBlockState(a).hasBlockEntity() || level.getBlockState(b).hasBlockEntity()) {
                continue;
            }
            var stateA = level.getBlockState(a);
            var stateB = level.getBlockState(b);
            RiftActions.set(session, level, a, stateB);
            RiftActions.set(session, level, b, stateA);
        }
        session.lifetime(20 * 20);
    }

    private static void meteor(ServerLevel level, EventSession session, BlockPos pos) {
        spawnCount(level, session, pos, "minecraft:magma_cube", 1, 6);
        RiftActions.set(session, level, pos, Blocks.FIRE.defaultBlockState());
        session.lifetime(20 * 10);
    }

    private static void armed(ServerLevel level, EventSession session, BlockPos pos) {
        Entity entity = RiftActions.spawn(level, pos, "minecraft:zombie", 0);
        if (entity instanceof Zombie zombie) {
            zombie.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
            zombie.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            session.track(zombie);
        }
        session.lifetime(20 * 60);
    }

    private static void shadow(ServerLevel level, EventSession session, BlockPos pos) {
        Entity entity = RiftActions.spawn(level, pos, "minecraft:armor_stand", 0);
        if (entity != null) {
            entity.setCustomName(net.minecraft.network.chat.Component.literal(" "));
            session.track(entity);
        }
        session.lifetime(60);
    }

    private static void fireworks(ServerLevel level, BlockPos pos) {
        for (int i = 0; i < 6; i++) {
            var rocket = net.minecraft.world.entity.EntityTypes.FIREWORK_ROCKET.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            if (rocket != null) {
                rocket.setPos(pos.getX() + i - 2, pos.getY() + 1, pos.getZ());
                level.addFreshEntity(rocket);
            }
        }
    }

    private static void structure(ServerLevel level, EventSession session, BlockPos pos, ServerPlayer owner, String kind) {
        if (!FortuneConfig.get().allowStructureEvents && !kind.equals("hot")) {
            RiftActions.loot(level, owner, pos, "fortunerift:rare", 1);
            return;
        }
        BlockPos base = switch (kind) {
            case "sky" -> pos.above(8);
            case "vault", "dungeon", "temple" -> pos;
            default -> pos;
        };
        var shell = switch (kind) {
            case "library" -> Blocks.BOOKSHELF.defaultBlockState();
            case "shrine" -> Blocks.CRYING_OBSIDIAN.defaultBlockState();
            case "camp" -> Blocks.OAK_LOG.defaultBlockState();
            case "spleef" -> Blocks.SNOW_BLOCK.defaultBlockState();
            default -> Blocks.DEEPSLATE_TILES.defaultBlockState();
        };
        int radius = kind.equals("temple") ? 4 : 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos floor = base.offset(x, 0, z);
                if (kind.equals("vault") || kind.equals("spleef") || kind.equals("hot")) {
                    RiftActions.set(session, level, floor, kind.equals("hot") ? Blocks.GOLD_BLOCK.defaultBlockState() : shell);
                } else {
                    level.setBlock(floor, shell, 3);
                }
            }
        }
        String table = kind.equals("temple") || kind.equals("vault") || kind.equals("dungeon") ? "fortunerift:epic" : "fortunerift:uncommon";
        chest(level, base.above(), table, false);
        if (kind.equals("dungeon") || kind.equals("temple") || kind.equals("arena")) {
            spawnCount(level, session, base.above(), "minecraft:zombie", kind.equals("arena") ? 6 : 3, 0);
        }
        if (kind.equals("vault") || kind.equals("spleef") || kind.equals("hot") || kind.equals("parkour")) {
            session.lifetime(kind.equals("vault") ? 45 * 20 : 30 * 20);
        }
        if (kind.equals("camp")) {
            level.setBlock(base.above(), Blocks.CAMPFIRE.defaultBlockState(), 3);
            session.onEnd(() -> level.setBlock(base.above(), Blocks.AIR.defaultBlockState(), 3));
        }
    }

    private static void celestial(ServerLevel level, EventSession session, BlockPos pos) {
        session.lifetime(30 * 20);
        for (int tick = 10; tick < 30 * 20; tick += 10) {
            int delay = tick;
            session.schedule(delay, () -> {
                int roll = RANDOM.nextInt(3);
                if (roll == 0) {
                    RiftActions.rain(level, pos, "minecraft:gold_nugget", 1, false);
                } else if (roll == 1) {
                    RiftActions.spawn(level, pos, "minecraft:silverfish", 0);
                } else if (FortuneConfig.get().allowTntEvents) {
                    tnt(level, pos.offset(RANDOM.nextInt(5) - 2, 4, RANDOM.nextInt(5) - 2), null, 20);
                }
            });
        }
    }

    private static void favor(ServerLevel level, BlockPos pos) {
        for (ServerPlayer player : RiftActions.nearby(level, pos, FortuneConfig.get().eventRadius)) {
            RiftActions.title(player, "FORTUNE HAS CHOSEN YOU");
            RiftActions.loot(level, player, pos, "fortunerift:mythic", 1);
        }
    }

    private static void storm(ServerLevel level, EventSession session) {
        lightning(level, session, session.anchor);
        session.lifetime(20 * 20);
    }

    private static void armor(ServerLevel level, ServerPlayer owner, BlockPos pos) {
        RiftActions.give(level, owner, pos, new ItemStack(Items.DIAMOND_HELMET));
        RiftActions.give(level, owner, pos, new ItemStack(Items.DIAMOND_CHESTPLATE));
        RiftActions.give(level, owner, pos, new ItemStack(Items.DIAMOND_LEGGINGS));
        RiftActions.give(level, owner, pos, new ItemStack(Items.DIAMOND_BOOTS));
    }

    private static void second(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        EventDefinition extra = EventRegistry.roll(RiftVariant.FORTUNE, level.dimension().identifier().toString(), new FortuneState(50), FortuneConfig.get(), RANDOM);
        if (extra != null && !"second_chance".equals(extra.id)) {
            run(extra, level, pos, owner, new EventSession(level, pos));
        }
    }

    private static void wheel(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        String[] tables = {"fortunerift:common", "fortunerift:uncommon", "fortunerift:rare", "fortunerift:epic"};
        String table = tables[RANDOM.nextInt(tables.length)];
        RiftActions.message(owner, "The wheel settles on " + table.substring(table.indexOf(':') + 1) + ".");
        RiftActions.loot(level, owner, pos, table, 1);
    }

    private static void hunt(ServerLevel level, EventSession session, BlockPos pos, ServerPlayer owner) {
        for (int i = 0; i < 5; i++) {
            BlockPos spot = pos.offset(RANDOM.nextInt(16) - 8, 1, RANDOM.nextInt(16) - 8);
            var item = RiftActions.item("fortunerift:rift_fragment");
            var entity = new net.minecraft.world.entity.item.ItemEntity(level, spot.getX(), spot.getY(), spot.getZ(), new ItemStack(item));
            level.addFreshEntity(entity);
            session.track(entity);
        }
        session.schedule(20 * 60, () -> RiftActions.loot(level, owner, pos, "fortunerift:uncommon", 1));
        session.lifetime(20 * 60 + 5);
    }

    private static void npc(ServerLevel level, EventSession session, BlockPos pos, String kind) {
        String id = switch (kind) {
            case "allay" -> "minecraft:allay";
            case "parade" -> "minecraft:iron_golem";
            default -> "minecraft:villager";
        };
        int count = kind.equals("parade") ? 3 : 1;
        spawnCount(level, session, pos, id, count, 0);
        session.lifetime(kind.equals("merchant") ? 20 * 180 : 20 * 60);
    }

    private static void sage(ServerLevel level, ServerPlayer owner) {
        if (owner == null) {
            return;
        }
        var profile = io.nightbeam.fortunerift.data.ProfileStore.get(level.getServer(), owner.getUUID());
        String hint = "Fortune is quiet.";
        for (EventDefinition event : EventRegistry.definitions()) {
            if (event.secret && !profile.seen.contains(event.id)) {
                hint = "A secret still waits: " + event.name + ".";
                break;
            }
        }
        RiftActions.message(owner, hint);
        RiftActions.effect(owner, "minecraft:luck", 30, 0);
    }

    private static void cataclysm(ServerLevel level, EventSession session, BlockPos pos, ServerPlayer owner) {
        if (!FortuneConfig.get().allowBossEvents) {
            return;
        }
        nearbyEffect(level, pos, "minecraft:darkness", 5, 0);
        Entity boss = RiftActions.spawn(level, pos, "minecraft:warden", 0);
        if (boss != null) {
            boss.setCustomName(net.minecraft.network.chat.Component.literal("Rift Cataclysm"));
            session.track(boss);
            session.onEnd(() -> RiftActions.loot(level, owner, pos, "fortunerift:cataclysm", 1));
        }
        session.lifetime(20 * 180);
    }

    private static void whisper(ServerLevel level, ServerPlayer owner) {
        if (owner == null) {
            return;
        }
        BlockPos shifted = owner.blockPosition().offset(8, 0, 0);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, shifted.getX(), shifted.getZ());
        owner.teleportTo(shifted.getX() + 0.5, y, shifted.getZ() + 0.5);
        RiftActions.effect(owner, "minecraft:darkness", 8, 0);
    }

    private static void reflection(ServerLevel level, EventSession session, BlockPos pos, ServerPlayer owner) {
        Entity entity = RiftActions.spawn(level, pos, "minecraft:zombie", 0);
        if (entity instanceof Zombie zombie && owner != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack worn = owner.getItemBySlot(slot);
                if (!worn.isEmpty()) {
                    zombie.setItemSlot(slot, worn.copy());
                }
            }
            session.track(zombie);
        }
        session.onEnd(() -> RiftActions.loot(level, owner, pos, "fortunerift:epic", 1));
        session.lifetime(20 * 30);
    }
}
