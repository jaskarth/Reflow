package com.jaskarth.reflow.optgraph.nodes.cache;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeD;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class OnceCacheGetSetNode extends Node {
    public OnceCacheGetSetNode(Node... children) {
        super(children);
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.CACHEONCE.get(), "put", "(L" + Refs.FUNCTION_CTX.get() + ";D)D"));
    }

    @Override
    public Type type() {
        return TypeD.bottom();
    }

    @Override
    public int req() {
        return 3;
    }

    @Override
    public String describe() {
        return "OnceCacheGetSet";
    }
}
