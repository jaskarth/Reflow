package com.jaskarth.reflow.mixin;

import com.jaskarth.reflow.compile.Compiler;
import com.jaskarth.reflow.compile.SharedCompileData;
import com.jaskarth.reflow.compile.StaticCompileData;
import com.jaskarth.reflow.runtime.ReflowDensityFunction;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Go before fabric api
@Mixin(value = RandomState.class, priority = 800)
public class MixinRandomState {
    @Mutable
    @Shadow
    @Final
    private NoiseRouter router;

    @Mutable
    @Shadow
    @Final
    private Climate.Sampler sampler;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void reflow$compile(NoiseGeneratorSettings noiseGeneratorSettings, HolderGetter holderGetter, long l, CallbackInfo ci) {
        NoiseRouter router = this.router;

        SharedCompileData shared = new SharedCompileData();

        this.router = new NoiseRouter(
                router.barrierNoise(),
                router.fluidLevelFloodednessNoise(),
                router.fluidLevelSpreadNoise(),
                router.lavaNoise(),
                ////
//                router.temperature(),
//                router.vegetation(),
//                router.continents(),
//                router.erosion(),
//                router.depth(),
//                router.ridges(),
                ////
                new ReflowDensityFunction(Compiler.compile("Temp", shared, router.temperature())),
                new ReflowDensityFunction(Compiler.compile("Vegetation", shared, router.vegetation())),
                new ReflowDensityFunction(Compiler.compile("Continents", shared, router.continents())),
                new ReflowDensityFunction(Compiler.compile("Erosion", shared, router.erosion())),
                new ReflowDensityFunction(Compiler.compile("Depth", shared, router.depth())),
                new ReflowDensityFunction(Compiler.compile("Ridges", shared, router.ridges())),
                ////
                router.preliminarySurfaceLevel(),
                router.finalDensity(),
                router.veinToggle(),
                router.veinRidged(),
                router.veinGap()
        );

        StaticCompileData.currentlyCompiling = "";

        this.sampler = new Climate.Sampler(
                this.router.temperature(),
                this.router.vegetation(),
                this.router.continents(),
                this.router.erosion(),
                this.router.depth(),
                this.router.ridges(),
                noiseGeneratorSettings.spawnTarget()
        );
    }
}
