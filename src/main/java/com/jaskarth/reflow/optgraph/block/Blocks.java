package com.jaskarth.reflow.optgraph.block;

import java.util.Set;

public class Blocks {
    private final Block start;
    private final Set<Block> blocks;

    public Blocks(Block start, Set<Block> blocks) {
        this.start = start;
        this.blocks = blocks;
    }

    public Block start() {
        return start;
    }

    public Set<Block> blocks() {
        return blocks;
    }
}
