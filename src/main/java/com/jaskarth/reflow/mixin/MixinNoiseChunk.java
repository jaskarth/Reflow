package com.jaskarth.reflow.mixin;

import com.jaskarth.reflow.compile.DotExporter;
import com.jaskarth.reflow.compile.Parser;
import com.jaskarth.reflow.runtime.ReflowFunc;
import com.jaskarth.reflow.runtime.ReflowDensityFunction;
import com.jaskarth.reflow.runtime.resource.InitCtx;
import com.jaskarth.reflow.runtime.resource.Resource;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(NoiseChunk.class)
public class MixinNoiseChunk {
    @Shadow @Final
    int firstNoiseX;

    @Shadow @Final
    int firstNoiseZ;

    @Shadow
    @Final
    private DensityFunction preliminarySurfaceLevel;

    private final InitCtx reflow$initCtx = new InitCtx();

    //@Inject(method = "<init>", at = @At("TAIL"))
    public void __init(int i, RandomState randomState, int j, int k, NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifierOrMarker, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blender, CallbackInfo ci) {
//        DotExporter.export("ForChunk", Parser.parseFully(this.preliminarySurfaceLevel));

        // Too lazy to breakpoint and kill...
        System.exit(1);
    }

    @Inject(method = "wrapNew", at = @At("HEAD"))
    private void reflow$setupState(DensityFunction func, CallbackInfoReturnable<DensityFunction> cir) {
        if (func instanceof ReflowDensityFunction(ReflowFunc r)) {
            for (Resource res : r.resources().getAll()) {
                res.init(reflow$initCtx, this.firstNoiseX, this.firstNoiseZ);
            }
        }
    }
}
