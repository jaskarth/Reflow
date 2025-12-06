package com.jaskarth.reflow.runtime.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceTracker {
    private final Map<ResourceType, List<ResourceLease>> resources = new HashMap<>();
    private final Map<String, Integer> names = new HashMap<>();
    // TODO: map nodes->lease! needed to track liveness

    public ResourceLease request(ResourceType type) {
        ResourceLease lease = new ResourceLease(type);
        resources.computeIfAbsent(type, k -> new ArrayList<>()).add(lease);

        return lease;
    }

    public void fillNames() {
        for (ResourceLease lease : leases()) {
            int val = names.compute(lease.type().name(), (k, v) -> v == null ? 0 : v + 1);
            lease.setName(lease.type().name() + "_" + val);
        }
    }

    public List<ResourceLease> leases() {
        List<ResourceLease> list = new ArrayList<>();
        for (List<ResourceLease> value : resources.values()) {
            list.addAll(value);
        }

        return list;
    }
}
