package com.jaskarth.reflow.optgraph.block;

import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.ctrl.CtrlNode;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private final List<Block> ins = new ArrayList<>();
    private final List<Block> outs = new ArrayList<>();
    private final List<Node> code = new ArrayList<>();
    private final CtrlNode head;

    public Block(CtrlNode head) {
        this.head = head;
    }

    public CtrlNode head() {
        return head;
    }

    public List<Block> ins() {
        return ins;
    }

    public List<Block> outs() {
        return outs;
    }

    public final List<Node> code() {
        return code;
    }

    public void addIn(Block block) {
        ins.add(block);
        block.outs.add(this);
    }
}
