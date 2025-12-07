package com.jaskarth.reflow.mixin.perf;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Set;

@Mixin(Holder.Reference.class)
public class MixinHolderReference<T> {
    @Shadow
    private @Nullable Set<TagKey<T>> tags;

    /**
     * @author jaskarth
     *
     * @reason For some reason, SetN#contains is ~50% slower than HashSet or FastUtil sets (See jmh/BenchSets for more details).
     */
    @Overwrite
    public void bindTags(Collection<TagKey<T>> collection) {
        this.tags = new ObjectOpenHashSet<>(collection);
    }
}
