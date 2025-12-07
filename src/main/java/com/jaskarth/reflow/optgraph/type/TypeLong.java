package com.jaskarth.reflow.optgraph.type;

public record TypeLong(long min, long max) implements Type {
    public TypeLong {
        if (min > max) {
            throw new IllegalStateException("TypeLong min " + min + " > max " + max);
        }
    }

    @Override
    public Type meet(Type other) {
        if (!(other instanceof TypeLong l)) {
            return Type.BOTTOM;
        }

        if (l.isBottom()) {
            return bottom();
        }

        return new TypeLong(Math.min(min, l.min), Math.max(max, l.max));
    }

    public static TypeLong bottom() {
        return new TypeLong(Long.MIN_VALUE, Long.MAX_VALUE);
    }

    @Override
    public boolean isBottom() {
        return min == Long.MIN_VALUE && max == Long.MAX_VALUE;
    }

    @Override
    public MachType mach() {
        return MachType.LONG;
    }
}
