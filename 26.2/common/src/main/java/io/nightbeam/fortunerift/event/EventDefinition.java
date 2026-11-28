package io.nightbeam.fortunerift.event;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EventDefinition {
    public final String id;
    public final String name;
    public final String description;
    public final int weight;
    public final Rarity rarity;
    public final EventCategory category;
    public final List<String> dimensions;
    public final int minPlayerDistance;
    public final int cooldownTicks;
    public final String script;
    public final String type;
    public final String lootTable;
    public final boolean secret;
    public final boolean positive;
    public final boolean destructive;
    public final boolean terrain;
    public final boolean tnt;
    public final boolean boss;
    public final boolean structure;
    public final Set<RiftVariant> variants;
    public final boolean enabled;
    public final Map<String, String> params;
    public final Set<String> specified;

    private EventDefinition(Builder builder) {
        this.id = builder.id;
        this.name = builder.name == null ? builder.id : builder.name;
        this.description = builder.description == null ? "" : builder.description;
        this.weight = builder.weight;
        this.rarity = builder.rarity;
        this.category = builder.category;
        this.dimensions = List.copyOf(builder.dimensions);
        this.minPlayerDistance = builder.minPlayerDistance;
        this.cooldownTicks = builder.cooldownTicks;
        this.script = builder.script;
        this.type = builder.type;
        this.lootTable = builder.lootTable;
        this.secret = builder.secret;
        this.positive = builder.positive;
        this.destructive = builder.destructive;
        this.terrain = builder.terrain;
        this.tnt = builder.tnt;
        this.boss = builder.boss;
        this.structure = builder.structure;
        this.variants = Collections.unmodifiableSet(EnumSet.copyOf(builder.variants));
        this.enabled = builder.enabled;
        this.params = Map.copyOf(builder.params);
        this.specified = Set.copyOf(builder.specified);
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public Builder toBuilder() {
        Builder builder = builder(id)
                .name(name)
                .description(description)
                .weight(weight)
                .rarity(rarity)
                .category(category)
                .dimensions(dimensions)
                .minPlayerDistance(minPlayerDistance)
                .cooldownTicks(cooldownTicks)
                .script(script)
                .type(type)
                .lootTable(lootTable)
                .secret(secret)
                .positive(positive)
                .destructive(destructive)
                .terrain(terrain)
                .tnt(tnt)
                .boss(boss)
                .structure(structure)
                .enabled(enabled);
        builder.variants.clear();
        builder.variants.addAll(variants);
        builder.params.putAll(params);
        builder.specified.addAll(specified);
        return builder;
    }

    public static final class Builder {
        private final String id;
        private String name;
        private String description = "";
        private int weight = 10;
        private Rarity rarity = Rarity.COMMON;
        private EventCategory category = EventCategory.REWARD;
        private List<String> dimensions = List.of();
        private int minPlayerDistance;
        private int cooldownTicks;
        private String script;
        private String type;
        private String lootTable;
        private boolean secret;
        private boolean positive = true;
        private boolean destructive;
        private boolean terrain;
        private boolean tnt;
        private boolean boss;
        private boolean structure;
        private final Set<RiftVariant> variants = EnumSet.allOf(RiftVariant.class);
        private boolean enabled = true;
        private final Map<String, String> params = new LinkedHashMap<>();
        private final Set<String> specified = new LinkedHashSet<>();

        private Builder(String id) {
            this.id = id;
        }

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder weight(int weight) { this.weight = weight; return this; }
        public Builder rarity(Rarity rarity) { this.rarity = rarity; return this; }
        public Builder category(EventCategory category) { this.category = category; return this; }
        public Builder dimensions(List<String> dimensions) { this.dimensions = dimensions; return this; }
        public Builder minPlayerDistance(int minPlayerDistance) { this.minPlayerDistance = minPlayerDistance; return this; }
        public Builder cooldownTicks(int cooldownTicks) { this.cooldownTicks = cooldownTicks; return this; }
        public Builder script(String script) { this.script = script; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder lootTable(String lootTable) { this.lootTable = lootTable; return this; }
        public Builder secret(boolean secret) { this.secret = secret; return this; }
        public Builder positive(boolean positive) { this.positive = positive; return this; }
        public Builder destructive(boolean destructive) { this.destructive = destructive; return this; }
        public Builder terrain(boolean terrain) { this.terrain = terrain; return this; }
        public Builder tnt(boolean tnt) { this.tnt = tnt; return this; }
        public Builder boss(boolean boss) { this.boss = boss; return this; }
        public Builder structure(boolean structure) { this.structure = structure; return this; }
        public Builder variants(Set<RiftVariant> variants) {
            this.variants.clear();
            this.variants.addAll(variants);
            return this;
        }
        public Builder enabled(boolean enabled) { this.enabled = enabled; return this; }
        public Builder param(String key, String value) { this.params.put(key, value); return this; }
        public Builder specify(String key) { this.specified.add(key); return this; }

        public EventDefinition build() {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Event id is required");
            }
            if (variants.isEmpty()) {
                throw new IllegalArgumentException("Event " + id + " has no variants");
            }
            return new EventDefinition(this);
        }
    }
}
