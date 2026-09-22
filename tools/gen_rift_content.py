"""Generate FortuneRift datapack JSON, lang, models, and block textures."""
import json
import struct
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "26.3" / "common" / "src" / "main" / "resources"
ALL = ["fortune_rift", "blessed_rift", "cursed_rift", "ancient_rift", "void_rift", "mythic_rift"]

def ev(i, name, desc, rarity, category, kind, **flags):
    data = {
        "id": i,
        "name": name,
        "description": desc,
        "weight": flags.pop("weight", 10),
        "rarity": rarity.upper(),
        "category": category.upper(),
        "dimensions": flags.pop("dimensions", ["minecraft:overworld", "minecraft:the_nether", "minecraft:the_end"]),
        "min_player_distance": 0,
        "cooldown_ticks": flags.pop("cooldown", 0),
        "secret": flags.pop("secret", False),
        "positive": flags.pop("positive", True),
        "destructive": flags.pop("destructive", False),
        "terrain": flags.pop("terrain", False),
        "tnt": flags.pop("tnt", False),
        "boss": flags.pop("boss", False),
        "structure": flags.pop("structure", False),
        "variants": flags.pop("variants", ALL),
        "enabled": True,
    }
    prefix = kind.split(":", 1)[0]
    if prefix in ("loot", "effect", "mob"):
        data["type"] = {"loot": "loot_burst", "effect": "status_effect", "mob": "mob_spawn"}[prefix]
        data["script"] = None
    else:
        data["type"] = None
        data["script"] = i
    data["behavior"] = kind
    for key, value in flags.items():
        data[key] = value
    return data

