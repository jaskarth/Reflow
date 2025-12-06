package com.jaskarth.reflow.runtime.ref;

import net.fabricmc.loader.api.FabricLoader;

public record RemappingRef(String dev, String prod) implements Ref {
    private static boolean shouldRemap;

    @Override
    public String get() {
        return shouldRemap ? prod : dev;
    }

    static {
        try {
            shouldRemap = !FabricLoader.getInstance().isDevelopmentEnvironment();
        } catch (Exception e) {
            // Testing without minecraft
            shouldRemap = false;
        }
    }
}
