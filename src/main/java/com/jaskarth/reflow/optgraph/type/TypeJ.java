package com.jaskarth.reflow.optgraph.type;

public record TypeJ(long min, long max) implements Type {
    public TypeJ {
        if (min > max) {
            throw new IllegalStateException("TypeJ min " + min + " > max " + max);
        }
    }

    public static TypeJ bottom() {
        return new TypeJ(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Override
    public MachType mach() {
        return MachType.LONG;
    }
}