EVENTS = [
    ev("diamond_rain", "Diamond Rain", "Diamonds fall around the rift.", "uncommon", "reward", "rain:minecraft:diamond:16"),
    ev("emerald_fountain", "Emerald Fountain", "Emeralds arc out of the rift.", "uncommon", "reward", "fountain:minecraft:emerald:12"),
    ev("golden_apple_jackpot", "Golden Apple Jackpot", "Golden apples spill out.", "rare", "reward", "loot:fortunerift:rare:3", loot_table="fortunerift:rare"),
    ev("experience_explosion", "Experience Explosion", "Experience bursts from the rift.", "common", "reward", "xp:30"),
    ev("enchanted_equipment_chest", "Enchanted Equipment", "A chest of enchanted gear appears.", "rare", "reward", "chest:fortunerift:rare"),
    ev("rare_potion_bundle", "Potion Bundle", "A bundle of potions.", "uncommon", "reward", "loot:fortunerift:uncommon:4", loot_table="fortunerift:uncommon"),
    ev("underground_treasure_room", "Treasure Room", "A small treasure room opens below.", "rare", "reward", "room", terrain=True, structure=True),
    ev("iron_golem_guardian", "Golem Guardian", "An iron golem guards you for a minute.", "uncommon", "reward", "ally:minecraft:iron_golem:60"),
    ev("temporary_luck", "Temporary Luck", "Luck III for a minute.", "common", "reward", "effect:minecraft:luck:60:2"),
    ev("emerald_trade_gift", "Trade Gift", "Emeralds and trade goods.", "common", "reward", "loot:fortunerift:uncommon:2", loot_table="fortunerift:uncommon"),
    ev("dungeon_loot_chest", "Dungeon Chest", "A dungeon chest appears.", "uncommon", "reward", "chest:fortunerift:uncommon"),
    ev("ore_vein_burst", "Ore Vein", "Stone nearby becomes ore.", "uncommon", "reward", "ore", terrain=True),
    ev("tool_gift", "Tool Gift", "An enchanted iron tool.", "common", "reward", "loot:fortunerift:common:1", loot_table="fortunerift:common"),
    ev("food_feast", "Food Feast", "A stack of food.", "common", "reward", "loot:fortunerift:common:1", loot_table="fortunerift:common"),
    ev("totem_drop", "Totem", "A totem of undying.", "epic", "reward", "loot:fortunerift:epic:1", loot_table="fortunerift:epic"),
    ev("netherite_scrap_cache", "Scrap Cache", "Netherite scrap.", "rare", "reward", "loot:fortunerift:rare:2", loot_table="fortunerift:rare"),
    ev("enchanted_book_storm", "Book Storm", "Enchanted books fall.", "rare", "reward", "books"),
    ev("beacon_flash", "Beacon Flash", "A short burst of haste and regeneration.", "uncommon", "reward", "effect:minecraft:regeneration:20:1"),
    ev("music_disc_gift", "Music Disc", "A music disc.", "uncommon", "reward", "loot:fortunerift:uncommon:1", loot_table="fortunerift:uncommon"),
    ev("shulker_supplies", "Shulker Supplies", "A shulker of supplies.", "epic", "reward", "loot:fortunerift:epic:1", loot_table="fortunerift:epic"),
    ev("horse_gift", "Horse Gift", "A saddled horse.", "rare", "reward", "horse"),
    ev("name_tag_bundle", "Name Tags", "Name tags and leads.", "common", "reward", "loot:fortunerift:common:1", loot_table="fortunerift:common"),
    ev("zombie_pack", "Zombie Pack", "Zombies claw out of the ground.", "common", "mob", "mob:minecraft:zombie:4", positive=False),
    ev("skeleton_ambush", "Skeleton Ambush", "Skeletons take aim.", "common", "mob", "mob:minecraft:skeleton:3", positive=False),
    ev("creeper_circle", "Creeper Circle", "Creepers surround the rift.", "uncommon", "mob", "creepers", positive=False, tnt=True),
    ev("witch_circle", "Witch Circle", "Witches appear.", "uncommon", "mob", "mob:minecraft:witch:2", positive=False),
    ev("phantom_dive", "Phantom Dive", "Phantoms dive, or husks in daylight.", "uncommon", "mob", "phantoms", positive=False),
    ev("silverfish_burst", "Silverfish Burst", "Silverfish scatter.", "common", "mob", "mob:minecraft:silverfish:8", positive=False),
    ev("endermite_swarm", "Endermite Swarm", "Endermites swarm.", "common", "mob", "mob:minecraft:endermite:6", positive=False),
    ev("pillager_patrol", "Pillager Patrol", "A patrol with no raid omen.", "rare", "mob", "mob:minecraft:pillager:4", positive=False),
    ev("blaze_column", "Blaze Column", "Blazes rise.", "rare", "mob", "mob:minecraft:blaze:3", positive=False),
    ev("cave_spider_nest", "Spider Nest", "Cave spiders and temporary webs.", "uncommon", "mob", "spiders", positive=False),
    ev("drowned_or_husk", "Wet or Dry", "Drowned in water, otherwise husks.", "uncommon", "mob", "drowned", positive=False),
    ev("vex_haunting", "Vex Haunting", "Vexes that fade.", "rare", "mob", "spawn_temp:minecraft:vex:4:30", positive=False),
    ev("short_fuse_tnt", "Short Fuse", "Primed TNT with a short fuse.", "uncommon", "trap", "tnt", positive=False, tnt=True),
    ev("anvil_drop", "Anvil Drop", "An anvil falls.", "uncommon", "trap", "anvil", positive=False),
    ev("cobweb_cage", "Cobweb Cage", "Cobwebs close in, then clear.", "common", "trap", "webs", positive=False),
    ev("lightning_ring", "Lightning Ring", "Lightning strikes around the rift.", "rare", "trap", "lightning", positive=False, destructive=True),
    ev("pit_fall", "Pitfall", "A shallow watered pit opens.", "uncommon", "trap", "pit", positive=False, terrain=True),
    ev("blindness_pulse", "Blindness", "A brief blindness.", "common", "trap", "effect:minecraft:blindness:8:0", positive=False),
    ev("lava_moat", "Lava Moat", "A lava ring that turns back to stone.", "rare", "trap", "lava", positive=False, terrain=True, destructive=True),
    ev("inventory_scramble", "Scramble", "Your inventory shuffles, then restores.", "rare", "trap", "scramble", positive=False),
    ev("fake_diamond_tnt", "Fake Diamonds", "Diamonds that were TNT.", "uncommon", "trap", "fake_tnt", positive=False, tnt=True),
    ev("floor_is_ice", "Ice Floor", "The floor becomes ice, then restores.", "common", "trap", "ice", positive=False, terrain=True),
    ev("mining_fatigue_curse", "Heavy Hands", "Mining fatigue.", "common", "trap", "effect:minecraft:mining_fatigue:20:1", positive=False),
    ev("arrow_rain", "Arrow Rain", "Arrows fall.", "uncommon", "trap", "arrows", positive=False),
    ev("strange_gravity", "Strange Gravity", "You lift off the ground.", "uncommon", "chaos", "effect:minecraft:levitation:3:0", positive=False),
    ev("chicken_storm", "Chicken Storm", "Twenty chickens.", "common", "chaos", "mob:minecraft:chicken:20"),
    ev("sheep_recolor", "Dyed Sheep", "Nearby sheep change color.", "common", "chaos", "sheep"),
    ev("giant_slime", "Giant Slime", "A huge slime drops in.", "rare", "chaos", "slime", positive=False),
    ev("block_shuffle", "Block Shuffle", "A few blocks trade places, then return.", "rare", "chaos", "shuffle", terrain=True),
    ev("launch_player", "Launch", "You are thrown upward.", "uncommon", "chaos", "launch", positive=False),
    ev("miniature_meteor", "Meteor", "A magma cube and a brief fire.", "rare", "chaos", "meteor", positive=False, destructive=True),
    ev("armed_mob", "Armed Mob", "A zombie in heavy gear.", "epic", "chaos", "armed", positive=False),
    ev("bee_swarm", "Bee Swarm", "Angry bees.", "uncommon", "chaos", "mob:minecraft:bee:8", positive=False),
    ev("shadow_apparition", "Shadow", "A figure appears, then is gone.", "rare", "chaos", "shadow", secret=True),
    ev("random_potion_cloud", "Potion Cloud", "A lingering cloud.", "uncommon", "chaos", "cloud"),
    ev("local_darkness", "Local Darkness", "Darkness closes in.", "common", "chaos", "effect:minecraft:darkness:12:0", positive=False),
    ev("firework_panic", "Fireworks", "Fireworks go off.", "uncommon", "chaos", "fireworks"),
    ev("note_concert", "Note Concert", "A short song of note particles.", "common", "chaos", "notes"),
    ev("rift_dungeon", "Rift Dungeon", "A dungeon entrance opens.", "epic", "structure", "dungeon", structure=True),
    ev("the_vault", "The Vault", "A vault you can loot before it seals.", "epic", "structure", "vault", structure=True, secret=True),
    ev("sky_chest", "Sky Chest", "A chest on a small platform.", "rare", "structure", "sky", structure=True),
    ev("library_nook", "Library Nook", "A bookshelf nook and a chest.", "uncommon", "structure", "library", structure=True),
    ev("ruined_shrine", "Ruined Shrine", "A crying-obsidian shrine. The portal is not lit.", "uncommon", "structure", "shrine", structure=True),
    ev("false_gateway", "False Gateway", "Gateway particles, and no teleport.", "rare", "structure", "gateway"),
    ev("campsite", "Campsite", "A camp that goes quiet when the event ends.", "common", "structure", "camp", structure=True),
    ev("mineshaft_cache", "Mineshaft Cache", "A plank cache.", "uncommon", "structure", "cache", structure=True),
    ev("celestial_rain", "Celestial Rain", "Loot, creatures, and blasts fall for half a minute.", "mythic", "world_event", "celestial", secret=True, tnt=True),
    ev("fortunes_favor", "Fortune's Favor", "Fortune chooses everyone nearby.", "mythic", "world_event", "favor", secret=True),
    ev("short_storm", "Short Storm", "Thunder for a minute, then the old weather returns.", "uncommon", "world_event", "storm"),
    ev("aurora", "Aurora", "Luck and light for nearby players.", "rare", "world_event", "aurora"),
    ev("ore_rain", "Ore Rain", "Raw ore falls.", "uncommon", "world_event", "ore_rain"),
    ev("chorus_bounce", "Chorus Bounce", "Chorus fruit, and a lift in the End.", "uncommon", "world_event", "chorus"),
    ev("tide_of_fish", "Tide of Fish", "Fish and prismarine.", "common", "world_event", "loot:fortunerift:common:3", loot_table="fortunerift:common"),
    ev("eclipse", "Eclipse", "Darkness for nearby players.", "rare", "world_event", "effect_nearby:minecraft:darkness:20:0", positive=False),
    ev("netherite_jackpot", "Netherite Jackpot", "Netherite ingots.", "epic", "jackpot", "loot:fortunerift:epic:1", loot_table="fortunerift:epic"),
    ev("ancient_debris_rain", "Debris Rain", "Ancient debris falls.", "epic", "jackpot", "rain:minecraft:ancient_debris:6"),
    ev("enchanted_golden_cache", "Golden Cache", "Enchanted golden apples.", "mythic", "jackpot", "loot:fortunerift:mythic:1", loot_table="fortunerift:mythic"),
    ev("emerald_block_pile", "Emerald Pile", "Emerald blocks as items.", "rare", "jackpot", "rain:minecraft:emerald_block:4"),
    ev("diamond_armor_set", "Diamond Armor", "A full set of protected diamond armor.", "epic", "jackpot", "armor"),
    ev("second_chance", "Second Chance", "Another reward follows.", "rare", "jackpot", "second"),
    ev("fortune_wheel", "Fortune Wheel", "A wheel spins, then pays the slice it already chose.", "rare", "minigame", "wheel"),
    ev("double_or_nothing", "Double or Nothing", "Take the reward, or risk it.", "epic", "minigame", "double"),
    ev("rift_hunt", "Rift Hunt", "Collect three fragments in a minute.", "rare", "minigame", "hunt"),
    ev("spleef_pocket", "Spleef Pocket", "Stay up for thirty seconds.", "uncommon", "minigame", "spleef", terrain=True),
    ev("archery_trial", "Archery Trial", "Hit the targets.", "uncommon", "minigame", "archery"),
    ev("parkour_pillars", "Parkour Pillars", "Reach the last pillar.", "uncommon", "minigame", "parkour", terrain=True),
    ev("one_wave_arena", "One Wave", "Clear one wave of zombies.", "rare", "minigame", "arena"),
    ev("hot_floor", "Hot Floor", "Stand on the gold.", "uncommon", "minigame", "hot", terrain=True),
    ev("rift_merchant", "Rift Merchant", "A merchant with odd trades, gone in three minutes.", "rare", "npc", "merchant"),
    ev("lost_adventurer", "Lost Adventurer", "A villager points you at a buried chest.", "uncommon", "npc", "adventurer"),
    ev("rift_sage", "Rift Sage", "A hint about a secret you have not seen.", "rare", "npc", "sage"),
    ev("allay_courier", "Allay Courier", "An allay delivers a gift.", "uncommon", "npc", "allay"),
    ev("golem_parade", "Golem Parade", "Three golems for a short while.", "rare", "npc", "parade"),
    ev("mimic_rift", "Mimic Rift", "The rift was a creature.", "rare", "secret", "mimic", secret=True, positive=False),
    ev("the_cataclysm", "The Cataclysm", "The sky darkens and a rift boss arrives.", "mythic", "secret", "cataclysm", secret=True, boss=True, positive=False),
    ev("rift_temple", "Rift Temple", "A temple with a lever puzzle and a final chest.", "mythic", "secret", "temple", secret=True, structure=True, boss=True),
    ev("void_whisper", "Void Whisper", "You are shifted to a nearby safe spot.", "epic", "secret", "whisper", secret=True, positive=False),
    ev("players_reflection", "Reflection", "A copy of your armor fights you.", "epic", "secret", "reflection", secret=True, positive=False),
]

