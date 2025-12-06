package com.jaskarth.reflow.compile;

import com.jaskarth.reflow.runtime.resource.CacheFlat;
import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.HashMap;
import java.util.Map;

public class SharedCompileData {
    private static final Map<DensityFunction, CacheFlat> MAP = new HashMap<>();

    public CacheFlat getOrMake(DensityFunction func) {
        CacheFlat cache = MAP.get(func);
        if (cache != null) {
            return cache;
        }

        cache = new CacheFlat();
        MAP.put(func, cache);
        return cache;
    }
}
