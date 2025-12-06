package com.jaskarth.reflow;

import com.jaskarth.reflow.runtime.ReflowDensityFunction;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class Reflow implements ModInitializer {

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.DENSITY_FUNCTION_TYPE, id("reflow"), ReflowDensityFunction.CODEC.codec());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("reflow", path);
    }
}
