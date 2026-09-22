package io.nightbeam.fortunerift.event;

import java.util.Set;

public final class SelectorRules {
    public final boolean allowDestructive;
    public final boolean allowTerrain;
    public final boolean allowTnt;
    public final boolean allowBoss;
    public final boolean allowStructure;
    public final boolean enableMythic;
    public final boolean fortuneEnabled;
    public final String dimension;
    public final Set<String> cooldownIds;

    public SelectorRules(
            boolean allowDestructive,
            boolean allowTerrain,
            boolean allowTnt,
            boolean allowBoss,
            boolean allowStructure,
            boolean enableMythic,
            boolean fortuneEnabled,
            String dimension,
            Set<String> cooldownIds
    ) {
        this.allowDestructive = allowDestructive;
        this.allowTerrain = allowTerrain;
        this.allowTnt = allowTnt;
        this.allowBoss = allowBoss;
        this.allowStructure = allowStructure;
        this.enableMythic = enableMythic;
        this.fortuneEnabled = fortuneEnabled;
        this.dimension = dimension;
        this.cooldownIds = cooldownIds == null ? Set.of() : Set.copyOf(cooldownIds);
    }

    public static SelectorRules open() {
        return new SelectorRules(true, true, true, true, true, true, true, null, Set.of());
    }

    public SelectorRules withTnt(boolean allowTnt) {
        return new SelectorRules(allowDestructive, allowTerrain, allowTnt, allowBoss, allowStructure, enableMythic, fortuneEnabled, dimension, cooldownIds);
    }

    public SelectorRules withFortune(boolean fortuneEnabled) {
        return new SelectorRules(allowDestructive, allowTerrain, allowTnt, allowBoss, allowStructure, enableMythic, fortuneEnabled, dimension, cooldownIds);
    }
}
