package com.jaskarth.reflow.optgraph.nodes.cache;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeInt;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class FlatCacheCheckNode extends Node {
    public FlatCacheCheckNode(Node... children) {
        super(children);
    }

    @Override
    public void generate(MethodBuilder builder) {
//        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.CACHEFLAT.get(), "is", "(J)Z"));
        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.CACHEFLATWRAP.get(), "is", "(J)Z"));
    }

    @Override
    public Type type() {
        return TypeInt.bool();
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public String describe() {
        return "FlatCacheCheck";
    }
}
