package com.jaskarth.reflow.optgraph.opto;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.vars.SpillLoadNode;
import com.jaskarth.reflow.optgraph.nodes.vars.SpillStoreNode;
import com.jaskarth.reflow.util.ValidationHelper;

public class VarsAndResources {
    public static void apply(CompileUnit unit) {
        // IR is stable, set up lv indices now

        int idx = 2;
        for (Node node : unit.cache().live()) {
            if (node instanceof SpillStoreNode store) {

                store.setIdx(idx);

                for (Node out : node.outs()) {
                    ValidationHelper.assertTrue(out instanceof SpillLoadNode, "out of store must be load");
                    ((SpillLoadNode)out).setIdx(idx);
                }

                idx += store.type().mach().slotSize();
            }
        }

        // Now set up all the resources
        unit.resources().fillNames();
    }
}
