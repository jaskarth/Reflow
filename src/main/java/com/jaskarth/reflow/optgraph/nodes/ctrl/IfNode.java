package com.jaskarth.reflow.optgraph.nodes.ctrl;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.util.ValidationHelper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.JumpInsnNode;

public class IfNode extends CtrlNode {
    public IfNode(Node ctrl, Node in) {
        super(in);
        addIn(0, ctrl);
    }

    @Override
    public void generate(MethodBuilder builder) {
        Node n1 = outs().get(0);
        Node n2 = outs().get(1);
        CtrlNode t;
        CtrlNode f;
        if (n1 instanceof IfProjNode proj && proj.isTrue()) {
            t = (CtrlNode) n1;
            f = (CtrlNode) n2;
        } else {
            t = (CtrlNode) n2;
            f = (CtrlNode) n1;
        }

        ValidationHelper.assertTrue(f.label != null, "label must not be null");
        builder.insn(new JumpInsnNode(Opcodes.IFNE, t.label));
        builder.insn(new JumpInsnNode(Opcodes.GOTO, f.label));
    }

    @Override
    public int req() {
        return 1;
    }

    @Override
    public String describe() {
        return "If";
    }
}
