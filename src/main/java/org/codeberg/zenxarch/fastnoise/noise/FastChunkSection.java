package org.codeberg.zenxarch.fastnoise.noise;

import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PaletteResizeListener;
import org.codeberg.zenxarch.fastnoise.mixin.PalettedContainerAccessor;

public final class FastChunkSection implements PaletteResizeListener<BlockState> {

  private final ChunkSection section;

  public FastChunkSection(ChunkSection section) {
    this.section = section;
  }

  public void setBlockState(int x, int y, int z, BlockState state) {
    var blkidx = (((y << 4) | z) << 4) | x;
    var valIdx = section.blockStateContainer.data.palette().index(state, this);

    ((FastPackedIntegerArray) section.blockStateContainer.data.storage())
        .zenxarch$unsafeSet(blkidx, valIdx);
  }

  @Override
  public int onResize(int newBits, BlockState object) {
    assert (newBits < 4);
    assert (newBits > 1);

    var oldData = section.blockStateContainer.data;

    @SuppressWarnings("unchecked")
    var newData =
        ((PalettedContainerAccessor<BlockState>) section.blockStateContainer)
            .zenxarch$getCompatibleData(null, newBits);

    for (int i = 0; i < oldData.palette().getSize(); i++)
      newData.palette().index(oldData.palette().get(i), PaletteResizeListener.throwing());

    var oldStorage = oldData.storage().getData();
    var newStorage = newData.storage().getData();

    if (newBits == 2) FastResize.fastResize1to2bits(oldStorage, newStorage);
    else newData.importFrom(oldData.palette(), oldData.storage());

    section.blockStateContainer.data = newData;

    return newData.palette().index(object, PaletteResizeListener.throwing());
  }

  public void recalculateCounts() {
    section.calculateCounts();
  }
}
