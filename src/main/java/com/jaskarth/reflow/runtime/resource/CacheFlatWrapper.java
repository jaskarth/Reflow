package com.jaskarth.reflow.runtime.resource;

import net.minecraft.world.level.levelgen.DensityFunction;

public class CacheFlatWrapper implements Resource {
    private final DensityFunction key;
    private CacheFlat wrapped;

    public CacheFlatWrapper(DensityFunction key, CacheFlat wrapped) {
        this.key = key;
        this.wrapped = wrapped;
    }

    public boolean is(long pos) {
        return wrapped.is(pos);
    }

    public double get(long pos) {
        return wrapped.get(pos);
    }

    public double put(long pos, double value) {
        return wrapped.put(pos, value);
    }

    @Override
    public Resource remake(DensityFunction.Visitor visitor) {
        // TODO: new cacheflat here for safety?
        return new CacheFlatWrapper(key, null);
    }

    @Override
    public void init(InitCtx ctx, int x, int z) {
        InitCtx.Res flat = ctx.flat(key);
        wrapped = flat.flat();

        if (flat.init()) {
            wrapped.init(ctx, x, z);
        }
    }
}
