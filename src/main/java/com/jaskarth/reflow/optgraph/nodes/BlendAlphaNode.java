package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.optgraph.hash.NodeCache;

public class BlendAlphaNode extends Node {
    @Override
    public int req() {
        return 0;
    }

    @Override
    public String describe() {
        return "BlendAlpha";
    }

    @Override
    public Node idealize(NodeCache nodes) {
        return nodes.get(new ImmDNode(1.0));
    }

    @Override
    public boolean eq(Node node) {
        return node instanceof BlendAlphaNode;
    }

    @Override
    public int hash() {
        return 999;
    }
}
