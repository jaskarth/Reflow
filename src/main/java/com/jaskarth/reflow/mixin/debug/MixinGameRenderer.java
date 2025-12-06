package com.jaskarth.reflow.mixin.debug;

import com.jaskarth.reflow.compile.StaticCompileData;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void reflow$debug(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci, ProfilerFiller profilerFiller, boolean b, GuiGraphics context) {
        String currentlyCompiling = StaticCompileData.currentlyCompiling;
        if (!currentlyCompiling.isEmpty()) {
            context.drawString(Minecraft.getInstance().font, "Reflow: Compiling " + currentlyCompiling, 1, 1, 0xFFFFFFFF);
        }
    }
}
