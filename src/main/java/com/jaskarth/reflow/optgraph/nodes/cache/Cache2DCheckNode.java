package com.jaskarth.reflow.optgraph.nodes.cache;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class Cache2DCheckNode extends Node {
    public Cache2DCheckNode(Node... children) {
        super(children);
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.CACHE2D.get(), "is", "(J)Z"));
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public String describe() {
        return "Cache2DCheck";
    }
}
