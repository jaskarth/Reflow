package com.jaskarth.reflow.optgraph.type;

public record TypeInt(int min, int max) implements Type {
    public TypeInt {
        if (min > max) {
            throw new IllegalStateException("TypeInt min " + min + " > max " + max);
        }
    }

    @Override
    public Type meet(Type other) {
        if (!(other instanceof TypeInt i)) {
            return Type.BOTTOM;
        }

        if (i.isBottom()) {
            return bottom();
        }

        return new TypeInt(Math.min(min, i.min), Math.max(max, i.max));
    }

    public static TypeInt bool() {
        return new TypeInt(0, 1);
    }

    public static TypeInt bottom() {
        return new TypeInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean isBottom() {
        return min == Integer.MIN_VALUE && max == Integer.MAX_VALUE;
    }

    @Override
    public MachType mach() {
        return MachType.INT;
    }
}
