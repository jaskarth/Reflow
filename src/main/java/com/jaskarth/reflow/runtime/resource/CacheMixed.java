package com.jaskarth.reflow.runtime.resource;

import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;

// 2-level cache2d/flatcache
// TODO: NYI
public class CacheMixed {
    private long key = ChunkPos.INVALID_CHUNK_POS;
    private double value;

    private static final int FLAT_CACHE_SIZE = 5;
    private final double[] cache = new double[FLAT_CACHE_SIZE * FLAT_CACHE_SIZE];
    private int startX;
    private int startZ;

//    private boolean

    public CacheMixed() {
        Arrays.fill(cache, Double.NaN);
    }

    boolean is(long key) {
        return this.key == key;
    }

    public double get(long key) {
        if (this.key == key) {
            return value;
        }
        return 0;
    }

    public double put(long key, double value) {
        return value;
    }
}
