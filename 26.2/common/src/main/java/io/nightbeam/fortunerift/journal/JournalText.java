package io.nightbeam.fortunerift.journal;

import io.nightbeam.fortunerift.data.PlayerRiftProfile;
import io.nightbeam.fortunerift.event.EventRegistry;
import net.minecraft.network.chat.Component;

public final class JournalText {
    private JournalText() {
    }

    public static Component component(PlayerRiftProfile profile) {
        StringBuilder text = new StringBuilder();
        text.append("Rifts Discovered ").append(profile.opened).append(" / 100\n");
        text.append("Events Witnessed ").append(profile.seen.size()).append('\n');
        EventRegistry.definitions().forEach(event -> {
            if (profile.seen.contains(event.id)) {
                text.append(event.name).append(" ✓\n");
            } else if (!event.secret) {
                text.append(event.name).append('\n');
            } else {
                text.append("???\n");
            }
        });
        return Component.literal(text.toString());
    }
}
