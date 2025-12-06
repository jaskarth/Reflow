package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeD;
import com.jaskarth.reflow.optgraph.type.TypeI;
import com.jaskarth.reflow.util.ValidationHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;

public class ConvI2DNode extends Node {
    public ConvI2DNode(Node node) {
        super(node);
    }

    @Override
    public int req() {
        return 1;
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new InsnNode(Opcodes.I2D));
    }

    @Override
    public Type type() {
        Type type = in(1).type();
        ValidationHelper.assertTrue(type instanceof TypeI, "input should be int");
        TypeI i = (TypeI) type;
        return new TypeD(i.min(), i.max());
    }

    @Override
    public boolean eq(Node node) {
        if (node instanceof ConvI2DNode o) {
            return in(1) == o.in(1);
        }

        return false;
    }

    @Override
    public int hash() {
        return in(1).hashCode();
    }

    @Override
    public String describe() {
        return "ConvI2D";
    }
}
