package com.jaskarth.reflow.optgraph.nodes.vars;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.util.ValidationHelper;
import org.objectweb.asm.tree.VarInsnNode;

public class SpillStoreNode extends Node {
    private int lv = -1;

    public SpillStoreNode(Node in) {
        super(in);
    }

    public void setIdx(int lv) {
        this.lv = lv;
    }

    @Override
    public void generate(MethodBuilder builder) {
        ValidationHelper.assertTrue(lv != -1, "must have an lv");
        builder.insn(new VarInsnNode(type().mach().storeOp(), lv));
    }

    @Override
    public Type type() {
        return in(1).type();
    }

    @Override
    public int req() {
        return 1;
    }

    @Override
    public String describe() {
        return "Spill/Store=" + lv;
    }
}
