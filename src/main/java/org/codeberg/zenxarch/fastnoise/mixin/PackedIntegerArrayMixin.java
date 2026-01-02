package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.util.collection.PackedIntegerArray;
import org.codeberg.zenxarch.fastnoise.noise.FastPackedIntegerArray;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PackedIntegerArray.class)
public abstract class PackedIntegerArrayMixin implements FastPackedIntegerArray {

  @Shadow @Final private long[] data;
  @Shadow @Final private int elementBits;
  @Shadow @Final private long maxValue;
  @Shadow @Final private int elementsPerLong;

  @Shadow
  abstract int getStorageIndex(int index);

  @Override
  public void zenxarch$unsafeSet(int index, int value) {
    int idx = this.getStorageIndex(index);
    int bitIdx = (index - idx * this.elementsPerLong) * this.elementBits;
    this.data[idx] =
        this.data[idx] & ~(this.maxValue << bitIdx) | ((long) value & this.maxValue) << bitIdx;
  }
}
