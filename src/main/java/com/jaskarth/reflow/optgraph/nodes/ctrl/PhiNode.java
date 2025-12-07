package com.jaskarth.reflow.optgraph.nodes.ctrl;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.nodes.ImmDNode;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.cache.OnceCacheGetSetNode;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.util.ValidationHelper;

public class PhiNode extends Node {
    public PhiNode(Node region, Node... in) {
        super(in);
        addIn(0, region);
    }

    @Override
    public void generate(MethodBuilder builder) {
        // none!
    }

    @Override
    public Node idealize(NodeCache nodes) {
        Node p = immOnceCache();
        if (p != null) {
            return p;
        }

        return null;
    }

    private Node immOnceCache() {
        for (Node in : ins()) {
            if (in instanceof OnceCacheGetSetNode once && once.in(3) instanceof ImmDNode imm) {
                return imm;
            }
        }
        return null;
    }

    @Override
    public Type type() {
        ValidationHelper.assertTrue(in(1).type().mach() == in(2).type().mach(), "types must be same");
        return in(1).type().meet(in(2).type());
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public String describe() {
        return "Phi";
    }
}
