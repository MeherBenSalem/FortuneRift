package io.nightbeam.fortunerift.event.json;

import io.nightbeam.fortunerift.event.EventCategory;
import io.nightbeam.fortunerift.event.EventDefinition;
import io.nightbeam.fortunerift.event.Rarity;
import io.nightbeam.fortunerift.event.RiftVariant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EventJsonLoader {
    private EventJsonLoader() {
    }

    public static EventDefinition parse(String json) {
        return fromMap(parseObject(json));
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object value = new Parser(json).parseValue();
        if (!(value instanceof Map<?, ?> object)) {
            throw new IllegalArgumentException("JSON must be an object");
        }
        return (Map<String, Object>) object;
    }

    public static EventDefinition merge(EventDefinition builtin, EventDefinition override) {
        EventDefinition.Builder builder = builtin.toBuilder().script(builtin.script);
        Set<String> keys = override.specified;
        boolean full = keys.isEmpty();
        if (full || keys.contains("name")) builder.name(override.name);
        if (full || keys.contains("description")) builder.description(override.description);
        if (full || keys.contains("weight")) builder.weight(override.weight);
        if (full || keys.contains("rarity")) builder.rarity(override.rarity);
        if (full || keys.contains("category")) builder.category(override.category);
        if (full || keys.contains("dimensions")) builder.dimensions(override.dimensions);
        if (full || keys.contains("min_player_distance")) builder.minPlayerDistance(override.minPlayerDistance);
        if (full || keys.contains("cooldown_ticks")) builder.cooldownTicks(override.cooldownTicks);
        if (full || keys.contains("type")) builder.type(override.type);
        if (full || keys.contains("loot_table")) builder.lootTable(override.lootTable);
        if (full || keys.contains("secret")) builder.secret(override.secret);
        if (full || keys.contains("positive")) builder.positive(override.positive);
        if (full || keys.contains("destructive")) builder.destructive(override.destructive);
        if (full || keys.contains("terrain")) builder.terrain(override.terrain);
        if (full || keys.contains("tnt")) builder.tnt(override.tnt);
        if (full || keys.contains("boss")) builder.boss(override.boss);
        if (full || keys.contains("structure")) builder.structure(override.structure);
        if (full || keys.contains("variants")) builder.variants(override.variants);
        if (full || keys.contains("enabled")) builder.enabled(override.enabled);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static EventDefinition fromMap(Map<?, ?> raw) {
        Map<String, Object> map = (Map<String, Object>) raw;
        String id = string(map, "id");
        EventDefinition.Builder builder = EventDefinition.builder(id)
                .name(stringOr(map, "name", id))
                .description(stringOr(map, "description", ""))
                .weight(intOr(map, "weight", 10))
                .rarity(Rarity.parse(stringOr(map, "rarity", "COMMON")))
                .category(EventCategory.parse(stringOr(map, "category", "REWARD")))
                .dimensions(stringList(map.get("dimensions")))
                .minPlayerDistance(intOr(map, "min_player_distance", 0))
                .cooldownTicks(intOr(map, "cooldown_ticks", 0))
                .script(nullable(map, "script"))
                .type(nullable(map, "type"))
                .lootTable(nullable(map, "loot_table"))
                .secret(boolOr(map, "secret", false))
                .positive(boolOr(map, "positive", true))
                .destructive(boolOr(map, "destructive", false))
                .terrain(boolOr(map, "terrain", false))
                .tnt(boolOr(map, "tnt", false))
                .boss(boolOr(map, "boss", false))
                .structure(boolOr(map, "structure", false))
                .enabled(boolOr(map, "enabled", true));
        if (map.get("variants") instanceof List<?> list && !list.isEmpty()) {
            Set<RiftVariant> variants = new java.util.LinkedHashSet<>();
            for (Object entry : list) {
                variants.add(RiftVariant.parse(String.valueOf(entry)));
            }
            builder.variants(variants);
        }
        Object behavior = map.get("behavior");
        if (behavior != null) {
            builder.param("behavior", String.valueOf(behavior));
        }
        for (String key : map.keySet()) {
            builder.specify(key);
        }
        return builder.build();
    }

    private static String string(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing " + key);
        }
        return String.valueOf(value);
    }

    private static String stringOr(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static String nullable(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static int intOr(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private static boolean boolOr(Map<String, Object> map, String key, boolean fallback) {
        Object value = map.get(key);
        if (value == null) {
            return fallback;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Object entry : list) {
            out.add(String.valueOf(entry));
        }
        return out;
    }

    static final class Parser {
        private final String text;
        private int index;

        Parser(String text) {
            this.text = text;
        }

        Object parseValue() {
            skip();
            if (index >= text.length()) {
                throw new IllegalArgumentException("Empty JSON");
            }
            char c = text.charAt(index);
            if (c == '{') {
                return parseObject();
            }
            if (c == '[') {
                return parseArray();
            }
            if (c == '"') {
                return parseString();
            }
            if (c == 't' || c == 'f') {
                return parseBoolean();
            }
            if (c == 'n') {
                expect("null");
                return null;
            }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            expect("{");
            Map<String, Object> map = new LinkedHashMap<>();
            skip();
            if (peek('}')) {
                index++;
                return map;
            }
            while (true) {
                skip();
                String key = parseString();
                skip();
                expect(":");
                map.put(key, parseValue());
                skip();
                if (peek('}')) {
                    index++;
                    return map;
                }
                expect(",");
            }
        }

        private List<Object> parseArray() {
            expect("[");
            List<Object> list = new ArrayList<>();
            skip();
            if (peek(']')) {
                index++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skip();
                if (peek(']')) {
                    index++;
                    return list;
                }
                expect(",");
            }
        }

        private String parseString() {
            expect("\"");
            StringBuilder builder = new StringBuilder();
            while (index < text.length()) {
                char c = text.charAt(index++);
                if (c == '"') {
                    return builder.toString();
                }
                if (c == '\\') {
                    char escaped = text.charAt(index++);
                    builder.append(switch (escaped) {
                        case '"', '\\', '/' -> escaped;
                        case 'n' -> '\n';
                        case 't' -> '\t';
                        case 'r' -> '\r';
                        default -> escaped;
                    });
                } else {
                    builder.append(c);
                }
            }
            throw new IllegalArgumentException("Unclosed string");
        }

        private Boolean parseBoolean() {
            if (text.startsWith("true", index)) {
                index += 4;
                return true;
            }
            if (text.startsWith("false", index)) {
                index += 5;
                return false;
            }
            throw new IllegalArgumentException("Expected boolean at " + index);
        }

        private Number parseNumber() {
            int start = index;
            if (peek('-')) {
                index++;
            }
            while (index < text.length() && (Character.isDigit(text.charAt(index)) || text.charAt(index) == '.')) {
                index++;
            }
            String number = text.substring(start, index);
            if (number.contains(".")) {
                return Double.parseDouble(number);
            }
            return Integer.parseInt(number);
        }

        private void skip() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        private boolean peek(char c) {
            return index < text.length() && text.charAt(index) == c;
        }

        private void expect(String token) {
            skip();
            if (!text.startsWith(token, index)) {
                throw new IllegalArgumentException("Expected " + token + " at " + index);
            }
            index += token.length();
        }
    }
}
