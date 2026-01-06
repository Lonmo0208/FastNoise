package org.codeberg.zenxarch.fastnoise.noise;

import net.minecraft.block.BlockState;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PaletteType;
import net.minecraft.world.chunk.PalettedContainer.Data;

public final class FastChunkSection implements PaletteResizeListener<BlockState> {

  private final ChunkSection section;
  private BlockState lastState = null;
  private int lastIdx = -1;

  private int defaultIdx = -1;

  public FastChunkSection(ChunkSection section) {
    this.section = section;
  }

  public void setDefaultBlockState(int x, int y, int z, BlockState state) {
    if (defaultIdx == -1)
      defaultIdx = section.blockStateContainer.data.palette().index(state, this);
    setBlockState(x, y, z, defaultIdx);
  }

  public void setBlockState(int x, int y, int z, BlockState state) {
    var valIdx =
        state == lastState
            ? lastIdx
            : section.blockStateContainer.data.palette().index(state, this);

    lastState = state;
    lastIdx = valIdx;
    setBlockState(x, y, z, valIdx);
  }

  private void setBlockState(int x, int y, int z, int value) {
    var blkidx = (((y << 4) | z) << 4) | x;
    section.blockStateContainer.data.storage().zenxarch$unsafeSet(blkidx, value);
  }

  private static final Palette.Factory ARRAY = ArrayPalette::create;
  private static final PaletteType ARRAY_4_TYPE = new PaletteType.Static(ARRAY, 4);
  private static final long[] EMPTY_DATA = new long[4096 / (64 / 4)];

  @Override
  public int onResize(int newBits, BlockState object) {
    var paletteData = new BlockState[16];
    paletteData[0] = FastWorldgen.AIR;
    var newData =
        new Data<BlockState>(
            ARRAY_4_TYPE,
            new PackedIntegerArray(4, 4096, EMPTY_DATA.clone()),
            new ArrayPalette<BlockState>(paletteData, 4, 1));

    section.blockStateContainer.data = newData;

    return newData.palette().index(object, PaletteResizeListener.throwing());
  }

  public void recalculateCounts() {
    section.calculateCounts();
  }
}
