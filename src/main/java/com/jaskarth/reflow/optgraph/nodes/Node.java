package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.util.ValidationHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    // TODO: max input size! nodes should have defined arity!
    private final List<@Nullable Node> inputs = new ArrayList<>();
    private final List<Node> outputs = new ArrayList<>();

    public Node(Node... children) {
        // null control
        inputs.add(null);
        for (Node child : children) {
            inputs.add(child);
            child.outputs.add(this);
        }

        ValidationHelper.assertTrue(inputs.size() == req() + 1, "wrong number of nodes!");
    }

    public final List<@Nullable Node> ins() {
        return this.inputs;
    }

    public final List<Node> outs() {
        return this.outputs;
    }

    // Number of data inputs
    public abstract int req();

    public abstract String describe();

    public Node idealize(NodeCache nodes) {
        return null;
    }

    public void generate(MethodBuilder builder) {
        throw new IllegalStateException("Can't generate code for " + this);
    }

    public Type type() {
        return Type.BOTTOM;
    }

    public Kind kind() {
        return Kind.DATA;
    }

    public enum Kind {
        CTRL,
        DATA;
    }

    public boolean remat() {
        return false;
    }

    // only used for rematerialization!
    public Node copy() {
        return null;
    }

    public final Node in(int i) {
        return inputs.get(i);
    }

    public final void setIn(int i, Node n) {
        this.inputs.set(i, n);
    }

    public final void addIn(int i, Node n) {
        ValidationHelper.assertTrue(in(i) == null, "can't call this with an existing node");
        setIn(i, n);
        n.outs().add(this);
    }

    // Hash-eq

    public boolean eq(Node node) {
        return node == this;
    }

    public int hash() {
        return 0;
    }

    // Helper functions

    public final void replaceInput(int idx, @NotNull Node nd) {
        Node old = inputs.set(idx, nd);

        old.outputs.remove(this);
        nd.outputs.add(this);
    }

    public final void replaceAll(@NotNull Node old, @NotNull Node nd) {
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i) == old) {
                inputs.set(i, nd);
            }
        }
        while (old.outputs.remove(this)) {
            nd.outputs.add(this);
        }
    }

    private static final boolean SHOW_IDENTITY = true;

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + describe() + "]" + (SHOW_IDENTITY ? "@" + Integer.toHexString(System.identityHashCode(this)) : "");
    }
}
