package com.jaskarth.reflow.mixin.perf;

import com.jaskarth.reflow.util.BiomeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Function;

@Mixin(SurfaceSystem.class)
public class MixinSurfaceSystem {

    // Use caching biome getter
    @Inject(method = "buildSurface", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/SurfaceRules$Context;<init>(Lnet/minecraft/world/level/levelgen/SurfaceSystem;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/NoiseChunk;Ljava/util/function/Function;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;)V",
            shift = At.Shift.BY, by = 2),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void reflow$changeBiomeGetter(RandomState randomState, BiomeManager biomeManager, Registry<Biome> registry, boolean bl, WorldGenerationContext worldGenerationContext, ChunkAccess chunkAccess, NoiseChunk noiseChunk, SurfaceRules.RuleSource ruleSource, CallbackInfo ci, BlockPos.MutableBlockPos mutableBlockPos, ChunkPos chunkPos, int i, int j, BlockColumn blockColumn, SurfaceRules.Context context) {
        int depth = worldGenerationContext.getGenDepth() >> 2;
        int minY = worldGenerationContext.getMinGenY() >> 2;
        Holder<Biome>[] cache = new Holder[6 * depth * 6];

        context.biomeGetter = b -> BiomeHelper.get(b, chunkAccess, biomeManager, cache, depth, minY);
    }
}
