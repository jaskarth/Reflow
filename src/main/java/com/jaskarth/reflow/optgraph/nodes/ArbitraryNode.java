package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class ArbitraryNode extends Node {
    private final String descriptor;

    public ArbitraryNode(String descriptor, Node... children) {
        super(children);
        this.descriptor = descriptor;
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.ARBRES.get(), "compute", "(L" + Refs.FUNCTION_CTX.get() + ";)D"));
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public String describe() {
        return "Arbitrary/" + descriptor;
    }
}
