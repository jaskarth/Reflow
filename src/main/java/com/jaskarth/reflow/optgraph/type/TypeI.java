package com.jaskarth.reflow.optgraph.type;

public record TypeI(int min, int max) implements Type {
    public TypeI {
        if (min > max) {
            throw new IllegalStateException("TypeI min " + min + " > max " + max);
        }
    }

    public static TypeI bottom() {
        return new TypeI(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public MachType mach() {
        return MachType.INT;
    }
}
