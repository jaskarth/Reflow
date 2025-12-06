package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class SplineNode extends Node {
//    private final CubicSpline<DensityFunctions.Spline.Point, DensityFunctions.Spline.Coordinate> spline;

    public SplineNode(Node... nodes) {
        super(nodes);
    }

    @Override
    public int req() {
        return 2;
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.SPLINERES.get(), "sample", "(L" + Refs.FUNCTION_CTX.get() + ";)D"));
    }

    // TODO: handle caching better!
    //   - This doesn't work for now because GVN'd splines float above their caches, so they end up being eagerly evaluated.
    //   - An idealization that moves non-cache uses of splines to the Phi of the cache output needs to be created.
//    @Override
//    public boolean eq(Node node) {
//        if (node instanceof SplineNode spl) {
//            return this.spline.equals(spl.spline);
//        }
//
//        return false;
//    }
//
//    @Override
//    public int hash() {
//        return spline.hashCode();
//    }

    @Override
    public String describe() {
        return "Spline";
    }
}
