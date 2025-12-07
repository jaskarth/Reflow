package com.jaskarth.reflow.optgraph.type;

import com.jaskarth.reflow.optgraph.nodes.ImmDNode;
import com.jaskarth.reflow.optgraph.nodes.Node;
import org.jetbrains.annotations.Nullable;

public record TypeDouble(double min, double max) implements Type {
    public static final TypeDouble FULL_RANGE = new TypeDouble(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

    public TypeDouble {
        if (min > max) {
            throw new IllegalStateException("TypeDouble min " + min + " > max " + max);
        }
    }

    @Override
    public MachType mach() {
        return MachType.DOUBLE;
    }

    @Override
    public @Nullable Node constant() {
        if (min == max) {
            return new ImmDNode(min);
        }
        return null;
    }

    @Override
    public Type meet(Type other) {
        if (!(other instanceof TypeDouble d)) {
            return Type.BOTTOM;
        }

        if (d.isBottom()) {
            return FULL_RANGE;
        }

        return new TypeDouble(Math.min(min, d.min), Math.max(max, d.max));
    }

    public boolean isBottom() {
        return Double.isInfinite(min) || Double.isInfinite(max);
    }

    public static Type bottom() {
        return FULL_RANGE;
    }

    public static Type of(double v) {
        return new TypeDouble(v, v);
    }
}
