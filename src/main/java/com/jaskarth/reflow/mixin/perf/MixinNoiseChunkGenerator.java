package com.jaskarth.reflow.mixin.perf;

import com.jaskarth.reflow.mixin.ImposterProtoChunkAccessor;
import com.jaskarth.reflow.mixin.LevelChunkSectionAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class MixinNoiseChunkGenerator extends ChunkGenerator {
    public MixinNoiseChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Shadow
    protected abstract NoiseChunk createNoiseChunk(ChunkAccess chunkAccess, StructureManager structureManager, Blender blender, RandomState randomState);

    @Shadow
    @Final
    private Holder<NoiseGeneratorSettings> settings;

    // Lock sections before and after building surface

    @Inject(method = "buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/blending/Blender;)V",
    at = @At("HEAD"))
    private void reflow$aquireSections(ChunkAccess chunk, WorldGenerationContext worldGenerationContext, RandomState randomState, StructureManager structureManager, BiomeManager biomeManager, Registry<Biome> registry, Blender blender, CallbackInfo ci) {
        for (LevelChunkSection section : chunk.getSections()) {
            section.acquire();
        }
    }

    @Inject(method = "buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/blending/Blender;)V",
            at = @At("RETURN"))
    private void reflow$releaseSections(ChunkAccess chunk, WorldGenerationContext worldGenerationContext, RandomState randomState, StructureManager structureManager, BiomeManager biomeManager, Registry<Biome> registry, Blender blender, CallbackInfo ci) {
        for (LevelChunkSection section : chunk.getSections()) {
            section.release();
        }
    }

    /**
     * @author jaskarth
     *
     * @reason Optimize biome setting. Originally from Rho.
     */
    @Overwrite
    private void doCreateBiomes(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        if (chunk instanceof ImposterProtoChunk imposter) {
            if (!((ImposterProtoChunkAccessor)imposter).isAllowWrites()) {
                // Eject if the imposter doesn't allow writes
                return;
            }

            chunk = imposter.getWrapped();
        }

        NoiseChunk noiseChunk = chunk.getOrCreateNoiseChunk(chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, blender, randomState));
        BiomeResolver biomeResolver = BelowZeroRetrogen.getBiomeResolver(blender.getBiomeResolver(this.biomeSource), chunk);
        Climate.Sampler sampler = noiseChunk.cachedClimateSampler(randomState.router(), this.settings.value().spawnTarget());

        record SectionData(LevelChunkSection section, int minY, PalettedContainer<Holder<Biome>> container) {}

        ChunkPos pos = chunk.getPos();
        int bx = QuartPos.fromBlock(pos.getMinBlockX());
        int bz = QuartPos.fromBlock(pos.getMinBlockZ());

        LevelChunkSection[] sections = chunk.getSections();
        SectionData[] data = new SectionData[sections.length];
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];

            int minY = (i + chunk.getMinSectionY()) << 2;
            PalettedContainer<Holder<Biome>> container = section.getBiomes().recreate();
            data[i] = new SectionData(section, minY, container);
        }

        for (int ax = 0; ax < 4; ax++) {
            for (int az = 0; az < 4; az++) {

                for (SectionData sectionData : data) {
                    int minY = sectionData.minY();
                    PalettedContainer<Holder<Biome>> container = sectionData.container();

                    for (int ay = 0; ay < 4; ay++) {
                        container.getAndSetUnchecked(ax, ay, az, biomeResolver.getNoiseBiome(bx + ax, minY + ay, bz + az, sampler));
                    }
                }
            }
        }

        for (SectionData sec : data) {
            ((LevelChunkSectionAccessor)sec.section()).setBiomes(sec.container());
        }
    }
}
