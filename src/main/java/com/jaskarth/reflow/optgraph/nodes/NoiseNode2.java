package com.jaskarth.reflow.optgraph.nodes;

import org.jetbrains.annotations.NotNull;

// TODO: why do i have 2 of these?
public class NoiseNode2 extends Node {
    private final Type type;
    private final String noise;
    private final double xzScale;
    private final double yScale;

    public NoiseNode2(Type type, @NotNull String noise, double xzScale, double yScale) {
        this.type = type;
        this.noise = noise;
        this.xzScale = xzScale;
        this.yScale = yScale;
    }

    public NoiseNode2(Type type, @NotNull String noise) {
        this(type, noise, 1, 1);
    }

    @Override
    public int req() {
        return 0;
    }

    @Override
    public String describe() {
        return type.toString() + "=" + noise;
    }

    @Override
    public boolean eq(Node node) {
        return node instanceof NoiseNode2 o && o.type == type && o.xzScale == xzScale && o.yScale == yScale && o.noise.equals(noise);
    }

    @Override
    public int hash() {
        return Integer.hashCode(type.ordinal()) + type.hashCode();
    }

    public enum Type {
        NoShift,
        ShiftA,
        ShiftB
    }
}
