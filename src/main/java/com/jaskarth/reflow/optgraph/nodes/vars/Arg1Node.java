package com.jaskarth.reflow.optgraph.nodes.vars;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypePtr;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.VarInsnNode;

public class Arg1Node extends Node {

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new VarInsnNode(Opcodes.ALOAD, 1));
    }

    @Override
    public int req() {
        return 0;
    }

    @Override
    public Type type() {
        return TypePtr.BOTPTR;
    }

    @Override
    public boolean remat() {
        return true;
    }

    @Override
    public Node copy() {
        return new Arg1Node();
    }

    @Override
    public boolean eq(Node node) {
        return node instanceof Arg1Node;
    }

    @Override
    public int hash() {
        return 999;
    }

    @Override
    public String describe() {
        return "Arg1";
    }
}
