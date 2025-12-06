package com.jaskarth.reflow.optgraph.opto;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.block.Block;
import com.jaskarth.reflow.optgraph.block.Blocks;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.ctrl.*;
import com.jaskarth.reflow.util.ValidationHelper;

import java.util.*;

public class LocalCodeMotion {
    public static Blocks schedule(CompileUnit unit) {
        Set<CtrlNode> ctrls = unit.doms().ctrls();
        // only block starts
        ctrls.removeIf(c -> !c.startsBlock());

        // Create blocks for each block ctrl
        Map<CtrlNode, Block> blocks = new HashMap<>();
        for (CtrlNode ctrl : ctrls) {
            blocks.put(ctrl, new Block(ctrl));
        }

        // Create block graph
        for (CtrlNode ctrl : ctrls) {
            Block block = blocks.get(ctrl);

            for (Node in : ctrl.ins()) {
                if (in instanceof CtrlNode inc) {
                    Block b = blocks.get(inc);
                    // not in map? it must not be a block start ctrl (likely an if). get its input.
                    if (b == null) {
                        ValidationHelper.assertTrue(inc.in(0) instanceof CtrlNode, "inconsistent schedule");
                        b = blocks.get((CtrlNode) inc.in(0));
                    }

                    block.addIn(b);
                }
            }
        }

        // Now, do the schedule.
        for (CtrlNode ctrl : ctrls) {
            // all nodes for this ctrl
            Set<Node> nodes = new HashSet<>();

            for (Node out : ctrl.outs()) {
                // don't schedule children that starts another block
                if (out instanceof CtrlNode ctrlOut && ctrlOut.startsBlock()) {
                    continue;
                }

                nodes.add(out);
            }

//            Node root = null;
            List<Node> roots = new ArrayList<>();
            for (Node n : nodes) {
                boolean in = false;
                for (Node out : n.outs()) {
                    if (nodes.contains(out)) {
                        in = true;
                        break;
                    }
                }

                if (!in) {
                    roots.add(n);
//                    System.out.println(root + " " + n);
//                    ValidationHelper.assertTrue(root == null, "can't have 2 different roots in a block");
//                    root = n;
                }
            }
            // move if root to the very end
            IfNode ifn = null;
            for (Node root : new ArrayList<>(roots)) {
                if (root instanceof IfNode ifRoot) {
                    roots.remove(root);
                    ifn = ifRoot;
                }
            }
            if (ifn != null) {
                roots.add(ifn);
            }

//            ValidationHelper.assertTrue(root != null, "must have a root");

            Block block = blocks.get(ctrl);

            List<Node> list = new ArrayList<>();
            list.add(ctrl);

            // Try to schedule all phis first
            if (ctrl instanceof RegionNode) {
                for (Node nd : ctrl.outs()) {
                    if (nd instanceof PhiNode) {
                        list.add(nd);
                    }
                }
            }

            Set<Node> scheduled = new HashSet<>(list);

            for (Node root : roots) {
                doSchedule(ctrl, root, list, scheduled);
            }

            block.code().addAll(list);

            if (ctrl instanceof RegionNode) {
                for (Node nd : ctrl.outs()) {
                    if (nd instanceof PhiNode) {
//                        ValidationHelper.assertTrue(list.get(1) == nd, "phi must be scheduled directly after region");
                    }
                }
            }
        }

        return new Blocks(blocks.get(unit.start()), new HashSet<>(blocks.values()));
    }

    private static void doSchedule(CtrlNode ctrl, Node n, List<Node> nodes, Set<Node> scheduled) {
        if (scheduled.contains(n)) {
            // Only schedule once
            return;
        }
        if (!noInputSchedule(n)) {
            for (Node in : n.ins()) {
                ValidationHelper.assertTrue(in != null, "can't be null");
                ValidationHelper.assertTrue(in instanceof StartNode || in instanceof RegionNode || in.in(0) != null, "must be scheduled");
                if (in.kind() == Node.Kind.DATA && in.in(0) == ctrl) {
                    doSchedule(ctrl, in, nodes, scheduled);
                }
            }
        }

        if (!(n instanceof PhiNode)) {
            nodes.add(n);
            scheduled.add(n);
        }
    }

    private static boolean noInputSchedule(Node n) {
        return n instanceof PhiNode || (n instanceof CtrlNode ctrl && ctrl.startsBlock());
    }
}
