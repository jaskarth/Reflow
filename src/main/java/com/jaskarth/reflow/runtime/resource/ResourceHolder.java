package com.jaskarth.reflow.runtime.resource;

import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.ArrayList;
import java.util.List;

public class ResourceHolder {
    private final List<Resource> resources;

    public ResourceHolder(List<Resource> resources) {
        this.resources = resources;
    }

    public ResourceHolder remake(DensityFunction.Visitor visitor) {
        List<Resource> resources = new ArrayList<>();

        for (int i = 0; i < this.resources.size(); i++) {
            resources.add(this.resources.get(i).remake(visitor));
        }

        return new ResourceHolder(resources);
    }

    public Resource get(int i) {
        return resources.get(i);
    }

    public List<Resource> getAll() {
        return resources;
    }
}
