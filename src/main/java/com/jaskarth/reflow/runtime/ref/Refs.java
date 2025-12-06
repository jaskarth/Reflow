package com.jaskarth.reflow.runtime.ref;

public final class Refs {
    // Builtins
    public static final Ref D = new BuiltinRef("D");
    public static final Ref I = new BuiltinRef("I");
    public static final Ref J = new BuiltinRef("J");
    public static final Ref Z = new BuiltinRef("Z");
    public static final Ref V = new BuiltinRef("V");

    // Minecraft classes
    public static final Ref FUNCTION_CTX = new RemappingRef("net/minecraft/world/level/levelgen/DensityFunction$FunctionContext", "net/minecraft/class_6910$class_6912");
    public static final Ref CHUNKPOS = new RemappingRef("net/minecraft/world/level/ChunkPos", "net/minecraft/class_1923");
    public static final Ref MTH = new RemappingRef("net/minecraft/util/Mth", "net/minecraft/class_3532");

    // Minecraft methods
    public static final Ref CHUNKPOS_ASLONG = new RemappingRef("asLong", "method_8331");
    public static final Ref MTH_CLAMPEDLERP = new RemappingRef("clampedLerp", "method_15390");
    public static final Ref MTH_INVERSELERP = new RemappingRef("inverseLerp", "method_15370");
    public static final Ref CTX_X = new RemappingRef("blockX", "comp_371");
    public static final Ref CTX_Y = new RemappingRef("blockY", "comp_372");
    public static final Ref CTX_Z = new RemappingRef("blockZ", "comp_373");

    // Reflow classes
    public static final Ref CACHE2D = new StrRef("com/jaskarth/reflow/runtime/resource/Cache2d");
    public static final Ref CACHEFLAT = new StrRef("com/jaskarth/reflow/runtime/resource/CacheFlat");
    public static final Ref CACHEFLATWRAP = new StrRef("com/jaskarth/reflow/runtime/resource/CacheFlatWrapper");
    public static final Ref CACHEONCE = new StrRef("com/jaskarth/reflow/runtime/resource/CacheOnce");
    public static final Ref SPLINERES = new StrRef("com/jaskarth/reflow/runtime/resource/SplineResource");
    public static final Ref NOISERES = new StrRef("com/jaskarth/reflow/runtime/resource/NoiseResource");
    public static final Ref ARBRES = new StrRef("com/jaskarth/reflow/runtime/resource/ArbitraryResource");

    public static String desc(Ref r, Ref... p) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Ref ref : p) {
            sb.append(ref.asDescriptor());
        }
        sb.append(")");
        sb.append(r.asDescriptor());

        return sb.toString();
    }
}
