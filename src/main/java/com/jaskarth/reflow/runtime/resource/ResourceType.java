package com.jaskarth.reflow.runtime.resource;

import com.jaskarth.reflow.runtime.ref.Ref;

import java.util.function.Supplier;

public record ResourceType(String name, Ref ref, Supplier<Resource> creator) {
}
