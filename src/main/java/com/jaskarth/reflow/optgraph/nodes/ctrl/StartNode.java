package com.jaskarth.reflow.optgraph.nodes.ctrl;

public class StartNode extends CtrlNode {
    @Override
    public int req() {
        return 0;
    }

    @Override
    public boolean startsBlock() {
        return true;
    }

    @Override
    public String describe() {
        return "Start";
    }
}
