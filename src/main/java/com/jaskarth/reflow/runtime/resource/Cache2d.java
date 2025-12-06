package com.jaskarth.reflow.runtime.resource;

import com.jaskarth.reflow.util.CacheThread;
import com.jaskarth.reflow.util.ValidationHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.concurrent.atomic.AtomicInteger;

public final class Cache2d implements Resource {
    private long key = ChunkPos.INVALID_CHUNK_POS;
    private double value;
    private boolean alive = false;
    // used for debugging
    private int startX;
    private int startZ;
    private static final AtomicInteger hits = new AtomicInteger();
    private static final AtomicInteger misses = new AtomicInteger();

    public boolean is(long key) {
        if (startX == 0 && startZ == 0) {
//            System.out.println(this.key + " -> " + key);
        }
        return alive && this.key == key;
    }

    public double get() {
//        hits.incrementAndGet();
        return value;
    }

    public double put(long key, double value) {
        this.key = key;
        this.value = value;
//        misses.incrementAndGet();

        return value;
    }

    @Override
    public Resource remake(DensityFunction.Visitor visitor) {
        return new Cache2d();
    }

    @Override
    public void init(InitCtx ctx, int x, int z) {
        // Only use cache2d in chunk gen context
        ValidationHelper.assertTrue(!alive, "must not reuse cache");
        alive = true;
        startX = x;
        startZ = z;
    }

    static {
//        CacheThread.start("Cache2d", hits, misses);
    }
}
