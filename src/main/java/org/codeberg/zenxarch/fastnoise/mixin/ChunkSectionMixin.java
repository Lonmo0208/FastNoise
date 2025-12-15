package org.codeberg.zenxarch.fastnoise.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.PalettedContainer.Counter;
import org.codeberg.zenxarch.fastnoise.noise.FastPaletteCount;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ChunkSection.class, priority = 500)
public abstract class ChunkSectionMixin {
  @WrapOperation(
      method = "calculateCounts",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/chunk/PalettedContainer;count(Lnet/minecraft/world/chunk/PalettedContainer;Counter;)V"))
  private void zenxarch$fastCount(
      PalettedContainer<BlockState> self, Counter<BlockState> counter, Operation<Void> op) {
    FastPaletteCount.fastCount(counter, self.data.palette(), self.data.storage());
  }
}
