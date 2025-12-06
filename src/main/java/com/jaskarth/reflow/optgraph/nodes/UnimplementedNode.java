package com.jaskarth.reflow.optgraph.nodes;

// Just for debugging!
public class UnimplementedNode extends Node {
    private final String name;
    private final int size;

    public UnimplementedNode(String name, Node... children) {
        super(children);
        this.name = name;
        this.size = children.length;
    }

    @Override
    public int req() {
        return size;
    }

    @Override
    public String describe() {
        return name;
    }
}
