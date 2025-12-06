package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.optgraph.hash.NodeCache;

public class BlendOffsetNode extends Node {
    @Override
    public int req() {
        return 0;
    }

    @Override
    public String describe() {
        return "BlendOffset";
    }

    @Override
    public Node idealize(NodeCache nodes) {
        return nodes.get(new ImmDNode(0.0));
    }

    @Override
    public boolean eq(Node node) {
        return node instanceof BlendOffsetNode;
    }

    @Override
    public int hash() {
        return 999;
    }
}
