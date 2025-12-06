package com.jaskarth.reflow.runtime;

import com.jaskarth.reflow.runtime.resource.ResourceHolder;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ReflowDensityFunction(ReflowFunc func) implements DensityFunction {
    public static final KeyDispatchDataCodec<ReflowDensityFunction> CODEC = KeyDispatchDataCodec.of(
            MapCodec.unit(new ReflowDensityFunction(null))
    );

    @Override
    public double compute(FunctionContext ctx) {
        return func.compute(ctx);
    }

    @Override
    public void fillArray(double[] ds, ContextProvider contextProvider) {
        contextProvider.fillAllDirectly(ds, this);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        ResourceHolder resources = func.resources().remake(visitor);
        return visitor.apply(new ReflowDensityFunction(func.create(resources)));
    }

    @Override
    public double minValue() {
        return -100000000;
    }

    @Override
    public double maxValue() {
        return 100000000;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return null;
    }
}
