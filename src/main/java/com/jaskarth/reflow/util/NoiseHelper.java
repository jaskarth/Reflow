package com.jaskarth.reflow.util;

public class NoiseHelper {
    public static final int[] GRADFLAT = new int[]{
            1, 1, 0,
            -1, 1, 0,
            1, -1, 0,
            -1, -1, 0,
            1, 0, 1,
            -1, 0, 1,
            1, 0, -1,
            -1, 0, -1,
            0, 1, 1,
            0, -1, 1,
            0, 1, -1,
            0, -1, -1,
            1, 1, 0,
            0, -1, 1,
            -1, 1, 0,
            0, -1, -1
    };

    public static final double[] GRADFLATD = new double[]{
            1, 1, 0,
            -1, 1, 0,
            1, -1, 0,
            -1, -1, 0,
            1, 0, 1,
            -1, 0, 1,
            1, 0, -1,
            -1, 0, -1,
            0, 1, 1,
            0, -1, 1,
            0, 1, -1,
            0, -1, -1,
            1, 1, 0,
            0, -1, 1,
            -1, 1, 0,
            0, -1, -1
    };

    public static final int[][] GRADIENT = new int[][]{
            {1, 1, 0},
            {-1, 1, 0},
            {1, -1, 0},
            {-1, -1, 0},
            {1, 0, 1},
            {-1, 0, 1},
            {1, 0, -1},
            {-1, 0, -1},
            {0, 1, 1},
            {0, -1, 1},
            {0, 1, -1},
            {0, -1, -1},
            {1, 1, 0},
            {0, -1, 1},
            {-1, 1, 0},
            {0, -1, -1}
    };

    public static double dot(int[] is, double d, double e, double f) {
        return is[0] * d + is[1] * e + is[2] * f;
    }
}
