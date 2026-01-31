package org.codeberg.zenxarch.fastnoise.heightmap;

import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.ChunkSection;

public final class HeightmapUtil {
  private HeightmapUtil() {
    throw new IllegalStateException("Utility class");
  }

  public static void updateHeightmap(
      int lx,
      int lz,
      PaletteStorage storage,
      Predicate<BlockState> predicate,
      BlockState state,
      int y,
      ChunkSection[] sections) {
    int idx = lx + (lz << 4);
    int current = storage.get(idx);
    if ((y + 2) > current) {
      if (predicate.test(state)) {
        if (y >= current) storage.set(idx, y + 1);
      } else { // go down the whole chunk till we hit something
        storage.set(idx, getHeightmapY(idx, sections, y - 1, predicate));
      }
    }
  }

  private static int getHeightmapY(
      int hidx, ChunkSection[] sections, int startY, Predicate<BlockState> predicate) {
    int ly = startY & 15;
    int sy = startY >> 4;
    while (sy >= 0) {
      var section = sections[sy];
      var palette = section.blockStateContainer.data.palette();
      var storage = section.blockStateContainer.data.storage();
      while (ly >= 0) {
        if (predicate.test(palette.get(storage.get((ly << 8) + hidx)))) {
          return (sy << 4) + ly + 1;
        }
        ly--;
      }
      ly = 15;
      sy--;
    }

    return 0;
  }
}
