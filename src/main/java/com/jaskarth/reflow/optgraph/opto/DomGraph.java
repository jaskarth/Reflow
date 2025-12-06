package com.jaskarth.reflow.optgraph.opto;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.ctrl.CtrlNode;
import com.jaskarth.reflow.optgraph.nodes.ctrl.RegionNode;
import com.jaskarth.reflow.optgraph.nodes.ctrl.StartNode;
import com.jaskarth.reflow.util.ValidationHelper;

import java.util.*;

public class DomGraph {
    private final Map<CtrlNode, CtrlNode> idoms;

    public DomGraph(Map<CtrlNode, CtrlNode> idoms) {
        this.idoms = idoms;
    }

    public CtrlNode idom(CtrlNode node) {
        return idoms.get(node);
    }

    public Set<CtrlNode> ctrls() {
        return new HashSet<>(idoms.keySet());
    }

    // find the lca of ctrl and an existing lca
    public CtrlNode findLcaDom(CtrlNode lca, CtrlNode ctrl) {
        if (dominates(ctrl, lca)) {
            return lca;
        }

        while (true) {
            lca = idom(lca);
            if (dominates(ctrl, lca)) {
                return lca;
            }
        }
    }

    // is 'node' dominated by 'dom'?
    public boolean dominates(CtrlNode node, CtrlNode dom) {
        if (node == dom) {
            return true;
        }

        while (true) {
            CtrlNode nd = idoms.get(node);
            if (nd == node) {
                return false;
            } else if (nd == dom) {
                return true;
            } else if (nd == null) {
                ValidationHelper.assertTrue(false, "should be impossible!");
            }

            node = nd;
        }
    }

    public static DomGraph create(CompileUnit unit) {
        StartNode start = unit.start();
        Set<CtrlNode> ctrls = ctrls(start);
        ctrls.remove(start);

        Map<CtrlNode, CtrlNode> idoms = new HashMap<>();
        idoms.put(start, start);
        for (CtrlNode ctrl : ctrls) {
            idoms.put(ctrl, null);
        }

        // This shit-ass algorithm works because we don't have loops, and all regions have only 2 inputs

        // First pass: all 1 input, non regions
        for (CtrlNode ctrl : new HashSet<>(ctrls)) {
            if (!(ctrl instanceof RegionNode)) {
                ValidationHelper.assertTrue(ctrl.in(0) != null, "in(0) must not be null");
                ValidationHelper.assertTrue(ctrl.in(0) instanceof CtrlNode, "in(0) must be ctrl");
                idoms.put(ctrl, (CtrlNode) ctrl.in(0));
                ctrls.remove(ctrl);
            }
        }

        // Now run for all regions
        int iter = 0;
        while (true) {
            boolean changed = false;
            for (CtrlNode ctrl : new HashSet<>(ctrls)) {
                if (ctrl instanceof RegionNode) {
                    CtrlNode dom1 = idoms.get((CtrlNode) ctrl.in(1));
                    CtrlNode dom2 = idoms.get((CtrlNode) ctrl.in(2));
                    if (dom1 == null || dom2 == null) {
                        continue;
                    }

                    if (dominates(idoms, dom1, dom2)) {
                        idoms.put(ctrl, dom2);
                    } else {
                        idoms.put(ctrl, dom1);
                    }

                    ctrls.remove(ctrl);
                    changed = true;
                } else {
                    ValidationHelper.assertTrue(false, "must not be");
                }
            }

            if (!changed) {
                break;
            }

            if (iter++ > 1000) {
                ValidationHelper.assertTrue(false, "infinite loop in idom creation");
            }
        }

        ValidationHelper.assertTrue(ctrls.isEmpty(), "all ctrls must have been processed");

        return new DomGraph(idoms);
    }

    // is 'node' dominated by 'dom'?
    private static boolean dominates(Map<CtrlNode, CtrlNode> idoms, CtrlNode node, CtrlNode dom) {
        if (node == dom) {
            return true;
        }

        while (true) {
            CtrlNode nd = idoms.get(node);
            if (nd == node) {
                return false;
            } else if (nd == dom) {
                return true;
            } else if (nd == null) {
                return false;
            }

            node = nd;
        }
    }

    private static Set<CtrlNode> ctrls(StartNode start) {
        Set<CtrlNode> set = new HashSet<>();
        Deque<CtrlNode> q = new ArrayDeque<>();
        q.add(start);

        while (!q.isEmpty()) {
            CtrlNode n = q.poll();

            if (!set.add(n)) {
                continue;
            }

            for (Node out : n.outs()) {
                if (out instanceof CtrlNode ctrl) {
                    q.add(ctrl);
                }
            }
        }

        return set;
    }
}
