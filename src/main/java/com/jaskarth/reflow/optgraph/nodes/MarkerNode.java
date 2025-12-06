package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.type.Type;
import net.minecraft.world.level.levelgen.DensityFunctions;

// Marker for a cache
public class MarkerNode extends Node {
    private final DensityFunctions.Marker.Type type;

    public MarkerNode(DensityFunctions.Marker.Type type, Node... children) {
        super(children);
        this.type = type;
    }

    @Override
    public Node idealize(NodeCache nodes) {
        if (in(1) instanceof ImmDNode) {
            return in(1);
        }

        // FlatCache(Cache2D(...)) => FlatCache(...)
        // This is a redundant layer of caching and just adds indirection.
        if (this.type == DensityFunctions.Marker.Type.FlatCache) {
            if (in(1) instanceof MarkerNode min0 && min0.type == DensityFunctions.Marker.Type.Cache2D) {
                return nodes.get(new MarkerNode(DensityFunctions.Marker.Type.FlatCache, min0.in(1)));
            }
        }

        return null;
    }

    @Override
    public Type type() {
        return in(1).type();
    }

    @Override
    public int req() {
        return 1;
    }

    @Override
    public String describe() {
        return "Marker/" + type;
    }

    @Override
    public boolean eq(Node node) {
        if (node instanceof MarkerNode o) {
            return o.type == type && in(1) == o.in(1);
        }

        return false;
    }

    @Override
    public int hash() {
        return Integer.hashCode(type.ordinal()) + in(1).hashCode();
    }
}
