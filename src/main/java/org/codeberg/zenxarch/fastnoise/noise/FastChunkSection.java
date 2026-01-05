package org.codeberg.zenxarch.fastnoise.noise;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PalettedContainer.Data;
import org.codeberg.zenxarch.fastnoise.mixin.PalettedContainerAccessor;

public final class FastChunkSection implements PaletteResizeListener<BlockState> {

  private final ChunkSection section;
  private static final int INITIAL_SIZE = 128;

  private ShortArrayList positions = new ShortArrayList(4096);
  private ObjectList<BlockState> states = new ObjectArrayList<>(128);
  private ShortArrayList lengths = new ShortArrayList(128);

  public FastChunkSection(ChunkSection section) {
    this.section = section;
  }

  private static short pack(int x, int y, int z) {
    return (short) ((((y << 4) | z) << 4) | x);
  }

  public void setBlockState(int x, int y, int z, BlockState state) {
    positions.add(pack(x, y, z));

    if (states.isEmpty() || state != states.getLast()) {
      states.add(state);
      lengths.add((short) 1);
    } else {
      lengths.elements()[lengths.size() - 1]++;
    }
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
    var oldStorageData = oldStorage.getData();
    var newStorageData = newStorage.getData();

    if (newBits == 2) FastResize.fastResize1to2bits(oldStorageData, newStorageData);
    else newStorage.zenxarch$copy(newStorage, oldStorage.getSize());
  }

  public void recalculateCounts() {
    var posz = positions.iterator();
    for (int i = 0; i < states.size(); i++) {
      var len = lengths.getShort(i);
      var blkId = section.blockStateContainer.data.palette().index(states.get(i), this);
      for (int j = 0; j < len; j++) {
        section.blockStateContainer.data.storage().set(posz.nextShort(), blkId);
      }
    }
    section.calculateCounts();
  }
}
