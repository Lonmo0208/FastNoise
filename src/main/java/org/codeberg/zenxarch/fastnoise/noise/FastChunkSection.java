package org.codeberg.zenxarch.fastnoise.noise;

import net.minecraft.block.BlockState;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer.Data;
import net.minecraft.world.chunk.PalettedContainer.DataProvider;
import net.minecraft.world.chunk.PalettedContainer.PaletteProvider;

public final class FastChunkSection {

  private final ChunkSection section;

  private int defaultIdx = 0;
  private int minIdx = 0;

  private long[] storage;
  private ArrayPalette<BlockState> palette;
  private BlockState[] states;

  public FastChunkSection(ChunkSection section) {
    this.section = section;
  }

  public void setDefaultBlockState(int x, int y, int z, BlockState state) {
    if (defaultIdx == 0) {
      if (palette == null) {
        init(state);
        defaultIdx = 1;
        minIdx = 2;
      } else this.states[(defaultIdx = this.palette.size++)] = state;
    }
    setBlockState(x, y, z, defaultIdx);
  }

  private int getIndex(BlockState state) {
    if (palette == null) {
      init(state);
      minIdx = 1;
      return 1;
    }
    for (int i = minIdx; i < palette.size; i++) {
      if (states[i] == state) return i;
    }
    states[palette.size] = state;
    return palette.size++;
  }

  public void setBlockState(int x, int y, int z, BlockState state) {
    var valIdx = getIndex(state);
    setBlockState(x, y, z, valIdx);
  }

  private void setBlockState(int x, int y, int z, long value) {
    this.storage[(y << 4) | z] |= value << (x * 4);
  }

  private static final Palette.Factory ARRAY = PaletteProvider.ARRAY;
  private static final DataProvider ARRAY_4_TYPE = new DataProvider(ARRAY, 4);

  private void init(BlockState state) {
    this.states = new BlockState[16];
    this.states[0] = FastWorldgen.AIR;
    this.states[1] = state;
    this.storage = new long[4096 / (64 / 4)];
    this.palette =
        new ArrayPalette<>(
            this.section.blockStateContainer.idList,
            this.states,
            this.section.blockStateContainer,
            4,
            2);
    var newData =
        new Data<BlockState>(
            ARRAY_4_TYPE, new PackedIntegerArray(4, 4096, this.storage), this.palette);

    section.blockStateContainer.data = newData;
  }

  public void recalculateCounts() {
    section.calculateCounts();
  }
}
