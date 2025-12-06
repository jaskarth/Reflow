package com.jaskarth.reflow.runtime.resource;

import com.jaskarth.reflow.util.ValidationHelper;

public class ResourceLease {
    private final ResourceType type;
    private String name;

    public ResourceLease(ResourceType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType type() {
        return type;
    }

    public String getName() {
        ValidationHelper.assertTrue(name != null, "resource is unleased");

        return name;
    }
}
