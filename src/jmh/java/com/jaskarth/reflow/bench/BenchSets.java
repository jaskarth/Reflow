package com.jaskarth.reflow.bench;

import com.jaskarth.reflow.util.ReflowImprovedNoise;
import com.jaskarth.reflow.util.VanillaImprovedNoise;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BenchSets {
    private static final int SIZE = 32;

    @Benchmark
    public void testJdk(Blackhole blackhole, BenchState state) {
        for (int i = 0; i < SIZE; i++) {
            blackhole.consume(state.jdk.contains(state.values[i]));
        }
    }

    @Benchmark
    public void testImmutable(Blackhole blackhole, BenchState state) {
        for (int i = 0; i < SIZE; i++) {
            blackhole.consume(state.immutable.contains(state.values[i]));
        }
    }

    @Benchmark
    public void testFastutilObj(Blackhole blackhole, BenchState state) {
        for (int i = 0; i < SIZE; i++) {
            blackhole.consume(state.fastutilObj.contains(state.values[i]));
        }
    }

    @Benchmark
    public void testFastutilRef(Blackhole blackhole, BenchState state) {
        for (int i = 0; i < SIZE; i++) {
            blackhole.consume(state.fastutilRef.contains(state.values[i]));
        }
    }

    @State(Scope.Benchmark)
    public static class BenchState {
        private final Set<Object> jdk = new HashSet<>();
        private Set<Object> immutable;
        private final ObjectOpenHashSet<Object> fastutilObj = new ObjectOpenHashSet<>();
        private final ReferenceOpenHashSet<Object> fastutilRef = new ReferenceOpenHashSet<>();
        private final Object[] values = new Object[SIZE];

        @Setup
        public void setup() {
            Random random = new Random(100);

            jdk.clear();
            fastutilObj.clear();
            fastutilRef.clear();

            List<Object> temp = new ArrayList<>();
            for (int i = 0; i < SIZE; i++) {
                Object o = new Object();
                if (random.nextBoolean()) {
                    jdk.add(o);
                    fastutilObj.add(o);
                    fastutilRef.add(o);
                    temp.add(o);
                }
                values[i] = o;
            }

            immutable = Set.of(temp.toArray());
        }
    }

    public static void main(String[] args) throws Throwable {
        Options opt = new OptionsBuilder()
                .include("BenchSets")
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                .warmupIterations(5)
                .measurementIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .measurementTime(TimeValue.seconds(1))
                .forks(3)
                // Show assembly hotpoints (needs hsdis)
//                .addProfiler(LinuxPerfAsmProfiler.class)
                .build();

        new Runner(opt).run();
    }
}
