package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.world.chunk.PaletteProvider;
import net.minecraft.world.chunk.PalettedContainer;
import org.codeberg.zenxarch.fastnoise.noise.FastPaletteCount;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> extends PalettedContainer<T> {
  public PalettedContainerMixin(T defaultValue, PaletteProvider<T> paletteProvider) {
    super(defaultValue, paletteProvider);
  }

  @Override
  public void count(Counter<T> counter) {
    @SuppressWarnings("unchecked")
    var self = (PalettedContainer<T>) (Object) this;
    var palette = self.data.palette();
    var storage = self.data.storage();
    FastPaletteCount.fastCount(counter, palette, storage);
  }
}
