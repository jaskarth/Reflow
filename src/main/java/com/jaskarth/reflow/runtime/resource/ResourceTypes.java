package com.jaskarth.reflow.runtime.resource;

import com.jaskarth.reflow.runtime.ref.Refs;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ResourceTypes {
    public static final ResourceType CACHE_2D = new ResourceType("cache2d", Refs.CACHE2D, Cache2d::new);
    public static final ResourceType CACHE_FLAT = new ResourceType("cacheFlat", Refs.CACHEFLAT, CacheFlat::new);
    public static final ResourceType CACHE_ONCE = new ResourceType("cacheOnce", Refs.CACHEONCE, CacheOnce::new);

    public static ResourceType flat(CacheFlat flat) {
        return new ResourceType("cacheFlat", Refs.CACHEFLAT, () -> flat);
    }

    public static ResourceType flatWrapper(CacheFlatWrapper cache) {
        return new ResourceType("cacheFlatWrap", Refs.CACHEFLATWRAP, () -> cache);
    }

    public static ResourceType spline(CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline) {
        return new ResourceType("spline", Refs.SPLINERES, () -> new SplineResource(spline));
    }

    public static ResourceType noise(NormalNoise noise) {
        return new ResourceType("noise", Refs.NOISERES, () -> new NoiseResource(noise));
    }

    public static ResourceType arbitrary(DensityFunction func) {
        return new ResourceType("arbitrary", Refs.ARBRES, () -> new ArbitraryResource(func));
    }
}
