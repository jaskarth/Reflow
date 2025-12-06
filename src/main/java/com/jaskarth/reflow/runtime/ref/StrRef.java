package com.jaskarth.reflow.runtime.ref;

public record StrRef(String v) implements Ref {
    @Override
    public String get() {
        return v;
    }
}
