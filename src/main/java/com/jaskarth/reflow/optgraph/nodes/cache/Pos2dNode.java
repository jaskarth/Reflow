package com.jaskarth.reflow.optgraph.nodes.cache;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeJ;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class Pos2dNode extends Node {
    public Pos2dNode(Node... children) {
        super(children);
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public Type type() {
        return TypeJ.bottom();
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new MethodInsnNode(Opcodes.INVOKESTATIC, Refs.CHUNKPOS.get(), Refs.CHUNKPOS_ASLONG.get(), "(II)J"));
    }

    @Override
    public String describe() {
        return "Pos2d";
    }

    @Override
    public boolean eq(Node node) {
        return node instanceof Pos2dNode o && o.in(1) == in(1) && o.in(2) == in(2);
    }

    @Override
    public int hash() {
        return 999 + in(1).hashCode() + in(2).hashCode();
    }
}
