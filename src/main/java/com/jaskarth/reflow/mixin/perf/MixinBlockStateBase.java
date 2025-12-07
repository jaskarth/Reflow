package com.jaskarth.reflow.mixin.perf;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase extends StateHolder<Block, BlockState> {
    protected MixinBlockStateBase(Block object, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<BlockState> mapCodec) {
        super(object, reference2ObjectArrayMap, mapCodec);
    }

    /**
     * @author jaskarth
     *
     * @reason Reduce indirection for ore block querying
     */
    @Overwrite
    public boolean is(TagKey<Block> tagKey) {
        return this.owner.builtInRegistryHolder().tags.contains(tagKey);
    }
}
