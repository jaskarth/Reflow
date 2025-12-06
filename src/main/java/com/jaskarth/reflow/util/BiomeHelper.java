package com.jaskarth.reflow.util;

import com.jaskarth.reflow.mixin.BiomeManagerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;

public class BiomeHelper {
    public static Holder<Biome> get(BlockPos pos, ChunkAccess chunk, BiomeManager biomes, Holder<Biome>[] cache, int ySize, int minY) {
        int i = pos.getX() - 2;
        int j = pos.getY() - 2;
        int k = pos.getZ() - 2;
        int l = i >> 2;
        int m = j >> 2;
        int n = k >> 2;
        double d = (i & 3) * 0.25;
        double e = (j & 3) * 0.25;
        double f = (k & 3) * 0.25;
        int o = 0;
        double g = Double.POSITIVE_INFINITY;

        for (int p = 0; p < 8; p++) {
            boolean bl = (p & 4) == 0;
            boolean bl2 = (p & 2) == 0;
            boolean bl3 = (p & 1) == 0;
            int q = bl ? l : l + 1;
            int r = bl2 ? m : m + 1;
            int s = bl3 ? n : n + 1;
            double h = bl ? d : d - 1.0;
            double t = bl2 ? e : e - 1.0;
            double u = bl3 ? f : f - 1.0;
            double v = getFiddledDistance(((BiomeManagerAccessor)biomes).getBiomeZoomSeed(), q, r, s, h, t, u);
            if (g > v) {
                o = p;
                g = v;
            }
        }

        int bx = (o & 4) == 0 ? l : l + 1;
        int by = (o & 2) == 0 ? m : m + 1;
        int bz = (o & 1) == 0 ? n : n + 1;

        int cx = bx >> 2;
        int cz = bz >> 2;
        ChunkPos p = chunk.getPos();

        // [-1, 4] -> [0, 5]
        int cpx = bx - (p.x << 2) + 1;
        int cpz = bz - (p.z << 2) + 1;
        int cpy = by - minY;

        int cacheIdx = -1;
        if (cpx >= 0 && cpx <= 5 && cpz >= 0 && cpz <= 5 && cpy >= 0 && cpy < ySize) {
            cacheIdx = (cpx * (6 * ySize)) + (cpz * ySize) + cpy;
            Holder<Biome> b = cache[cacheIdx];
            if (b != null) {
                return b;
            }
        }

        //
//        if (p.x == 0 && p.z == 0) {
//            System.out.println(pos + " -> (" + bx + ", " + by + ", " + bz + ")");
//        }
        //

        if (cx == p.x && cz == p.z) {
            Holder<Biome> res = chunk.getNoiseBiome(bx, by, bz);
            if (cacheIdx >= 0) {
                cache[cacheIdx] = res;
            }
            return res;
        }

        Holder<Biome> res = ((BiomeManagerAccessor)biomes).getNoiseBiomeSource().getNoiseBiome(bx, by, bz);
        if (cacheIdx >= 0) {
            cache[cacheIdx] = res;
        }

        return res;
    }

    public static double getFiddledDistance(long l, int i, int j, int k, double d, double e, double f) {
        long m = LinearCongruentialGenerator.next(l, i);
        m = LinearCongruentialGenerator.next(m, j);
        m = LinearCongruentialGenerator.next(m, k);
        m = LinearCongruentialGenerator.next(m, i);
        m = LinearCongruentialGenerator.next(m, j);
        m = LinearCongruentialGenerator.next(m, k);
        double g = getFiddle(m);
        m = LinearCongruentialGenerator.next(m, l);
        double h = getFiddle(m);
        m = LinearCongruentialGenerator.next(m, l);
        double n = getFiddle(m);
        return Mth.square(f + n) + Mth.square(e + h) + Mth.square(d + g);
    }

    private static final double DIST_FACTOR = (1.0 / 1024.0D) * 0.9;

    private static double getFiddle(long seed) {
        double d = (double) ((int) (seed >> 24) & 1023) * DIST_FACTOR;
        return d - 0.45;
    }
}
