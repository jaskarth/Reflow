package com.jaskarth.reflow.mixin.perf;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PerlinNoise.class)
public abstract class MixinPerlinNoise {
    @Shadow
    @Final
    private double lowestFreqInputFactor;

    @Shadow
    @Final
    private double lowestFreqValueFactor;

    @Shadow
    @Final
    private ImprovedNoise[] noiseLevels;

    @Shadow
    public static double wrap(double d) {
        return 0;
    }

    @Shadow
    @Final
    private DoubleList amplitudes;

    /**
     * @author jaskarth
     *
     * @reason Avoid branch in common path, use fewer variables
     */
    @Overwrite
    @Deprecated
    public double getValue(double d, double e, double f) {
        double i = 0.0;
        double j = this.lowestFreqInputFactor;
        double k = this.lowestFreqValueFactor;

        for (int l = 0; l < this.noiseLevels.length; l++) {
            ImprovedNoise improvedNoise = this.noiseLevels[l];
            if (improvedNoise != null) {
                double m = improvedNoise.noise(wrap(d * j), wrap(e * j), wrap(f * j));
                i += this.amplitudes.getDouble(l) * m * k;
            }

            j *= 2.0;
            k *= 0.5;
        }

        return i;
    }
}
