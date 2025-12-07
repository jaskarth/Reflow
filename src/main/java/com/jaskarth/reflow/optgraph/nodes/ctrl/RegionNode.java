package com.jaskarth.reflow.optgraph.nodes.ctrl;

import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.util.ValidationHelper;

public class RegionNode extends CtrlNode {
    public RegionNode(Node... ctrlIn) {
        super(ctrlIn);
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public Node idealize(NodeCache nodes) {
        for (Node out : outs()) {
            if (out instanceof PhiNode) {
                return null;
            }
        }
        // should have no phis now, kill the whole if/true/false structure

        // find cfg diamond
        if (in(1) instanceof IfProjNode p1 && in(2) instanceof IfProjNode p2) {
            if (p1.isTrue() == !p2.isTrue() && p1.in(0) == p2.in(0)) {
                ValidationHelper.assertTrue(p1.in(0) instanceof IfNode, "must be if");
                return p1.in(0).in(0);
            }
        }

        return null;
    }

    @Override
    public boolean startsBlock() {
        return true;
    }

    @Override
    public String describe() {
        return "Region";
    }
}
