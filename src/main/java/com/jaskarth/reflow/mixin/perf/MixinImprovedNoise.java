package com.jaskarth.reflow.mixin.perf;

import com.jaskarth.reflow.util.NoiseHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.spongepowered.asm.mixin.*;

@Mixin(ImprovedNoise.class)
public abstract class MixinImprovedNoise {
    @Shadow
    @Final
    public double xo;

    @Shadow
    @Final
    public double yo;

    @Shadow
    @Final
    public double zo;

    @Shadow
    @Final
    private byte[] p;

    /**
     * @author jaskarth
     * @reason Avoid branch in common path
     */
    @Overwrite
    public double noise(double d, double e, double f) {
        double i = d + this.xo;
        double j = e + this.yo;
        double k = f + this.zo;
        int l = Mth.floor(i);
        int m = Mth.floor(j);
        int n = Mth.floor(k);
        double o = i - l;
        double p = j - m;
        double q = k - n;

        return this.sampleAndLerpEx(l, m, n, o, p, q);
    }

    @Unique
    private static double gradDotEx(double[] ary, int i, double d, double e, double f) {
        int ptr = (i & 15) * 3;
        double da = ary[ptr];
        double db = ary[ptr + 1];
        double dc = ary[ptr + 2];

        return (da * d) + (db * e) + (dc * f);
    }

    /**
     * @author jaskarth
     * @reason Do something deranged for better performance
     */
    @Overwrite
    private int p(int i) {
        // Instead of masking by a constant 0xFF, mask by the length instead. This is faster because the length is already
        // in a register, as it is used for the bounds check. Masking by the array length avoids needing to manifest the 0xFF,
        // as the trailing 0xFF is removed because AndI(LoadB, ConI(0xFF)) => LoadUB.
        return this.p[i & (this.p.length - 1)] & 0xFF;
    }

    @Unique
    private double sampleAndLerpEx(int i, int j, int k, double d, double e, double f) {
        int l = this.p(i);
        int m = this.p(i + 1);
        int n = this.p(l + j);
        int o = this.p(l + j + 1);
        int p = this.p(m + j);
        int q = this.p(m + j + 1);

        double[] ary = NoiseHelper.GRADFLATD;

        double h = gradDotEx(ary, this.p(n + k), d, e, f);
        double r = gradDotEx(ary, this.p(p + k), d - 1.0, e, f);
        double s = gradDotEx(ary, this.p(o + k), d, e - 1.0, f);
        double t = gradDotEx(ary, this.p(q + k), d - 1.0, e - 1.0, f);
        double u = gradDotEx(ary, this.p(n + k + 1), d, e, f - 1.0);
        double v = gradDotEx(ary, this.p(p + k + 1), d - 1.0, e, f - 1.0);
        double w = gradDotEx(ary, this.p(o + k + 1), d, e - 1.0, f - 1.0);
        double x = gradDotEx(ary, this.p(q + k + 1), d - 1.0, e - 1.0, f - 1.0);
        double y = Mth.smoothstep(d);
        double z = Mth.smoothstep(e);
        double aa = Mth.smoothstep(f);
        return Mth.lerp3(y, z, aa, h, r, s, t, u, v, w, x);
    }
}
