"""Write the large guaranteed FortuneRift loot table."""
import json
from pathlib import Path

OUT = Path(__file__).resolve().parents[1] / "26.3" / "common" / "src" / "main" / "resources" / "data" / "fortunerift" / "loot_table" / "guaranteed.json"


def entry(item, weight, lo, hi):
    return {
        "type": "minecraft:item",
        "name": item if ":" in item else f"minecraft:{item}",
        "weight": weight,
        "functions": [{
            "function": "minecraft:set_count",
            "count": {"type": "minecraft:uniform", "min": lo, "max": hi},
        }],
    }


def pool(rolls, items):
    return {"rolls": rolls, "entries": [entry(*item) for item in items]}


TABLE = {
    "type": "minecraft:chest",
    "pools": [
        pool(3, [
            ("coal", 8, 8, 24),
            ("charcoal", 4, 8, 16),
            ("iron_ingot", 8, 4, 16),
            ("copper_ingot", 7, 6, 18),
            ("gold_ingot", 7, 3, 12),
            ("redstone", 6, 8, 24),
            ("lapis_lazuli", 6, 6, 18),
            ("emerald", 6, 2, 8),
            ("diamond", 4, 1, 4),
            ("amethyst_shard", 5, 4, 12),
            ("quartz", 5, 4, 16),
            ("experience_bottle", 5, 2, 8),
            ("gunpowder", 4, 4, 12),
            ("string", 4, 4, 12),
            ("leather", 4, 4, 10),
            ("bone", 3, 4, 10),
            ("arrow", 4, 8, 24),
            ("torch", 3, 8, 16),
            ("bread", 4, 6, 16),
            ("cooked_beef", 4, 4, 12),
            ("golden_carrot", 3, 2, 6),
            ("ender_pearl", 3, 1, 4),
        ]),
        pool(2, [
            ("raw_iron", 6, 6, 16),
            ("raw_gold", 6, 4, 12),
            ("raw_copper", 5, 8, 16),
            ("iron_block", 3, 1, 3),
            ("gold_block", 3, 1, 2),
            ("diamond_block", 1, 1, 1),
            ("emerald_block", 2, 1, 2),
            ("lapis_block", 2, 1, 3),
            ("ancient_debris", 2, 1, 2),
            ("netherite_scrap", 2, 1, 2),
            ("netherite_ingot", 1, 1, 1),
            ("blaze_rod", 3, 1, 4),
            ("ghast_tear", 2, 1, 2),
            ("magma_cream", 3, 2, 6),
            ("slime_ball", 3, 4, 8),
            ("prismarine_shard", 3, 4, 10),
            ("prismarine_crystals", 3, 2, 6),
            ("glowstone_dust", 3, 4, 12),
            ("crying_obsidian", 2, 1, 4),
            ("echo_shard", 1, 1, 1),
            ("shulker_shell", 1, 1, 1),
            ("nautilus_shell", 2, 1, 2),
            ("heart_of_the_sea", 1, 1, 1),
            ("golden_apple", 3, 1, 2),
            ("enchanted_golden_apple", 1, 1, 1),
        ]),
        pool(1, [
            ("iron_pickaxe", 6, 1, 1),
            ("iron_axe", 4, 1, 1),
            ("iron_sword", 4, 1, 1),
            ("iron_shovel", 3, 1, 1),
            ("diamond_pickaxe", 3, 1, 1),
            ("diamond_axe", 2, 1, 1),
            ("diamond_sword", 2, 1, 1),
            ("bow", 4, 1, 1),
            ("crossbow", 3, 1, 1),
            ("shield", 3, 1, 1),
            ("fishing_rod", 2, 1, 1),
            ("shears", 2, 1, 1),
            ("flint_and_steel", 2, 1, 1),
            ("spyglass", 2, 1, 1),
            ("clock", 2, 1, 1),
            ("compass", 2, 1, 1),
            ("recovery_compass", 1, 1, 1),
            ("saddle", 3, 1, 1),
            ("name_tag", 3, 1, 1),
            ("lead", 3, 1, 2),
            ("iron_helmet", 3, 1, 1),
            ("iron_chestplate", 3, 1, 1),
            ("iron_leggings", 3, 1, 1),
            ("iron_boots", 3, 1, 1),
            ("diamond_helmet", 2, 1, 1),
            ("diamond_chestplate", 2, 1, 1),
            ("diamond_leggings", 2, 1, 1),
            ("diamond_boots", 2, 1, 1),
            ("golden_helmet", 2, 1, 1),
            ("totem_of_undying", 1, 1, 1),
            ("elytra", 1, 1, 1),
            ("trident", 1, 1, 1),
            ("mace", 1, 1, 1),
        ]),
        pool(1, [
            ("music_disc_13", 2, 1, 1),
            ("music_disc_cat", 2, 1, 1),
            ("music_disc_blocks", 2, 1, 1),
            ("music_disc_chirp", 2, 1, 1),
            ("music_disc_far", 2, 1, 1),
            ("music_disc_mall", 2, 1, 1),
            ("music_disc_mellohi", 2, 1, 1),
            ("music_disc_stal", 2, 1, 1),
            ("music_disc_strad", 2, 1, 1),
            ("music_disc_ward", 2, 1, 1),
            ("music_disc_wait", 2, 1, 1),
            ("music_disc_otherside", 1, 1, 1),
            ("music_disc_pigstep", 1, 1, 1),
            ("music_disc_relic", 1, 1, 1),
            ("disc_fragment_5", 2, 1, 2),
            ("sniffer_egg", 1, 1, 1),
            ("dragon_breath", 2, 1, 2),
            ("nether_star", 1, 1, 1),
            ("beacon", 1, 1, 1),
            ("conduit", 1, 1, 1),
            ("enchanted_golden_apple", 2, 1, 1),
            ("netherite_upgrade_smithing_template", 1, 1, 1),
            ("silence_armor_trim_smithing_template", 1, 1, 1),
            ("flow_armor_trim_smithing_template", 1, 1, 1),
            ("bolt_armor_trim_smithing_template", 1, 1, 1),
            ("experience_bottle", 6, 4, 12),
            ("emerald", 6, 4, 12),
            ("diamond", 6, 2, 6),
        ]),
    ],
}


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(TABLE, indent=2) + "\n", encoding="utf-8")
    kinds = sum(len(pool["entries"]) for pool in TABLE["pools"])
    print(f"wrote {OUT} with {kinds} entries")


if __name__ == "__main__":
    main()
