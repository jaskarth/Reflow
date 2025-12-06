package com.jaskarth.reflow.optgraph;

import com.jaskarth.reflow.optgraph.block.Blocks;
import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.ctrl.StartNode;
import com.jaskarth.reflow.optgraph.opto.DomGraph;
import com.jaskarth.reflow.runtime.resource.ResourceTracker;
import com.jaskarth.reflow.util.ValidationHelper;

public class CompileUnit {
    private final NodeCache cache;
    private final ResourceTracker resources;
    // TODO: return!
    private final Node top;
    private final StartNode start;
    private Phase phase = Phase.PARSED;
    private DomGraph doms;
    private Blocks blocks;


    public CompileUnit(NodeCache cache, ResourceTracker resources, Node top, StartNode start) {
        this.cache = cache;
        this.resources = resources;
        this.top = top;
        this.start = start;
    }

    public NodeCache cache() {
        return cache;
    }

    public ResourceTracker resources() {
        return resources;
    }

    public Node top() {
        return top;
    }

    public StartNode start() {
        return start;
    }

    public Phase phase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public DomGraph doms() {
        ValidationHelper.assertTrue(doms != null, "accessing doms before they're built");
        return doms;
    }

    public void setDoms(DomGraph doms) {
        this.doms = doms;
    }

    public Blocks blocks() {
        ValidationHelper.assertTrue(blocks != null, "accessing blocks before they're built");
        return blocks;
    }

    public void setBlocks(Blocks blocks) {
        this.blocks = blocks;
    }

    public enum Phase {
        PARSED,
        IDEALIZED,
        GLOBAL_CODE_MOTION,
        LOCAL_CODE_MOTION
    }
}
