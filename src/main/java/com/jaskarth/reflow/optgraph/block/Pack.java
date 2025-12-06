package com.jaskarth.reflow.optgraph.block;

import com.jaskarth.reflow.optgraph.nodes.Node;

import java.util.List;

@Deprecated
public class Pack {
    private final List<Node> ordered;
    private Pack next;
    private Pack last;

    public Pack(List<Node> ordered) {
        this.ordered = ordered;
    }

    public List<Node> get() {
        return this.ordered;
    }
}
