package com.jaskarth.reflow.optgraph.type;

import org.objectweb.asm.Opcodes;

public enum MachType {
    BOTTOM,

    DOUBLE,
    INT,
    LONG,
    PTR;

    public int storeOp() {
        return switch (this) {
            case DOUBLE -> Opcodes.DSTORE;
            case INT -> Opcodes.ISTORE;
            case LONG -> Opcodes.LSTORE;
            case PTR -> Opcodes.ASTORE;
            default -> throw new IllegalStateException("cannot store this op");
        };
    }

    public int loadOp() {
        return switch (this) {
            case DOUBLE -> Opcodes.DLOAD;
            case INT -> Opcodes.ILOAD;
            case LONG -> Opcodes.LLOAD;
            case PTR -> Opcodes.ALOAD;
            default -> throw new IllegalStateException("cannot load this op");
        };
    }

    public int slotSize() {
        return switch (this) {
            case LONG, DOUBLE -> 2;
            case INT, PTR -> 1;
            default -> throw new IllegalStateException("illegal op for stack size");
        };
    }

}
