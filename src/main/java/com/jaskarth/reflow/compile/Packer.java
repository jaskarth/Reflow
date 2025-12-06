package com.jaskarth.reflow.compile;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.block.Pack;

import java.util.*;

public class Packer {
    private static class Graph {
        private Map<Node, List<Edge>> incomingEdges = new HashMap<>();
        public Graph(CompileUnit unit) {
            for (Node node : unit.cache().live()) {
                for (Node in : node.ins()) {
                    if (in == null) {
                        continue;
                    }
                    incomingEdges.computeIfAbsent(in, k -> new ArrayList<>()).add(new Edge(node, in));
                }
            }
        }

        private record Edge(Node n, Node m) {

        }
    }

    // Node - optimized node under
    public static Pack compile(CompileUnit unit) {
        // Topsort

        // TODO: need to check cycles? is that even possible?

        Graph g = new Graph(unit);

        List<Node> sorted = new ArrayList<>();
        Deque<Node> s = new ArrayDeque<>();
        s.add(unit.top());

        while (!s.isEmpty()) {
            Node n = s.poll();
            sorted.add(n);
            for (Node in : n.ins()) {
                if (in == null) {
                    continue;
                }
                g.incomingEdges.get(in).remove(new Graph.Edge(n, in));
                if (g.incomingEdges.get(in).isEmpty()) {
                    s.add(in);
                }
            }
        }

        return new Pack(sorted);
    }
}
