package io.nightbeam.fortunerift.data;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PlayerRiftProfile {
    public int fortune = 50;
    public int opened;
    public int negative;
    public final Set<String> seen = new LinkedHashSet<>();

    public String save() {
        return fortune + "\n" + opened + "\n" + negative + "\n" + String.join(",", seen);
    }

    public static PlayerRiftProfile load(String raw) {
        PlayerRiftProfile profile = new PlayerRiftProfile();
        String[] lines = raw.split("\n", -1);
        if (lines.length > 0 && !lines[0].isBlank()) {
            profile.fortune = Integer.parseInt(lines[0].trim());
        }
        if (lines.length > 1 && !lines[1].isBlank()) {
            profile.opened = Integer.parseInt(lines[1].trim());
        }
        if (lines.length > 2 && !lines[2].isBlank()) {
            profile.negative = Integer.parseInt(lines[2].trim());
        }
        if (lines.length > 3 && !lines[3].isBlank()) {
            for (String id : lines[3].split(",")) {
                if (!id.isBlank()) {
                    profile.seen.add(id.trim());
                }
            }
        }
        return profile;
    }
}
