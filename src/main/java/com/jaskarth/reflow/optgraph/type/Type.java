package com.jaskarth.reflow.optgraph.type;

import com.jaskarth.reflow.optgraph.nodes.Node;
import org.jetbrains.annotations.Nullable;

public interface Type {
    Type BOTTOM = new Bottom();

    default @Nullable Node constant() {
        return null;
    }

    default Type meet(Type other) {
        throw new IllegalStateException("Meet not implemented");
    }

    boolean isBottom();

    MachType mach();

    record Bottom() implements Type {

        @Override
        public Type meet(Type other) {
            return BOTTOM;
        }

        @Override
        public MachType mach() {
            return MachType.BOTTOM;
        }

        @Override
        public boolean isBottom() {
            return true;
        }
    }
}
