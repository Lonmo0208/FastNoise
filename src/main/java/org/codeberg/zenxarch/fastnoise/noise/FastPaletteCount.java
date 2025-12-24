package org.codeberg.zenxarch.fastnoise.noise;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer.Counter;

public final class FastPaletteCount {
  /**
   * @implNote Assumes storage size is 4096
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
    int count = 0;
    for (int i = 0; i < data.length; i++) count += Long.bitCount(data[i]);
    counter.accept(palette.get(0), storage.getSize() - count);
    counter.accept(palette.get(1), count);
  }

  private static void fastCountSize4(int[] counts, long ix) {
    counts[0] += Long.bitCount((~ix >> 1) & ~ix & 0x5555555555555555l);
    counts[1] += Long.bitCount((~ix >> 1) & ix & 0x5555555555555555l);
    counts[2] += Long.bitCount((ix >> 1) & ~ix & 0x5555555555555555l);
    counts[3] += Long.bitCount((ix >> 1) & ix & 0x5555555555555555l);
  }

  private static <T> void fastCountSize4(
      Counter<T> counter, Palette<T> palette, PaletteStorage storage) {
    int[] counts = {0, 0, 0, 0};
    var size = palette.getSize();
    var data = storage.getData();

    for (int i = 0; i < data.length; i++) fastCountSize4(counts, data[i]);

    for (int i = 0; i < size; i++) if (counts[i] != 0) counter.accept(palette.get(i), counts[i]);
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
