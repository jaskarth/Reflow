package com.jaskarth.reflow.optgraph.type;

import com.jaskarth.reflow.optgraph.nodes.ImmDNode;
import com.jaskarth.reflow.optgraph.nodes.Node;
import org.jetbrains.annotations.Nullable;

public record TypeD(double min, double max) implements Type {
    public static final TypeD FULL_RANGE = new TypeD(-Double.MAX_VALUE, Double.MAX_VALUE);

    public TypeD {
        if (min > max) {
            throw new IllegalStateException("TypeD min " + min + " > max " + max);
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

    public boolean isDBottom() {
        return min == -Double.MAX_VALUE && max == Double.MAX_VALUE;
    }

    public static Type bottom() {
        return new TypeD(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static Type of(double v) {
        return new TypeD(v, v);
    }
}
