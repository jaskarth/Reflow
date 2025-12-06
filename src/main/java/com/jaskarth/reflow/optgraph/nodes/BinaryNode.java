package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeD;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;

public class BinaryNode extends Node {
    private final DensityFunctions.TwoArgumentSimpleFunction.Type type;

    public BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type type, Node... nodes) {
        super(nodes);
        this.type = type;
    }

    @Override
    public Node idealize(NodeCache nodes) {
        Node in1 = in(1);
        Node in2 = in(2);

        if (type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
            if (swapArms(nodes)) {
                return this;
            }

            // (x + i1) + i0 => x + (i1 + i0)
            if (in1 instanceof BinaryNode bin1 && in2 instanceof ImmDNode imm1) {
                if (bin1.type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD && bin1.in(2) instanceof ImmDNode imm2) {
                    Node con = nodes.get(new ImmDNode(imm1.getValue() + imm2.getValue()));
                    return nodes.get(new BinaryNode(DensityFunctions.TwoArgumentSimpleFunction.Type.ADD, bin1.in(1), con));
                }
            }

            // x + 0 => x

            if (in2 instanceof ImmDNode imm2 && imm2.getValue() == 0) {
                return in(1);
            }
        } else if (type == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL) {
            if (swapArms(nodes)) {
                return this;
            }

            // x * 1 => x
            if (in2 instanceof ImmDNode imm2 && imm2.getValue() == 1) {
                return in(1);
            }
        }

        return null;
    }

    @Override
    public void generate(MethodBuilder builder) {
        switch (this.type) {
            case ADD -> builder.insn(new InsnNode(Opcodes.DADD));
            case MUL -> builder.insn(new InsnNode(Opcodes.DMUL));
        }
    }

    private boolean swapArms(NodeCache nodes) {
        Node in1 = in(1);
        Node in2 = in(2);
        if (in1 instanceof ImmDNode && !(in2 instanceof ImmDNode)) {
            // swap inputs
            setIn(2, in1);
            setIn(1, in2);

            return true;
        }

        return false;
    }

    @Override
    public Type type() {
        Type t0 = in(1).type();
        Type t1 = in(2).type();

        // Can't operate on bottom types
        if (t0 instanceof TypeD d0 && d0.isDBottom()) {
            return TypeD.bottom();
        }
        if (t1 instanceof TypeD d1 && d1.isDBottom()) {
            return TypeD.bottom();
        }

        if (t0 instanceof TypeD(double min0, double max0) && t1 instanceof TypeD(double min1, double max1)) {
            if (type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
                return new TypeD(min0 + min1, max0 + max1);
            } else if (type == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL) {
                return new TypeD(min0 * min1, max0 * max1);
            }
        }

        return Type.BOTTOM;
    }

    @Override
    public boolean eq(Node node) {
        if (node instanceof BinaryNode o) {
            return o.type == type && in(1) == o.in(1) && in(2) == o.in(2);
        }

        return false;
    }

    @Override
    public int hash() {
        return Integer.hashCode(type.ordinal()) + (in(1).hashCode() * 31) + in(2).hashCode();
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public String describe() {
        return "" + type;
    }
}
