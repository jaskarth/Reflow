package com.jaskarth.reflow.mixin.perf;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/world/level/levelgen/SurfaceSystem$1")
public class MixinSurfaceSystemBlockColumn {
//    @Redirect(method = "getBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
//    private BlockState reflow$getBlock(ChunkAccess instance, BlockPos pos) {
//        return instance.getBlockState(pos);
//    }

    // Use lockless set
    @Redirect(method = "setBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkAccess;setBlockState(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState reflow$setBlock(ChunkAccess chunk, BlockPos pos, BlockState state) {
        int y = pos.getY();
        return chunk.getSections()[chunk.getSectionIndex(y)].setBlockState(pos.getX() & 15, y & 15, pos.getZ() & 15, state, false);
    }
}
