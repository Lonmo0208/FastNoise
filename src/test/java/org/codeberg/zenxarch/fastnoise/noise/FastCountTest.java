package org.codeberg.zenxarch.fastnoise.noise;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Random;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.PaletteProvider;
import net.minecraft.world.chunk.PaletteResizeListener;
import net.minecraft.world.chunk.PalettedContainer.Counter;
import org.codeberg.zenxarch.fastnoise.mixin.PaletteProviderAccessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

public class FastCountTest {

  private PaletteProvider<BlockState> paletteProvider;

  @BeforeAll
  public void init() {
    SharedConstants.createGameVersion();
    Bootstrap.initialize();
    this.paletteProvider = PaletteProvider.forBlockStates(Block.STATE_IDS);
  }

  @RepeatedTest(4)
  private void fastCount2() {
    fastCount(16 * 16 * 16, 1);
    fastCount(16 * 16 * 16, 2);

    fastCount(3, 1);
    fastCount(17, 2);
  }

  @RepeatedTest(4)
  private void fastCount4() {
    fastCount(16 * 16 * 16, 3);
    fastCount(16 * 16 * 16, 4);

    fastCount(72141, 3);
    fastCount(1721, 4);
  }

  @RepeatedTest(4)
  private void fastCount16() {
    fastCount(16 * 16 * 16, 17);
    fastCount(16 * 16 * 16, 32);
  }

  @RepeatedTest(4)
  private void fastCount64() {
    fastCount(16 * 16 * 16, 100);
    fastCount(16 * 16 * 16, 111);
  }

  private void fastCount(int size, int count) {
    var bits = MathHelper.ceilLog2(count);
    var paletteType = ((PaletteProviderAccessor) this.paletteProvider).zenxarch$createType(bits);

    var storage = new PackedIntegerArray(bits, size);
    var palette = paletteType.createPalette(paletteProvider, List.of());

    for (int i = 0; i < count; i++)
      palette.index(paletteProvider.getIdList().get(i), PaletteResizeListener.throwing());

    var random = new Random();
    for (int i = 0; i < size; i++) {
      storage.set(i, random.nextInt(count));
    }

    var counter = new TestCounter();

    {
      var counts = new Int2IntOpenHashMap();
      storage.forEach(key -> counts.addTo(key, 1));
      counts
          .int2IntEntrySet()
          .forEach(entry -> counter.accept(palette.get(entry.getIntKey()), entry.getIntValue()));
    }

    FastPaletteCount.fastCount(counter, palette, storage);
  }

  private static class TestCounter implements Counter<BlockState> {
    private Object2IntMap<BlockState> counts = new Object2IntOpenHashMap<>();

    @Override
    public void accept(BlockState state, int count) {
      if (counts.containsKey(state)) Assertions.assertEquals(counts.apply(state), count);
      else counts.put(state, count);
    }
  }
}
