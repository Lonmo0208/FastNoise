package org.codeberg.zenxarch.fastnoise.noise;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer.Counter;

public final class FastPaletteCount {
  /**
   * @implNote Assumes storage size i 4096
   */
  public static <T> void fastCount(Counter<T> counter, Palette<T> palette, PaletteStorage storage) {
    var size = palette.getSize();
    switch (size) {
      case 1:
        counter.accept(palette.get(0), storage.getSize());
        break;
      case 2:
        fastCountSize2(counter, palette, storage);
        break;
      case 3, 4:
        fastCountSize4(counter, palette, storage);
        break;
      default:
        fastCountSizeMoreThan4(counter, palette, storage);
        break;
    }
  }

  private static <T> void fastCountSize2(
      Counter<T> counter, Palette<T> palette, PaletteStorage storage) {
    var data = storage.getData();
    short count = 0;
    for (int i = 0; i < data.length; i++) count += Long.bitCount(data[i]);
    counter.accept(palette.get(0), storage.getSize() - count);
    counter.accept(palette.get(1), count);
  }

  private static int fastCountSize4(long ix, long mask) {
    var t = ix ^ mask;
    return Long.bitCount((t >> 1) & t);
  }

  private static <T> void fastCountSize4(
      Counter<T> counter, Palette<T> palette, PaletteStorage storage) {
    final long mask0 = ~0L;
    final long mask1 = 0x5555_5555_5555_5555L << 1;
    final long mask2 = 0x5555_5555_5555_5555L;
    final long mask3 = 0L;

    int count0 = 0;
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;

    var size = palette.getSize();
    var data = storage.getData();

    for (int i = 0; i < data.length; i++) {
      if (data[i] == 0) {
        count0 += 32;
        continue;
      }
      count0 += fastCountSize4(data[i], mask0);
      count1 += fastCountSize4(data[i], mask1);
      count2 += fastCountSize4(data[i], mask2);
      count3 += fastCountSize4(data[i], mask3);
    }

    if (count0 != 0) counter.accept(palette.get(0), count0);
    if (count1 != 0) counter.accept(palette.get(1), count1);
    if (count2 != 0) counter.accept(palette.get(2), count2);
    if (count3 != 0) if (size == 4) counter.accept(palette.get(3), count3);
  }

  private static <T> void fastCountSizeMoreThan4(
      Counter<T> counter, Palette<T> palette, PaletteStorage storage) {
    if (palette.getSize() <= 64) fastCountSizeLessThan64(counter, palette, storage);
    else {
      var counts = new Int2IntOpenHashMap();
      storage.forEach(key -> counts.addTo(key, 1));
      counts
          .int2IntEntrySet()
          .forEach(entry -> counter.accept(palette.get(entry.getIntKey()), entry.getIntValue()));
    }
  }

  private static <T> void fastCountSizeLessThan64(
      Counter<T> counter, Palette<T> palette, PaletteStorage storage) {
    var count = new int[palette.getSize()];
    var storageSize = storage.getSize();
    for (int i = 0; i < storageSize; i++) count[storage.get(i)]++;
    for (int i = 0; i < count.length; i++)
      if (count[i] > 0) counter.accept(palette.get(i), count[i]);
  }
}
