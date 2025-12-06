package com.jaskarth.reflow.optgraph.nodes;

import net.minecraft.world.level.levelgen.DensityFunctions;

public class UnaryNode extends Node {
    private final DensityFunctions.Mapped.Type type;

    public UnaryNode(DensityFunctions.Mapped.Type type, Node... nodes) {
        super(nodes);
        this.type = type;
    }

    @Override
    public boolean eq(Node node) {
        if (node instanceof UnaryNode o) {
            return o.type == type && in(1) == o.in(1);
        }

        return false;
    }

    @Override
    public int hash() {
        return Integer.hashCode(type.ordinal()) + in(1).hashCode();
    }

    @Override
    public int req() {
        return 1;
    }

    @Override
    public String describe() {
        return "" + type;
    }
}
