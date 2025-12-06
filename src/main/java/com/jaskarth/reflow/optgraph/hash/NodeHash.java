package com.jaskarth.reflow.optgraph.hash;

import com.jaskarth.reflow.optgraph.nodes.Node;

public record NodeHash(Node node, int hash) {
    public NodeHash(Node node) {
        this(node, node.hash());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeHash(Node o, int u)) {
            return node.eq(o);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return node.hash();
    }
}
