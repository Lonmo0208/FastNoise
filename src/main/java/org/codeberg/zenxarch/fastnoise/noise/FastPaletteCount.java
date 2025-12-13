package org.codeberg.zenxarch.fastnoise.noise;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer.Counter;

public final class FastPaletteCount {
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

  private static final long[] fastCountSize4Table = initFastCountSize4Table();

  private static long[] initFastCountSize4Table() {
    var result = new long[256];
    final long[] count = {1L, 1L << 16, 1L << 32, 1L << 48};

    for (long i = 0; i < 256; i++) {
      var ia = i & 0b11;
      var ib = (i >> 2) & 0b11;
      var ic = (i >> 4) & 0b11;
      var id = (i >> 6) & 0b11;
      result[(int) i] = count[(int) ia] + count[(int) ib] + count[(int) ic] + count[(int) id];
    }

    return result;
  }

  private static long fastCountSize4(long ix) {
    long result = 0;
    result += fastCountSize4Table[(int) ((ix >> 0) & 0xFF)];
    result += fastCountSize4Table[(int) ((ix >> 8) & 0xFF)];
    result += fastCountSize4Table[(int) ((ix >> 16) & 0xFF)];
    result += fastCountSize4Table[(int) ((ix >> 24) & 0xFF)];
    result += fastCountSize4Table[(int) ((ix >> 32) & 0xFF)];
    result += fastCountSize4Table[(int) ((ix >> 40) & 0xFF)];
    result += fastCountSize4Table[(int) ((ix >> 48) & 0xFF)];
    result += fastCountSize4Table[(int) ((ix >> 56) & 0xFF)];
    return result;
  }

  private static <T> void fastCountSize4(
      Counter<T> counter, Palette<T> palette, PaletteStorage storage) {
    if (!canUseFastCountSize4(storage.getSize())) {
      fastCountSizeMoreThan4(counter, palette, storage);
      return;
    }
    long count = 0;
    var size = palette.getSize();
    var data = storage.getData();
    for (int i = 0; i < data.length; i++) {
      count += fastCountSize4(data[i]);
    }

    counter.accept(palette.get(0), (int) (count & 0xFFFF));
    counter.accept(palette.get(1), (int) ((count >> 16) & 0xFFFF));
    counter.accept(palette.get(2), (int) ((count >> 32) & 0xFFFF));
    if (size == 4) counter.accept(palette.get(3), (int) ((count >> 48) & 0xFFFF));
  }

  private static boolean canUseFastCountSize4(int size) {
    if (size > 0x1000000) return false;
    return (size & 0x1F) == 0;
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
