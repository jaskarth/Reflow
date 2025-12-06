package com.jaskarth.reflow.runtime.resource;

import net.minecraft.world.level.levelgen.DensityFunction;

public class ArbitraryResource implements Resource {
    private final DensityFunction func;

    public ArbitraryResource(DensityFunction func) {
        this.func = func;
    }

    public double compute(DensityFunction.FunctionContext ctx) {
        return func.compute(ctx);
    }

    @Override
    public Resource remake(DensityFunction.Visitor visitor) {
        return new ArbitraryResource(func.mapAll(visitor));
    }
}
