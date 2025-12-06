package com.jaskarth.reflow.optgraph.nodes.ctrl;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.util.ValidationHelper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;

public abstract class CtrlNode extends Node {
    // TODO: method
    public LabelNode label = null;

    public CtrlNode(Node... children) {
        super(children);
    }

    @Override
    public void generate(MethodBuilder builder) {
        ValidationHelper.assertTrue(label != null, "label must not be null");
        builder.insn(label);
    }

    public void setupCodegen(Label label) {
        this.label = new LabelNode(label);
    }

    public boolean startsBlock() {
        return false;
    }

    @Override
    public Kind kind() {
        return Kind.CTRL;
    }
}
