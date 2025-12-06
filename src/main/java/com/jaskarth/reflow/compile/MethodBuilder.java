package com.jaskarth.reflow.compile;

import com.jaskarth.reflow.util.ValidationHelper;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodBuilder {
    private final String thisClass;
    private final MethodNode node;
    private final Label start = new Label();
    private final Label end = new Label();
    private boolean started = false;

    public MethodBuilder(String thisClass, MethodNode node) {
        this.thisClass = thisClass;
        this.node = node;
    }

    public String thisClass() {
        return this.thisClass;
    }

    public void start() {
        ValidationHelper.assertTrue(!started, "must not have started");
        started = true;
        insn(new LabelNode(start));
    }

    public void insn(AbstractInsnNode insn) {
        ValidationHelper.assertTrue(started, "must have started");
        this.node.instructions.add(insn);
    }

    public void end() {
        insn(new LabelNode(end));
    }

    public Label startLabel() {
        return this.start;
    }

    public Label endLabel() {
        return this.end;
    }

    public MethodNode node() {
        return this.node;
    }
}
