package com.jaskarth.reflow.runtime.resource;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseResource implements Resource {
    private final NormalNoise noise;

    @CompileCalled
    public NoiseResource(NormalNoise noise) {
        this.noise = noise;
    }

    public double sample(double x, double y, double z) {
        return this.noise.getValue(x, y, z);
    }

    @Override
    public Resource remake(DensityFunction.Visitor visitor) {
        return new NoiseResource(noise);
    }
}
