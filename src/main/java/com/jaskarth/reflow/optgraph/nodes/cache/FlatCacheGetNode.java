package com.jaskarth.reflow.optgraph.nodes.cache;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeD;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class FlatCacheGetNode extends Node {
    public FlatCacheGetNode(Node... children) {
        super(children);
    }

    @Override
    public void generate(MethodBuilder builder) {
//        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.CACHEFLAT.get(), "get", "(J)D"));
        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.CACHEFLATWRAP.get(), "get", "(J)D"));
    }

    @Override
    public Type type() {
        return TypeD.bottom();
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public String describe() {
        return "FlatCacheGet";
    }
}
