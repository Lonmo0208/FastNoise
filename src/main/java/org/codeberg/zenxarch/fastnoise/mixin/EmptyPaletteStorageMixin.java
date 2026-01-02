package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.util.collection.EmptyPaletteStorage;
import org.codeberg.zenxarch.fastnoise.noise.FastPackedIntegerArray;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EmptyPaletteStorage.class)
public abstract class EmptyPaletteStorageMixin implements FastPackedIntegerArray {
  @Override
  public void zenxarch$unsafeSet(int index, int value) {}
}
