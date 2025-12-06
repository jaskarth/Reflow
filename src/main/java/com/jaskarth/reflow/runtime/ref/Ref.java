package com.jaskarth.reflow.runtime.ref;

public interface Ref {
    String get();

    default String asDescriptor() {
        return "L" + get() + ";";
    }
}
