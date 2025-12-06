package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeD;
import org.objectweb.asm.tree.LdcInsnNode;

public class ImmDNode extends Node {
    private final double value;

    public ImmDNode(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public Type type() {
        return TypeD.of(value);
    }

    @Override
    public boolean eq(Node node) {
        return node instanceof ImmDNode imm && imm.value == value;
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new LdcInsnNode(value));
    }

    @Override
    public boolean remat() {
        return true;
    }

    @Override
    public Node copy() {
        return new ImmDNode(value);
    }

    @Override
    public int hash() {
        return Double.hashCode(value);
    }

    @Override
    public int req() {
        return 0;
    }

    @Override
    public String describe() {
        return "" + value;
    }
}
