package com.jaskarth.reflow.bench;

import com.jaskarth.reflow.util.ReflowImprovedNoise;
import com.jaskarth.reflow.util.VanillaImprovedNoise;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.phys.Vec3;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.LinuxPerfAsmProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class BenchNoise {
    private static final int SIZE = 1024;

    @Benchmark
    public void testVanilla(Blackhole blackhole, BenchState state) {
        double sum = 0;
        for (int i = 0; i < SIZE; i++) {
            Vec3 p = state.pos[i];
            sum += state.vanilla.noise(p.x, p.y, p.z);
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void testReflow(Blackhole blackhole, BenchState state) {
        double sum = 0;
        for (int i = 0; i < SIZE; i++) {
            Vec3 p = state.pos[i];
            sum += state.reflow.noise(p.x, p.y, p.z);
        }
        blackhole.consume(sum);
    }

    @State(Scope.Benchmark)
    public static class BenchState {
        private final VanillaImprovedNoise vanilla = new VanillaImprovedNoise(RandomSource.create(100));
        private final ReflowImprovedNoise reflow = new ReflowImprovedNoise(RandomSource.create(100));
        private final Vec3[] pos = new Vec3[SIZE];

        @Setup
        public void setup() {
            Random random = new Random(100);
            for (int i = 0; i < SIZE; i++) {
                pos[i] = new Vec3((random.nextDouble() - random.nextDouble()) * 1000,
                        (random.nextDouble() - random.nextDouble()) * 1000,
                        (random.nextDouble() - random.nextDouble()) * 1000);
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        Options opt = new OptionsBuilder()
                .include("BenchNoise")
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                .warmupIterations(5)
                .measurementIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .measurementTime(TimeValue.seconds(1))
                .forks(3)
                // Show assembly hotpoints (needs hsdis)
//                .addProfiler(LinuxPerfAsmProfiler.class)

                // These options require a fastdebug build of the JVM. Use at your own mortal peril.
//                .jvmArgsPrepend("-XX:CompileCommand=TraceAutoVectorization,*ReflowImprovedNoise::*,ALL")
//                .jvmArgsPrepend("-XX:CompileCommand=IGVPrintLevel,*ReflowImprovedNoise::*,3")
//                .jvmArgsPrepend("-XX:CompileCommand=print,*ReflowImprovedNoise::p")
                .build();

        new Runner(opt).run();
    }
}
