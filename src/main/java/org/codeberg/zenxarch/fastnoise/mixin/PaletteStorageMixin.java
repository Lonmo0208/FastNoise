package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.util.collection.PaletteStorage;
import org.codeberg.zenxarch.fastnoise.noise.FastPackedIntegerArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PaletteStorage.class)
public interface PaletteStorageMixin extends FastPackedIntegerArray {
  @Shadow
  void set(int index, int value);

  @Shadow
  int get(int index);

  @Override
  default void zenxarch$unsafeSet(int index, int value) {
    set(index, value);
  }

  @Override
  default int zenxarch$unsafeGet(int index) {
    return get(index);
  }
}
