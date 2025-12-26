package org.codeberg.zenxarch.fastnoise.noise;

import net.minecraft.block.BlockState;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PalettedContainer.Data;
import org.codeberg.zenxarch.fastnoise.mixin.PalettedContainerAccessor;

public final class FastChunkSection implements PaletteResizeListener<BlockState> {

  private final ChunkSection section;

  public FastChunkSection(ChunkSection section) {
    this.section = section;
  }

  public void setBlockState(int x, int y, int z, BlockState state) {
    var blkidx = (((y << 4) | z) << 4) | x;
    var valIdx = section.blockStateContainer.data.palette().index(state, this);

    section.blockStateContainer.data.storage().zenxarch$unsafeSet(blkidx, valIdx);
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

    var canUseFastImport = true;
    for (int i = 0; i < oldData.palette().getSize(); i++) {
      if (i != newData.palette().index(oldData.palette().get(i), PaletteResizeListener.throwing()))
        canUseFastImport = false;
    }

    if (canUseFastImport) fastImport(oldData, newData, newBits);
    else newData.importFrom(oldData.palette(), oldData.storage());

    section.blockStateContainer.data = newData;

    return newData.palette().index(object, PaletteResizeListener.throwing());
  }

  private static <T> void fastImport(Data<T> oldData, Data<T> newData, int newBits) {
    var oldStorage = oldData.storage();
    var newStorage = newData.storage();
    var oldStorageData = oldStorage.getData();
    var newStorageData = newStorage.getData();

    if (newBits == 2) FastResize.fastResize1to2bits(oldStorageData, newStorageData);
    else fastImport(oldStorage, newStorage);
  }

  private static <T> void fastImport(PaletteStorage oldStorage, PaletteStorage newStorage) {
    for (int i = 0; i < oldStorage.getSize(); i++)
      newStorage.zenxarch$unsafeSet(i, oldStorage.get(i));
  }

  public void recalculateCounts() {
    section.calculateCounts();
  }
}
