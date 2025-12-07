package com.jaskarth.reflow.optgraph.nodes.vars;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeInt;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class CtxArgNode extends Node {
    private final Arg arg;
    public CtxArgNode(Arg arg) {
        this.arg = arg;
    }

    @Override
    public Type type() {
        return TypeInt.bottom();
    }

    @Override
    public int req() {
        return 0;
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new VarInsnNode(Opcodes.ALOAD, 1));
        builder.insn(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Refs.FUNCTION_CTX.get(), name(arg), "()I"));
    }

    private static String name(Arg arg) {
        return (switch (arg) {
            case X -> Refs.CTX_X;
            case Y -> Refs.CTX_Y;
            case Z -> Refs.CTX_Z;
        }).get();
    }

    @Override
    public boolean eq(Node node) {
        if (node instanceof CtxArgNode o) {
            return o.arg == arg;
        }

        return false;
    }

    @Override
    public int hash() {
        return arg.hashCode();
    }

    @Override
    public String describe() {
        return "CtxArg=" + arg;
    }

    public enum Arg {
        X,
        Y,
        Z
    }
}
