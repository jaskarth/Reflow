package com.jaskarth.reflow.optgraph.type;

public class TypePtr implements Type {
    public static final TypePtr BOTPTR = new TypePtr();

    @Override
    public MachType mach() {
        return MachType.PTR;
    }
}
