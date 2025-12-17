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
    short count = 0;
    for (int i = 0; i < data.length; i++) count += Long.bitCount(data[i]);
    counter.accept(palette.get(0), storage.getSize() - count);
    counter.accept(palette.get(1), count);
  }

  private static int fastCountSize4(long ix, long mask) {
    var t = ix ^ mask;
    return Long.bitCount((t >> 1) & t);
  }

  private static final long[] twoBitMasks =
      new long[] {~0L, ~0x5555_5555_5555_5555L, ~0xAAAA_AAAA_AAAA_AAAAL, ~0xFFFF_FFFF_FFFF_FFFFL};

  private static <T> void fastCountSize4(
      Counter<T> counter, Palette<T> palette, PaletteStorage storage) {
    int[] counts = {0, 0, 0, 0};
    var size = palette.getSize();
    var data = storage.getData();

    for (int i = 0; i < data.length; i++) {
      if (data[i] == 0) counts[0] += 32;
      else {
        counts[0] += fastCountSize4(data[i], twoBitMasks[0]);
        counts[1] += fastCountSize4(data[i], twoBitMasks[1]);
        if ((data[i] & 0xaaaa_aaaa_aaaa_aaaaL) == 0) continue;
        counts[2] += fastCountSize4(data[i], twoBitMasks[2]);
        counts[3] += fastCountSize4(data[i], twoBitMasks[3]);
      }
    }

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
