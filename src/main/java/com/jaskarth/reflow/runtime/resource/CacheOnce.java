package com.jaskarth.reflow.runtime.resource;

import com.jaskarth.reflow.util.ValidationHelper;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;

public class CacheOnce implements Resource {
    private long counter = -1;
    private double cached = Double.NaN;
    private boolean alive;

    public boolean is(DensityFunction.FunctionContext ctx) {
        if (!alive) {
            return false;
        }

        return ctx instanceof NoiseChunk nc && nc.interpolationCounter == counter;
    }

    public double get() {
        return cached;
    }

    public double put(DensityFunction.FunctionContext ctx, double v) {
        if (ctx instanceof NoiseChunk nc) {
            counter = nc.interpolationCounter;
            cached = v;
        }
        return v;
    }

    @Override
    public Resource remake(DensityFunction.Visitor visitor) {
        return new CacheOnce();
    }

    @Override
    public void init(InitCtx ctx, int x, int z) {
        ValidationHelper.assertTrue(!alive, "must not reuse cache");
        alive = true;
    }
}
