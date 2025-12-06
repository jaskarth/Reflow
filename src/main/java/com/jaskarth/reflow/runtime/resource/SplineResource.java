package com.jaskarth.reflow.runtime.resource;

import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class SplineResource implements Resource {
    private final CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline;

    public SplineResource(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline) {
        this.spline = spline;
    }

    @CompileCalled
    public double sample(DensityFunction.FunctionContext ctx) {
        return spline.apply(new DensityFunctions.Spline.Point(ctx));
    }

    public Resource remake(DensityFunction.Visitor visitor) {
        return new SplineResource(spline.mapAll(c -> c.mapAll(visitor)));
    }
}
