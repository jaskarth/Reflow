package com.jaskarth.reflow.optgraph.opto;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.ctrl.*;
import com.jaskarth.reflow.util.ValidationHelper;

import java.util.*;

// Borrowed heavily from: https://github.com/SeaOfNodes/Simple/blob/main/chapter11/README.md
// Go check it out, it's very helpful.
public class GlobalCodeMotion {
    public static void scheduleEarly(CompileUnit unit) {
        StartNode start = unit.start();
        List<CtrlNode> postorder = new ArrayList<>();
        reversePostorder(start, new HashSet<>(), postorder);

        Set<Node> set = new HashSet<>();
        for (int i = postorder.size() - 1; i >= 0; i--) {
            CtrlNode ctrl = postorder.get(i);
            for (Node in : ctrl.ins()) {
                if (in != null) {
                    doScheduleEarly(unit, in, set);
                }
            }

            // schedule phis
            if (ctrl instanceof RegionNode) {
                for (Node out : ctrl.outs()) {
                    if (out instanceof PhiNode) {
                        doScheduleEarly(unit, out, set);
                    }
                }
            }
        }
    }

    public static void scheduleLate(CompileUnit unit) {
        Map<Node, CtrlNode> ctrls = new HashMap<>();
        Set<Node> nodes = new HashSet<>();
        doScheduleLate(unit, unit.start(), nodes, ctrls);

        for (Node node : nodes) {
            CtrlNode ctrl = ctrls.get(node);

            node.replaceInput(0, ctrl);
        }

    }

    private static void reversePostorder(CtrlNode node, Set<Node> visited, List<CtrlNode> output) {
        // already seen?
        if (!visited.add(node)) {
            return;
        }

        for (Node out : node.outs()) {
            if (out instanceof CtrlNode ctrl) {
                reversePostorder(ctrl, visited, output);
            }
        }
        output.add(node);
    }

    private static void doScheduleEarly(CompileUnit unit, Node n, Set<Node> visited) {
        ValidationHelper.assertTrue(n != null, "can't schedule null");
        if (!visited.add(n)) {
            return;
        }

        // schedule all of n's inputs
        for (Node in : n.ins()) {
            if (in != null && !isPinned(in)) {
                doScheduleEarly(unit,in, visited);
            }
        }
        // now schedule n
        if (!isPinned(n)) {
            CtrlNode cfg = unit.start();

            for (int i = 1; i < n.ins().size(); i++) {
                CtrlNode inCtrl = (CtrlNode) n.in(i).in(0);
//                System.out.println("Comparing " + cfg + " and " + inCtrl);
                if (!unit.doms().dominates(cfg, inCtrl)) {
                    cfg = inCtrl;
                }
//                System.out.println("Went with " + cfg);
            }
            n.addIn(0, cfg);
        }
    }

    private static void doScheduleLate(CompileUnit unit, Node n, Set<Node> nodes, Map<Node, CtrlNode> late) {
        DomGraph doms = unit.doms();
        // Already done
        if (late.get(n) != null) {
            return;
        }
        if (n instanceof CtrlNode ctrl) {
            late.put(n, ctrl.startsBlock() ? ctrl : (CtrlNode) ctrl.in(0));
        } else if (n instanceof PhiNode) {
            // late schedule of phi is region
            late.put(n, (CtrlNode) n.in(0));
        }

        // walk outputs forwards
        for (Node out : n.outs()) {
            if (isForwards(out, n)) {
                doScheduleLate(unit, out, nodes, late);
            }
        }

        if (isPinned(n)) {
            return;
        }

        CtrlNode early = (CtrlNode) n.in(0);
        ValidationHelper.assertTrue(early != null, "must have done an early schedule");
        CtrlNode lca = null;
        for (Node out : n.outs()) {
            CtrlNode ctrl = useBlock(n, out, late);
            if (lca == null) {
                lca = ctrl;
            } else {
                CtrlNode lcaDom = doms.findLcaDom(lca, ctrl);
                if (lcaDom instanceof IfNode) {
                    lcaDom = doms.idom(lcaDom);
                }
//                System.out.println(n + " " + lca + " " + ctrl + " " + lcaDom);
                lca = lcaDom;
            }

//            if (lca == null || doms.dominates(lca, ctrl)) {
//                lca = ctrl;
//            }
        }

        CtrlNode best = lca;
        lca = doms.idom(lca);
        while (lca != doms.idom(early)) {
            if (better(doms, lca, best)) {
                best = lca;
            }

            lca = doms.idom(lca);
        }

        ValidationHelper.assertTrue(!(best instanceof IfNode), "can't be if");
        nodes.add(n);
        late.put(n, best);

    }

    private static boolean better(DomGraph doms, CtrlNode lca, CtrlNode best) {
        return doms.dominates(lca, best);
    }

    private static CtrlNode useBlock(Node n, Node out, Map<Node, CtrlNode> late) {
        if (out instanceof PhiNode phi) {
            CtrlNode found = null;
            for (int i = 1; i < phi.ins().size(); i++) {
                if (phi.in(i) == n) {
                    ValidationHelper.assertTrue(found == null, "must be");
                    found = (CtrlNode) phi.in(0).in(i);
                }
            }
            ValidationHelper.assertTrue(found != null, "must have found something");
            return found;
        }

        return late.get(out);
    }

    private static boolean isForwards(Node out, Node n) {
        // loops aren't real yet
        return true;
    }

    private static boolean isPinned(Node node) {
        // TODO: also generic proj?
        return node instanceof PhiNode || node instanceof CtrlNode;
    }
}
