package com.jaskarth.reflow.tests;

import com.jaskarth.reflow.compile.Compiler;
import com.jaskarth.reflow.compile.SharedCompileData;
import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.compile.DotExporter;
import com.jaskarth.reflow.compile.Parser;
import com.jaskarth.reflow.optgraph.block.Blocks;
import com.jaskarth.reflow.optgraph.opto.*;
import com.jaskarth.reflow.runtime.ReflowFunc;
import com.jaskarth.reflow.util.ValidationHelper;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.CubicSpline;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.concurrent.atomic.AtomicReference;

public class Test {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        DensityFunction func = DensityFunctions.add(
//                DensityFunctions.cache2d(
                DensityFunctions.flatCache(
                        DensityFunctions.mul(
                                DensityFunctions.add(
                                        DensityFunctions.spline(CubicSpline.constant(4)),
                                        DensityFunctions.constant(10)
                                ),
                                DensityFunctions.constant(5)
                        )
//                )
        ),
                DensityFunctions.constant(5));

        HolderLookup.Provider provider = new RegistrySetBuilder()
                .add(Registries.NOISE_SETTINGS, NoiseGeneratorSettings::bootstrap)
                .add(Registries.DENSITY_FUNCTION, NoiseRouterData::bootstrap)
                .add(Registries.NOISE, NoiseData::bootstrap)
                .build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        HolderGetter<NoiseGeneratorSettings> settingsGetter = provider.lookupOrThrow(Registries.NOISE_SETTINGS);
        HolderGetter<NormalNoise.NoiseParameters> noises = provider.lookupOrThrow(Registries.NOISE);

        RandomState state = RandomState.create(settingsGetter.get(NoiseGeneratorSettings.OVERWORLD).get().value(), noises, 100);

//        func = findInsideInterpolation(settingsGetter);
//        func = holderGetter.get(NoiseRouterData.CONTINENTS).get().value();
//        func = settingsGetter.get(NoiseGeneratorSettings.OVERWORLD).get().value().noiseRouter().continents();
        func = state.router().depth();
//        func = buildRedundantBlock();

        ReflowFunc reflow = Compiler.compile("Depth", new SharedCompileData(), state.router().depth());
        SharedCompileData shared = new SharedCompileData();
//        Compiler.compile("Erosion", shared, state.router().erosion());
//        Compiler.compile("Ridges", new SharedCompileData(), state.router().ridges());
//        Compiler.compile("Temperature", new SharedCompileData(), state.router().temperature());
//        Compiler.compile("Vegetation", new SharedCompileData(), state.router().vegetation());
        Compiler.compile("Continents", shared, state.router().continents());
//        if (true) return;

        // Testing code
        for (int i = 0; i < 10; i++) {
            long start = System.currentTimeMillis();
            double sum = 0;
            for (int x = 0; x < 100; x++) {
                for (int z = 0; z < 100; z++) {
                    for (int y = 0; y < 100; y++) {
                        sum += reflow.compute(new DensityFunction.SinglePointContext(x, y, z));
                    }
                }
            }
            long end = System.currentTimeMillis();

            System.out.println("Reflow Took: " + (end - start) + " ms");

            System.out.println(sum);

            start = System.currentTimeMillis();
            sum = 0;
            for (int x = 0; x < 100; x++) {
                for (int z = 0; z < 100; z++) {
                    for (int y = 0; y < 100; y++) {
                        sum += func.compute(new DensityFunction.SinglePointContext(x, y, z));
                    }
                }
            }
            end = System.currentTimeMillis();

            System.out.println("Vanilla Took: " + (end - start) + " ms");

            System.out.println(sum);
        }
    }

    private static DensityFunction findInsideInterpolation(HolderGetter<NoiseGeneratorSettings> getter) {
        AtomicReference<DensityFunction> func = new AtomicReference<>();
        getter.get(NoiseGeneratorSettings.OVERWORLD).get().value().noiseRouter().finalDensity()
                .mapAll(d -> {
                    if (d instanceof DensityFunctions.Marker(DensityFunctions.Marker.Type type, DensityFunction wrapped) && type == DensityFunctions.Marker.Type.Interpolated) {
                        if (wrapped instanceof DensityFunctions.BlendDensity density) {
                            func.set(wrapped);
                        }
                    }
                    return d;
                });

        return func.get();
    }

    private static DensityFunction buildRedundantBlock() {
        return DensityFunctions.add(
                DensityFunctions.mul(
                        DensityFunctions.add(
                                DensityFunctions.spline(CubicSpline.constant(4)),
                                DensityFunctions.constant(10)
                        ),
                        DensityFunctions.constant(5)
                ),
                DensityFunctions.add(
                        DensityFunctions.spline(CubicSpline.constant(4)),
                        DensityFunctions.constant(10)
                )
        );
    }
}
