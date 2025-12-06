package com.jaskarth.reflow.optgraph.nodes.cache;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypePtr;
import com.jaskarth.reflow.runtime.resource.ResourceLease;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class LoadResourceNode extends Node {
    private final ResourceLease lease;

    public LoadResourceNode(ResourceLease lease) {
        this.lease = lease;
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new VarInsnNode(Opcodes.ALOAD, 0));
        builder.insn(new FieldInsnNode(Opcodes.GETFIELD, builder.thisClass(), lease.getName(), lease.type().ref().asDescriptor()));
    }

    @Override
    public Type type() {
        return TypePtr.BOTPTR;
    }

    @Override
    public int req() {
        return 0;
    }

    @Override
    public String describe() {
        return "LoadResource";
    }
}
