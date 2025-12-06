package com.jaskarth.reflow.runtime.resource;

import net.minecraft.world.level.levelgen.DensityFunction;

public interface Resource {
    Resource remake(DensityFunction.Visitor visitor);

    default void init(InitCtx ctx, int x, int z) {

    }
}