assert len(EVENTS) == 100, len(EVENTS)
assert len({e["id"] for e in EVENTS}) == 100

LOOT = {
    "common": [("coal", 8, 4, 12), ("iron_ingot", 6, 2, 6), ("bread", 6, 4, 10), ("arrow", 5, 8, 16), ("leather", 4, 2, 6), ("copper_ingot", 5, 3, 8), ("experience_bottle", 3, 1, 3)],
    "uncommon": [("gold_ingot", 6, 2, 6), ("emerald", 5, 1, 4), ("lapis_lazuli", 4, 3, 8), ("golden_carrot", 4, 2, 4), ("ender_pearl", 3, 1, 2), ("iron_pickaxe", 2, 1, 1)],
    "rare": [("diamond", 6, 1, 3), ("golden_apple", 4, 1, 2), ("ender_pearl", 3, 1, 2), ("blaze_rod", 3, 1, 2), ("name_tag", 2, 1, 1), ("experience_bottle", 3, 2, 5)],
    "epic": [("diamond", 5, 3, 8), ("netherite_scrap", 4, 1, 2), ("totem_of_undying", 2, 1, 1), ("enchanted_golden_apple", 1, 1, 1), ("shulker_box", 2, 1, 1)],
    "mythic": [("netherite_ingot", 5, 1, 1), ("enchanted_golden_apple", 4, 1, 2), ("elytra", 1, 1, 1), ("nether_star", 1, 1, 1)],
}

