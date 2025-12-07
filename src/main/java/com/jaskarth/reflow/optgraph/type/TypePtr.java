package com.jaskarth.reflow.optgraph.type;

public class TypePtr implements Type {
    public static final TypePtr BOTPTR = new TypePtr();

    @Override
    public Type meet(Type other) {
        return BOTPTR;
    }

    @Override
    public boolean isBottom() {
        return this == BOTPTR;
    }

    @Override
    public MachType mach() {
        return MachType.PTR;
    }
}
