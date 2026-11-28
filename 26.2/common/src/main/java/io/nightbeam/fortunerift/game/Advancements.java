package io.nightbeam.fortunerift.game;

import io.nightbeam.fortunerift.data.PlayerRiftProfile;
import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.Rarity;
import io.nightbeam.fortunerift.registry.RiftIds;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

public final class Advancements {
    private Advancements() {
    }

    public static void grant(ServerPlayer player, PlayerRiftProfile profile, EventDefinition event) {
        award(player, "feeling_lucky");
        if (profile.opened >= 25) {
            award(player, "what_could_go_wrong");
        }
        if (profile.opened >= 100) {
            award(player, "professional_gambler");
        }
        if (profile.negative >= 5) {
            award(player, "bad_luck");
        }
        if (event.rarity == Rarity.EPIC) {
            award(player, "jackpot");
        }
        if (event.rarity == Rarity.MYTHIC) {
            award(player, "chosen_by_fortune");
        }
        if (profile.seen.size() >= 90) {
            award(player, "seen_everything");
        }
    }

    private static void award(ServerPlayer player, String path) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(RiftIds.id(path));
        if (holder != null) {
            player.getAdvancements().award(holder, "code");
        }
    }
}
