package com.jaskarth.reflow.optgraph.nodes.ctrl;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.util.ValidationHelper;

public class IfProjNode extends CtrlNode {
    private final IfType type;

    public IfProjNode(Node ctrl, IfType type) {
        super();
        this.type = type;
        addIn(0, ctrl);
    }

    public IfType ifType() {
        return this.type;
    }

    public boolean isTrue() {
        return this.type == IfType.TRUE;
    }

    @Override
    public void generate(MethodBuilder builder) {
        super.generate(builder);

        CtrlNode region = null;
        for (Node out : outs()) {
            if (out instanceof CtrlNode ctrl && ctrl.startsBlock()) {
                ValidationHelper.assertTrue(region == null, "only one region out");
                region = ctrl;
            }
        }

        if (region != null) {
            ValidationHelper.assertTrue(region.label != null, "label must not be null");
//            builder.insn(new JumpInsnNode(Opcodes.GOTO, region.label));
        } else {
            // no region, the if at the end of the block will take care of it

        }
    }

    @Override
    public boolean startsBlock() {
        return true;
    }

    @Override
    public int req() {
        return 0;
    }

    @Override
    public String describe() {
        return "IfProj/" + type;
    }

    public enum IfType {
        TRUE,
        FALSE
    }
}
