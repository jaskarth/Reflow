package com.jaskarth.reflow.optgraph.nodes;

import org.jetbrains.annotations.NotNull;

public class ShiftedNode extends Node {
    @NotNull
    private final String noise;
    private final double xzScale;
    private final double yScale;

    public ShiftedNode(@NotNull String noise, double xzScale, double yScale, Node... children) {
        super(children);
        this.noise = noise;
        this.xzScale = xzScale;
        this.yScale = yScale;
    }

//    @Override
//    public boolean eq(Node node) {
//        if (node instanceof ShiftedNode o) {
//            return o.noise.equals(noise) && o.xzScale == xzScale && o.yScale == yScale
//                    && in(1).eq(o.in(1)) && in(2).eq(o.in(2)) && in(3).eq(o.in(3));
//        }
//
//        return false;
//    }
//
//    @Override
//    public int hash() {
//        return noise.hashCode() + Double.hashCode(xzScale) + Double.hashCode(yScale) + in(1).hash() + in(2).hash() + in(3).hash();
//    }

    @Override
    public int req() {
        return 3;
    }

    @Override
    public String describe() {
        return "Shifted(" + xzScale + "," + yScale + ")=" + noise;
    }
}
