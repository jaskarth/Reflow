package com.jaskarth.reflow.optgraph.opto;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.nodes.ImmDNode;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.cache.OnceCacheGetSetNode;
import com.jaskarth.reflow.util.ValidationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;

public class Idealize {
    public static void idealize(CompileUnit unit) {
        NodeCache nodes = unit.cache();
        ValidationHelper.assertTrue(nodes.isHashing(), "must be hashing to idealize");
        Deque<Node> q = new ArrayDeque<>(nodes.live());
        nodes.setupIdeal(q::add);
        nodes.checkHashes();

        while (!q.isEmpty()) {
            Node n = q.poll();

            if (!nodes.has(n)) {
                // TODO: maybe destruct it here?
                continue;
            }
            nodes.unhash(n);

            // Not a constant? Try to make it so.
            if (!(n instanceof ImmDNode)) {
                Node con = n.type().constant();

                if (con != null) {
                    con = nodes.get(con);
                    processIdeal(con, q, n, nodes);
                    nodes.checkHashes();
                    continue;
                }
            }

            Node changed = n.idealize(nodes);

            processIdeal(changed, q, n, nodes);
            nodes.checkHashes();
        }

        nodes.checkHashes();

        // Any nodes with no outputs? destroy them now
        while (true) {
            boolean progress = false;
            for (Node node : new HashSet<>(nodes.live())) {
                // Remove any non-return nodes with no inputs
                if (node.outs().isEmpty() && node != unit.top()) {
                    nodes.destruct(node);
                    progress = true;
                }
            }

            if (!progress) {
                break;
            }
        }

        nodes.checkHashes();

        nodes.finishIdeal();
    }

    private static void processIdeal(@Nullable Node changed, Deque<Node> q, @NotNull Node n, NodeCache nodes) {
        if (changed == null) {
            // stick it back in the hashtable
            nodes.rehash(n);
            return;
        }
        q.add(changed);

        // Notify ins and outs of the changing node
        for (Node in : n.ins()) {
            if (in == null) {
                continue;
            }
            q.add(in);
        }

        for (Node out : n.outs()) {
            q.add(out);
            notifyExtraOuts(out, q);
        }

        if (n == changed) {
            nodes.rehash(changed);
            return;
        }
        nodes.unhash(changed);

        // replace old node with new one
        for (Node out : new HashSet<>(n.outs())) {
            nodes.unhash(out);
            out.replaceAll(n, changed);
            nodes.rehash(out);
        }

        // kill it now
        nodes.destruct(n);

        nodes.rehash(changed);
    }

    private static void notifyExtraOuts(Node out, Deque<Node> q) {
        if (out instanceof OnceCacheGetSetNode) {
            q.addAll(out.outs());
        }
    }
}
