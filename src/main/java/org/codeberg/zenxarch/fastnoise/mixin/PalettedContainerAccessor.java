package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.world.chunk.PalettedContainer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PalettedContainer.class)
public interface PalettedContainerAccessor<T> {
  @Invoker("getCompatibleData")
  PalettedContainer.Data<T> zenxarch$getCompatibleData(
      @Nullable PalettedContainer.Data<T> previousData, int bits);
}
