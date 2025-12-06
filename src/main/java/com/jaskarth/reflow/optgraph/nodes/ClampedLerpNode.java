package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class ClampedLerpNode extends Node {
    public ClampedLerpNode(Node... nodes) {
        super(nodes);
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new MethodInsnNode(Opcodes.INVOKESTATIC, Refs.MTH.get(), Refs.MTH_CLAMPEDLERP.get(), "(DDD)D"));
    }

    @Override
    public int req() {
        return 3;
    }

    @Override
    public String describe() {
        return "ClampedLerp";
    }
}
