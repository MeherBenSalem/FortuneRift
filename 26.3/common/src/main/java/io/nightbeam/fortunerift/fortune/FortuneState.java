package io.nightbeam.fortunerift.fortune;

public final class FortuneState {
    public static final int DEFAULT = 50;
    public static final int MIN = 0;
    public static final int MAX = 100;

    private final int value;

    public FortuneState(int value) {
        this.value = clamp(value);
    }

    public int value() {
        return value;
    }

    public static int clamp(int fortune) {
        return Math.max(MIN, Math.min(MAX, fortune));
    }

    public static int adjust(int fortune, boolean positive) {
        int next = positive ? fortune - 8 : fortune + 10;
        return clamp(next);
    }

    public static double positiveMultiplier(int fortune, boolean enabled) {
        if (!enabled) {
            return 1.0;
        }
        return 1.0 + (clamp(fortune) - DEFAULT) / 100.0;
    }

    public static double negativeMultiplier(int fortune, boolean enabled) {
        if (!enabled) {
            return 1.0;
        }
        return 1.0 - (clamp(fortune) - DEFAULT) / 200.0;
    }
}
