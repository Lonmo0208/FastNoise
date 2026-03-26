package org.codeberg.zenxarch.fastnoise.surface.cache;

import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.Chunk;

public final class FastChunkCache {
  private final FastSectionCache[] caches;
  private final int minY;

  public FastChunkCache(Chunk chunk, BlockState defaultState) {
    var sections = chunk.getSectionArray();
    this.caches = new FastSectionCache[sections.length];
    for (int i = 0; i < sections.length; i++)
      caches[i] = new FastSectionCache(sections[i], defaultState);
    this.minY = chunk.getBottomY();
  }

  public boolean isEmpty(int x, int y, int z) {
    var sy = (y - minY) >> 4;
    return caches[sy].isEmpty(x, z);
  }

  public FastSectionCache.STATE getState(int x, int y, int z) {
    var sy = (y - minY) >> 4;
    return caches[sy].getState(x, y & 0xF, z);
  }

  public void setStateToDefaultBlock(int x, int y, int z) {
    var sy = (y - minY) >> 4;
    caches[sy].setStateToDefault(x, y & 0xF, z);
  }
}
