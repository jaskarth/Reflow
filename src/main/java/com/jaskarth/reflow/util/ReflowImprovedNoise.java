package com.jaskarth.reflow.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

// This is a test noise use for analyzing performance changes. Don't actually use this!
public final class ReflowImprovedNoise {
	private static final float SHIFT_UP_EPSILON = 1.0E-7F;
	private final byte[] p;
	private final int[] p2;
	public final double xo;
	public final double yo;
	public final double zo;

	public ReflowImprovedNoise(RandomSource randomSource) {
		this.xo = randomSource.nextDouble() * 256.0;
		this.yo = randomSource.nextDouble() * 256.0;
		this.zo = randomSource.nextDouble() * 256.0;
		this.p = new byte[256];
		this.p2 = new int[256];

		for (int i = 0; i < 256; i++) {
			this.p[i] = (byte)i;
		}

		for (int i = 0; i < 256; i++) {
			int j = randomSource.nextInt(256 - i);
			byte b = this.p[i];
			this.p[i] = this.p[i + j];
			this.p[i + j] = b;
			this.p2[i + j] = b & 0xFF;
		}
	}

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

        return this.sampleAndLerp(l, m, n, o, p, q);
	}

	private static double gradDot(int[] ary, int i, double d, double e, double f) {
        int ptr = (i & 15) * 3;
        int ia = ary[ptr];
        int ib = ary[ptr + 1];
        int ic = ary[ptr + 2];

        return (ia * d) + (ib * e) + (ic * f);
	}

    private static double gradDotD(double[] ary, int i, double d, double e, double f) {
        int ptr = (i & 15) * 3;
        double da = ary[ptr];
        double db = ary[ptr + 1];
        double dc = ary[ptr + 2];

        return (da * d) + (db * e) + (dc * f);
    }

	private int p(int i) {
//		return this.p[i & 0xFF] & 0xFF;
		return this.p[i & (this.p.length - 1)] & 0xFF;
	}

	private double sampleAndLerp(int i, int j, int k, double d, double e, double f) {
		int l = this.p(i);
		int m = this.p(i + 1);
		int n = this.p(l + j);
		int o = this.p(l + j + 1);
		int p = this.p(m + j);
		int q = this.p(m + j + 1);

        double[] ary = NoiseHelper.GRADFLATD;

        double h = gradDotD(ary, this.p(n + k), d, e, f);
		double r = gradDotD(ary, this.p(p + k), d - 1.0, e, f);
		double s = gradDotD(ary, this.p(o + k), d, e - 1.0, f);
		double t = gradDotD(ary, this.p(q + k), d - 1.0, e - 1.0, f);
		double u = gradDotD(ary, this.p(n + k + 1), d, e, f - 1.0);
		double v = gradDotD(ary, this.p(p + k + 1), d - 1.0, e, f - 1.0);
		double w = gradDotD(ary, this.p(o + k + 1), d, e - 1.0, f - 1.0);
		double x = gradDotD(ary, this.p(q + k + 1), d - 1.0, e - 1.0, f - 1.0);
		double y = Mth.smoothstep(d);
		double z = Mth.smoothstep(e);
		double aa = Mth.smoothstep(f);
		return Mth.lerp3(y, z, aa, h, r, s, t, u, v, w, x);
	}
}
