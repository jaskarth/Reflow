package com.jaskarth.reflow.runtime;

import com.jaskarth.reflow.runtime.resource.ResourceHolder;
import net.minecraft.world.level.levelgen.DensityFunction;

public interface ReflowFunc {
    double compute(DensityFunction.FunctionContext ctx);

    ReflowFunc create(ResourceHolder holder);

    ResourceHolder resources();
}
