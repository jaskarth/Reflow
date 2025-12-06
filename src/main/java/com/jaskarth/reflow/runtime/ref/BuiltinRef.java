package com.jaskarth.reflow.runtime.ref;

public record BuiltinRef(String v) implements Ref {
    @Override
    public String get() {
        return v;
    }

    @Override
    public String asDescriptor() {
        return v;
    }
}
