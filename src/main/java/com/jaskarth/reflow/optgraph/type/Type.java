package com.jaskarth.reflow.optgraph.type;

import com.jaskarth.reflow.optgraph.nodes.Node;
import org.jetbrains.annotations.Nullable;

public interface Type {
    Type BOTTOM = new Bottom();

    default @Nullable Node constant() {
        return null;
    }

    // FIXME
    default Type meet(Type t1, Type t2) {
        return null;
    }

    MachType mach();

    record Bottom() implements Type {

        @Override
        public MachType mach() {
            return MachType.BOTTOM;
        }
    }
}
