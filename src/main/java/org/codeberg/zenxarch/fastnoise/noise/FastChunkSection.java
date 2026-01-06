package org.codeberg.zenxarch.fastnoise.noise;

import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PalettedContainer.Data;
import org.codeberg.zenxarch.fastnoise.mixin.PalettedContainerAccessor;

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

  @Override
  public int onResize(int newBits, BlockState object) {
    var oldData = section.blockStateContainer.data;

    @SuppressWarnings("unchecked")
    var newData =
        ((PalettedContainerAccessor<BlockState>) section.blockStateContainer)
            .zenxarch$getCompatibleData(null, newBits);

    var canUseFastImport = true;
    var mapping = new int[oldData.palette().getSize()];
    for (int i = 0; i < oldData.palette().getSize(); i++) {
      var newIdx =
          newData.palette().index(oldData.palette().get(i), PaletteResizeListener.throwing());
      if (newIdx != i) canUseFastImport = false;
      mapping[i] = newIdx;
    }

    if (canUseFastImport) fastImport(oldData, newData, newBits);
    else newData.storage().zenxarch$copy(oldData.storage(), oldData.storage().getSize(), mapping);

    section.blockStateContainer.data = newData;

    return newData.palette().index(object, PaletteResizeListener.throwing());
  }

  private static <T> void fastImport(Data<T> oldData, Data<T> newData, int newBits) {
    var oldStorage = oldData.storage();
    var newStorage = newData.storage();

    newStorage.zenxarch$copy(oldStorage, oldStorage.getSize());
  }

  public void recalculateCounts() {
    section.calculateCounts();
  }
}
