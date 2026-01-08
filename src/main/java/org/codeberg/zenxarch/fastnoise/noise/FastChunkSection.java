package org.codeberg.zenxarch.fastnoise.noise;

import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
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

  private Reference2IntArrayMap<BlockState> fluids = new Reference2IntArrayMap<>(2);
  private Reference2IntArrayMap<BlockState> ores = new Reference2IntArrayMap<>(3);

  private int defaultIdx = -1;

  private long[] storage;

  public FastChunkSection(ChunkSection section) {
    this.section = section;
  }

  public void setDefaultBlockState(int x, int y, int z, BlockState state) {
    if (defaultIdx == -1)
      defaultIdx = section.blockStateContainer.data.palette().index(state, this);
    setBlockState(x, y, z, defaultIdx);
  }

  private int getIndex(BlockState state) {
    if (state == lastState) return lastIdx;
    if (state.getFluidState().isEmpty())
      return ores.computeIfAbsent(
          state, statex -> section.blockStateContainer.data.palette().index(state, this));
    return fluids.computeIfAbsent(
        state, statex -> section.blockStateContainer.data.palette().index(state, this));
  }

  public void setBlockState(int x, int y, int z, BlockState state) {
    var valIdx = getIndex(state);

    lastState = state;
    lastIdx = valIdx;
    setBlockState(x, y, z, valIdx);
  }

  private void setBlockState(int x, int y, int z, int value) {
    this.storage[(y << 4) | z] |= Integer.toUnsignedLong(value) << (x * 4);
  }

  private static final Palette.Factory ARRAY = ArrayPalette::create;
  private static final PaletteType ARRAY_4_TYPE = new PaletteType.Static(ARRAY, 4);

  @Override
  public int onResize(int newBits, BlockState state) {
    var paletteData = new BlockState[16];
    paletteData[0] = FastWorldgen.AIR;
    paletteData[1] = state;
    this.storage = new long[4096 / (64 / 4)];
    var newData =
        new Data<BlockState>(
            ARRAY_4_TYPE,
            new PackedIntegerArray(4, 4096, this.storage),
            new ArrayPalette<BlockState>(paletteData, 4, 2));

    section.blockStateContainer.data = newData;
    return 1;
  }

  public void recalculateCounts() {
    section.calculateCounts();
  }
}
