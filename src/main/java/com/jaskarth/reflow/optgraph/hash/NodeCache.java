package com.jaskarth.reflow.optgraph.hash;

import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.ctrl.StartNode;
import com.jaskarth.reflow.util.ValidationHelper;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class NodeCache {
    // TODO: make sure that nodes keys and live set are always aligned
    private final Map<NodeHash, Node> nodes = new HashMap<>();
    public final Set<Node> live = new HashSet<>();
    private Consumer<Node> nodeCallback = n -> {};

    private boolean shouldHash = true;

    public NodeCache() {

    }

    public void stopHashing() {
        shouldHash = false;
    }

    public boolean isHashing() {
        return shouldHash;
    }

    public void setupIdeal(@NotNull Consumer<Node> callback) {
        this.nodeCallback = callback;
    }

    public void finishIdeal() {
        this.nodeCallback = n -> {};
    }

    public void resetTables() {
        System.out.println(nodes.size() + " / " + live.size());
        Set<Node> nodes = new HashSet<>(this.live);
        this.nodes.clear();
        for (Node node : nodes) {
            this.nodes.put(new NodeHash(node), node);
            if (!this.nodes.containsKey(new NodeHash(node))) {
                ValidationHelper.assertTrue(false, "wtf");
            }
        }
        System.out.println(nodes.size() + " / " + live.size());
    }

    public void checkHashes() {
        ValidationHelper.assertTrue(nodes.size() == live.size(), "tables must be parallel");
        for (Node n : live) {
            ValidationHelper.assertTrue(nodes.containsValue(n), "must be in table");
            ValidationHelper.assertTrue(nodes.containsKey(new NodeHash(n)), "must be in table");
        }
        for (Node n : nodes.values()) {
            ValidationHelper.assertTrue(live.contains(n), "must be");
        }
    }

    public @NotNull Node get(@NotNull Node in) {
        ValidationHelper.assertTrue(in != null, "can't be null");
        if (!shouldHash) {
            // Past hashing? just return the given node.
            live.add(in);
            return in;
        }

        NodeHash hash = new NodeHash(in);
        Node node = nodes.get(hash);
        if (node != null) {
            destructHash(in);
            nodeCallback.accept(node);
            return node;
        }

        nodes.put(hash, in);
        live.add(in);
        nodeCallback.accept(in);
        return in;
    }

    public boolean has(@NotNull Node in) {
        ValidationHelper.assertTrue(live.contains(in) == (nodes.get(new NodeHash(in)) != null), "must be");
        return nodes.get(new NodeHash(in)) != null;
    }

    public void unhash(@NotNull Node in) {
        Node old = nodes.remove(new NodeHash(in));
        boolean removed = live.remove(in);
        ValidationHelper.assertTrue(removed, "must have removed");
        ValidationHelper.assertTrue(old != null, "should already be hashed " + in);
    }

    public void rehash(Node in) {
        NodeHash hash = new NodeHash(in);
        Node put = nodes.put(hash, in);
        boolean added = live.add(in);
        ValidationHelper.assertTrue(added, "must have added");
        ValidationHelper.assertTrue(put == null, "must not be hashed " + in);
    }

    // only for nodes that haven't been fully added to the hash table
    private void destructHash(@NotNull Node in) {
        for (Node inp : in.ins()) {
            if (inp == null) {
                continue;
            }

            inp.outs().remove(in);
        }
    }

    public void destruct(@NotNull Node in) {
        ValidationHelper.assertTrue(!(in instanceof StartNode), "shouldn't destruct start");
//        ValidationHelper.assertTrue(has(in), "must destruct node that exists");

        for (Node inp : in.ins()) {
            if (inp == null) {
                continue;
            }

            inp.outs().remove(in);
        }
        Node r1 = nodes.remove(new NodeHash(in));
        live.remove(in);
//        ValidationHelper.assertTrue(r1 == in, "must have removed the same one");
    }

    public Set<Node> live() {
        return live;
    }
}
