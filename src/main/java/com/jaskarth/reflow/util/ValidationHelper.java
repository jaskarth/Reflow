package com.jaskarth.reflow.util;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.nodes.Node;

public class ValidationHelper {
    public static void assertTrue(boolean cond, String detail) {
        if (!cond) {
            throw new IllegalStateException("Assertion failed: " + detail);
        }
    }

    public static void invariants(CompileUnit unit) {
        assertNodesEq(unit);
    }

    private static void assertNodesEq(CompileUnit unit) {
        for (Node node : unit.cache().live()) {
            if (!node.eq(node)) {
                assertTrue(false, "node.eq " + node + " not reflexive");
            }
        }
    }
}
