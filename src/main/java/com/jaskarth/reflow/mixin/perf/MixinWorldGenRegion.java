package com.jaskarth.reflow.mixin.perf;

import com.jaskarth.reflow.util.BiomeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldGenRegion.class)
public abstract class MixinWorldGenRegion implements WorldGenLevel {
    @Shadow
    @Final
    private long seed;

    @Shadow
    @Final
    private StaticCache2D<GenerationChunkHolder> cache;
    @Shadow
    @Final
    private ChunkAccess center;
    @Shadow
    @Final
    private ChunkStep generatingStep;
    @Unique
    private long reflow$obfSeed;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void reflow$capSeed(ServerLevel serverLevel, StaticCache2D staticCache2D, ChunkStep chunkStep, ChunkAccess chunkAccess, CallbackInfo ci) {
        reflow$obfSeed = BiomeManager.obfuscateSeed(this.seed);
    }

    // Try to get biome from chunk instead of globally
    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
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
            double v = BiomeHelper.getFiddledDistance(this.reflow$obfSeed, q, r, s, h, t, u);
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

        int dist = this.center.getPos().getChessboardDistance(cx, cz);
        ChunkStatus hereStatus = dist >= this.generatingStep.directDependencies().size() ? null : this.generatingStep.directDependencies().get(dist);
        if (hereStatus != null) {
            GenerationChunkHolder holder = this.cache.get(cx, cz);
            if (ChunkStatus.BIOMES.isOrBefore(hereStatus)) {
                ChunkAccess chunk = holder.getChunkIfPresentUnchecked(hereStatus);
                return chunk.getNoiseBiome(bx, by, bz);
            }
        }

        return this.getUncachedNoiseBiome(bx, by, bz);
    }
}