CRACKS = {
    (2, 1), (3, 2), (4, 3), (5, 4), (6, 5), (7, 6), (8, 7), (9, 8), (10, 9),
    (8, 2), (9, 3), (10, 4), (11, 5), (4, 10), (5, 11), (6, 12), (3, 8), (12, 7), (13, 6), (2, 12),
}
COLORS = {
    "fortune_rift": (224, 160, 32),
    "blessed_rift": (255, 214, 96),
    "cursed_rift": (150, 70, 190),
    "ancient_rift": (70, 180, 160),
    "void_rift": (90, 40, 160),
    "mythic_rift": (255, 244, 210),
}
FRAME_BIAS = (0, 30, 70, 30)


def chunk(tag, data):
    return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)


def write_png(path, width, height, rows):
    raw = b"".join(b"\x00" + row for row in rows)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    path.write_bytes(png)


def texture(name, color):
    rows = []
    for frame, bias in enumerate(FRAME_BIAS):
        for y in range(16):
            row = bytearray()
            for x in range(16):
                if (x, y) in CRACKS:
                    r, g, b = color
                    scale = 1 + bias / 200
                    row += bytes((min(255, int(r * scale)), min(255, int(g * scale)), min(255, int(b * scale)), 255))
                elif (x * 3 + y * 5 + frame) % 17 == 0:
                    row += bytes((12, 10, 18, 255))
                else:
                    row += bytes((22, 16, 34, 255))
            rows.append(bytes(row))
    return rows


