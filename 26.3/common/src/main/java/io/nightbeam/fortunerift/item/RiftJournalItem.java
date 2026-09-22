package io.nightbeam.fortunerift.item;

import io.nightbeam.fortunerift.data.PlayerRiftProfile;
import io.nightbeam.fortunerift.data.ProfileStore;
import io.nightbeam.fortunerift.journal.JournalText;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class RiftJournalItem extends Item {
    public RiftJournalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server) {
            PlayerRiftProfile profile = ProfileStore.get(server.level().getServer(), server.getUUID());
            server.sendSystemMessage(JournalText.component(profile));
        }
        return InteractionResult.SUCCESS;
    }
}
