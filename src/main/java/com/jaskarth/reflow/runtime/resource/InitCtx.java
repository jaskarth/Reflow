package com.jaskarth.reflow.runtime.resource;

import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.HashMap;
import java.util.Map;

public class InitCtx {
    private final Map<DensityFunction, CacheFlat> maps = new HashMap<>();

    public InitCtx() {

    }

    public Res flat(DensityFunction key) {
        CacheFlat flat = maps.get(key);
        if (flat != null) {
            return new Res(false, flat);
        }

        flat = new CacheFlat();
        maps.put(key, flat);

        return new Res(true, flat);
    }

    public record Res(boolean init, CacheFlat flat) {}
}