def loot_table(entries):
    return {
        "type": "minecraft:chest",
        "pools": [{
            "rolls": 2,
            "entries": [{
                "type": "minecraft:item",
                "name": f"minecraft:{item}",
                "weight": weight,
                "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}],
            } for item, weight, lo, hi in entries],
        }],
    }


def shaped(pattern, key, result):
    return {
        "type": "minecraft:crafting_shaped",
        "pattern": pattern,
        "key": {symbol: item if item.startswith("minecraft:") or item.startswith("fortunerift:") else f"minecraft:{item}" for symbol, item in key.items()},
        "result": {"id": result, "count": 1},
    }


def shapeless(ingredients, result):
    return {
        "type": "minecraft:crafting_shapeless",
        "ingredients": ingredients,
        "result": {"id": result, "count": 1},
    }


def write_item_definition(name, model):
    folder = ROOT / "assets" / "fortunerift" / "items"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{name}.json").write_text(json.dumps({
        "model": {"type": "minecraft:model", "model": model}
    }, indent=2) + "\n", encoding="utf-8")


def main():
    events = ROOT / "data" / "fortunerift" / "events"
    events.mkdir(parents=True, exist_ok=True)
    ids = []
    for event in EVENTS:
        payload = {k: v for k, v in event.items() if v is not None}
        (events / f"{event['id']}.json").write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")
        ids.append(event["id"] + "\t" + event["behavior"])
    (events / "index.txt").write_text("\n".join(ids) + "\n", encoding="utf-8")

    loot_root = ROOT / "data" / "fortunerift" / "loot_table"
    loot_root.mkdir(parents=True, exist_ok=True)
    for name, entries in LOOT.items():
        (loot_root / f"{name}.json").write_text(json.dumps(loot_table(entries), indent=2) + "\n", encoding="utf-8")
    (loot_root / "cataclysm.json").write_text(json.dumps({
        "type": "minecraft:chest",
        "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "fortunerift:rift_heart"}]},
            {"rolls": 1, "entries": [{"type": "minecraft:loot_table", "value": "fortunerift:mythic"}]},
        ],
    }, indent=2) + "\n", encoding="utf-8")

    recipes = ROOT / "data" / "fortunerift" / "recipe"
    recipes.mkdir(parents=True, exist_ok=True)
    (recipes / "fortune_rift.json").write_text(json.dumps(shaped(
        ["OGO", "GAG", "OGO"], {"O": "obsidian", "G": "gold_ingot", "A": "amethyst_shard"}, "fortunerift:fortune_rift"
    ), indent=2) + "\n", encoding="utf-8")
    pairs = {
        "blessed_rift": "minecraft:glistering_melon_slice",
        "cursed_rift": "minecraft:ghast_tear",
        "ancient_rift": "minecraft:echo_shard",
        "void_rift": "minecraft:ender_eye",
    }
    for name, extra in pairs.items():
        (recipes / f"{name}.json").write_text(json.dumps(shapeless(
            ["fortunerift:fortune_rift", extra], f"fortunerift:{name}"
        ), indent=2) + "\n", encoding="utf-8")
    (recipes / "mythic_rift.json").write_text(json.dumps(shapeless(
        ["fortunerift:fortune_rift", "minecraft:netherite_ingot", "minecraft:enchanted_golden_apple"], "fortunerift:mythic_rift"
    ), indent=2) + "\n", encoding="utf-8")
    (recipes / "rift_journal.json").write_text(json.dumps(shapeless(
        ["minecraft:book", "minecraft:amethyst_shard"], "fortunerift:rift_journal"
    ), indent=2) + "\n", encoding="utf-8")

    adv = ROOT / "data" / "fortunerift" / "advancement"
    adv.mkdir(parents=True, exist_ok=True)
    advancements = [
        ("feeling_lucky", None, "Feeling Lucky", "Open your first Fortune Rift.", "task"),
        ("what_could_go_wrong", "feeling_lucky", "What Could Go Wrong?", "Open 25 Fortune Rifts.", "task"),
        ("professional_gambler", "what_could_go_wrong", "Professional Gambler", "Open 100 Fortune Rifts.", "challenge"),
        ("bad_luck", "feeling_lucky", "Bad Luck", "Trigger five dangerous events.", "task"),
        ("jackpot", "feeling_lucky", "Jackpot", "Trigger an epic reward.", "goal"),
        ("chosen_by_fortune", "jackpot", "Chosen by Fortune", "Trigger your first mythic event.", "challenge"),
        ("seen_everything", "professional_gambler", "I've Seen Everything", "Discover every standard rift event.", "challenge"),
    ]
    for name, parent, title, description, frame in advancements:
        (adv / f"{name}.json").write_text(json.dumps({
            "parent": f"fortunerift:{parent}" if parent else "minecraft:adventure/root",
            "display": {
                "icon": {"id": "fortunerift:fortune_rift"},
                "title": {"text": title},
                "description": {"text": description},
                "frame": frame,
                "show_toast": True,
                "announce_to_chat": True,
            },
            "criteria": {"code": {"trigger": "minecraft:impossible"}},
        }, indent=2) + "\n", encoding="utf-8")

    tex = ROOT / "assets" / "fortunerift" / "textures" / "block"
    tex.mkdir(parents=True, exist_ok=True)
    item_tex = ROOT / "assets" / "fortunerift" / "textures" / "item"
    item_tex.mkdir(parents=True, exist_ok=True)
    models_block = ROOT / "assets" / "fortunerift" / "models" / "block"
    models_item = ROOT / "assets" / "fortunerift" / "models" / "item"
    states = ROOT / "assets" / "fortunerift" / "blockstates"
    for folder in (models_block, models_item, states):
        folder.mkdir(parents=True, exist_ok=True)
    lang = {}
    for name, color in COLORS.items():
        write_png(tex / f"{name}.png", 16, 64, texture(name, color))
        (tex / f"{name}.png.mcmeta").write_text(json.dumps({"animation": {"frametime": 10, "interpolate": False}}) + "\n", encoding="utf-8")
        (states / f"{name}.json").write_text(json.dumps({"variants": {
            "player_placed=false": {"model": f"fortunerift:block/{name}"},
            "player_placed=true": {"model": f"fortunerift:block/{name}"},
        }}) + "\n", encoding="utf-8")
        (models_block / f"{name}.json").write_text(json.dumps({"parent": "minecraft:block/cube_all", "textures": {"all": f"fortunerift:block/{name}"}}) + "\n", encoding="utf-8")
        (models_item / f"{name}.json").write_text(json.dumps({"parent": f"fortunerift:block/{name}"}) + "\n", encoding="utf-8")
        write_item_definition(name, f"fortunerift:block/{name}")
        pretty = name.replace("_", " ").title().replace("Rift", "Rift")
        lang[f"block.fortunerift.{name}"] = pretty
    for item_name, title, pixels in (
        ("rift_journal", "Rift Journal", (180, 150, 70)),
        ("rift_fragment", "Rift Fragment", (255, 214, 96)),
        ("rift_heart", "Rift Heart", (220, 60, 80)),
    ):
        rows = []
        for y in range(16):
            row = bytearray()
            for x in range(16):
                edge = x == 0 or y == 0 or x == 15 or y == 15
                row += bytes((40, 32, 48, 255) if edge else (*pixels, 255))
            rows.append(bytes(row))
        write_png(item_tex / f"{item_name}.png", 16, 16, rows)
        (models_item / f"{item_name}.json").write_text(json.dumps({
            "parent": "minecraft:item/generated",
            "textures": {"layer0": f"fortunerift:item/{item_name}"},
        }) + "\n", encoding="utf-8")
        lang[f"item.fortunerift.{item_name}"] = title
        write_item_definition(item_name, f"fortunerift:item/{item_name}")
    lang["itemGroup.fortunerift"] = "FortuneRift"
    lang_path = ROOT / "assets" / "fortunerift" / "lang"
    lang_path.mkdir(parents=True, exist_ok=True)
    (lang_path / "en_us.json").write_text(json.dumps(lang, indent=2) + "\n", encoding="utf-8")
    print(f"wrote {len(EVENTS)} events")


if __name__ == "__main__":
    main()
