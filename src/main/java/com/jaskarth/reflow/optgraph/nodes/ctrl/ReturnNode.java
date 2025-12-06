package com.jaskarth.reflow.optgraph.nodes.ctrl;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;

public class ReturnNode extends CtrlNode {
    public ReturnNode(Node node) {
        super(node);
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new InsnNode(Opcodes.DRETURN));
    }

    @Override
    public int req() {
        return 1;
    }

    @Override
    public String describe() {
        return "Return";
    }
}
