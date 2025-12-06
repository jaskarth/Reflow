package com.jaskarth.reflow.optgraph.nodes;

import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.runtime.ref.Refs;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

public class NoiseNode extends Node {
    private final String noise;

    public NoiseNode(@NotNull String noise, Node... children) {
        super(children);
        this.noise = noise;
    }

    @Override
    public void generate(MethodBuilder builder) {
        builder.insn(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Refs.NOISERES.get(), "sample", "(DDD)D"));
    }

    @Override
    public int req() {
        return 4;
    }

    @Override
    public String describe() {
        return "Noise=" + noise;
    }

    @Override
    public boolean eq(Node node) {
        return node instanceof NoiseNode o && o.noise.equals(noise) && o.in(1) == in(1) && o.in(2) == in(2) && o.in(3) == in(3) && o.in(4) == in(4);
    }

    @Override
    public int hash() {
        return noise.hashCode() + in(1).hashCode() + in(2).hashCode() + in(3).hashCode() + in(4).hashCode();
    }

    public enum Type {
        ShiftA,
        ShiftB
    }
}
