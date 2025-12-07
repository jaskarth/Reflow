package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.type.Type;
import com.jaskarth.reflow.optgraph.type.TypeDouble;
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
        Type t1 = in(1).type();
        Type t2 = in(2).type();

        // Can't operate on bottom types
        if ((t1 instanceof TypeDouble d1 && d1.isBottom()) || (t2 instanceof TypeDouble d2 && d2.isBottom())) {
            return TypeDouble.bottom();
        }

        if (t1 instanceof TypeDouble(double min1, double max1) && t2 instanceof TypeDouble(double min2, double max2)) {
            if (type == DensityFunctions.TwoArgumentSimpleFunction.Type.ADD) {
                return new TypeDouble(min1 + min2, max1 + max2);
            } else if (type == DensityFunctions.TwoArgumentSimpleFunction.Type.MUL) {
                return new TypeDouble(min1 * min2, max1 * max2);
            }
        }

        return TypeDouble.bottom();
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
