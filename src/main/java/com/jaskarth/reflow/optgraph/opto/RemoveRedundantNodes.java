package com.jaskarth.reflow.optgraph.opto;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.nodes.Node;

// This class itself is redundant
public class RemoveRedundantNodes {
    public static void run(CompileUnit unit) {
        for (Node node : unit.cache().live()) {
            for (int i = 1; i < node.ins().size(); i++) {
                Node in = node.ins().get(i);
                Node canonical = unit.cache().get(in);
                if (canonical == in) {
                    continue;
                }

                node.replaceInput(i, canonical);
            }
        }
    }
}
