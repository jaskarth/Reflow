package com.jaskarth.reflow.runtime.resource;

import com.jaskarth.reflow.util.CacheThread;
import com.jaskarth.reflow.util.ValidationHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheFlat implements Resource {
    private static final int SIZE = 5;
    private final double[] cache = new double[SIZE * SIZE];
    private boolean alive;
    private int startX;
    private int startZ;

    private static final AtomicInteger hits = new AtomicInteger();
    private static final AtomicInteger misses = new AtomicInteger();

    public CacheFlat() {
        Arrays.fill(cache, Double.POSITIVE_INFINITY);
    }

    private int idx(long pos) {
        int x = (ChunkPos.getX(pos) >> 2) - this.startX;
        int z = (ChunkPos.getZ(pos) >> 2) - this.startZ;

        return x * SIZE + z;
    }

    public boolean is(long pos) {
        if (!alive) {
            return false;
        }

        int px = ChunkPos.getX(pos);
        int pz = ChunkPos.getZ(pos);

        if (!((px & 3) == 0 && (pz & 3) == 0)) {
            return false;
        }

        int x = (px >> 2) - this.startX;
        int z = (pz >> 2) - this.startZ;

        if (x >= 0 && z >= 0 && x < SIZE && z < SIZE) {
            double d = cache[x * SIZE + z];
            // Check for NaN
            return Double.isFinite(d);
        }
        return false;
    }

    public double get(long pos) {
//        hits.incrementAndGet();
        int i = idx(pos);
        return cache[i];
    }

    public double put(long pos, double value) {
        if (!alive) {
            return value;
        }
        int px = ChunkPos.getX(pos);
        int pz = ChunkPos.getZ(pos);
        int x = (px >> 2) - this.startX;
        int z = (pz >> 2) - this.startZ;

        // misses.incrementAndGet()

        if (x >= 0 && z >= 0 && x < SIZE && z < SIZE && (px & 3) == 0 && (pz & 3) == 0) {
            cache[x * SIZE + z] = value;
        }
        return value;
    }

    @Override
    public Resource remake(DensityFunction.Visitor visitor) {
        return new CacheFlat();
    }

    @Override
    public void init(InitCtx ctx, int x, int z) {
        ValidationHelper.assertTrue(!alive, "must not reuse cache");
        alive = true;
        startX = x;
        startZ = z;
    }

    static {
//        CacheThread.start("FlatCache", hits, misses);
    }
}
