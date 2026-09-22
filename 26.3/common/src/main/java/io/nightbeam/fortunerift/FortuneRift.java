package io.nightbeam.fortunerift;

import io.nightbeam.fortunerift.platform.Services;

public final class FortuneRift {
    private FortuneRift() {
    }

    public static void init() {
        Constants.LOG.info("FortuneRift 1.0.0 on {}", Services.PLATFORM.getPlatformName());
        io.nightbeam.fortunerift.config.FortuneConfig.load();
        io.nightbeam.fortunerift.event.EventRegistry.loadDefaults();
    }
}