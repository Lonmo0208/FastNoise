package org.codeberg.zenxarch.fastnoise.noise;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer.Counter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class FastCountBenchmark {
  Blackhole hole;
  Counter<Integer> counter =
      new Counter<Integer>() {
        @Override
        public void accept(Integer object, int count) {
          hole.consume(count);
        }
      };

  Palette<Integer> palette;
  PaletteStorage storage;
  Random random;

  @Param(value = {"0", "1", "2", "3", "4", "5", "6", "7"})
  int sizeIdx;

  @Setup(Level.Trial)
  public void beforeAll() {
    var list = new ArrayList<Integer>();
    for (int i = 0; i < 128; i++) list.add(i);
    palette = ArrayPalette.create(MathHelper.ceilLog2(128), list);
  }

  @Setup(Level.Iteration)
  public void init() {
    random = new Random(0);
  }

  private void fillRandomData(int count) {
    var bits = MathHelper.ceilLog2(count);
    if (bits == 0) storage = new EmptyPaletteStorage(4096);
    else storage = new PackedIntegerArray(MathHelper.ceilLog2(count), 4096);
    for (int i = 0; i < 4096; i++) storage.set(i, random.nextInt(count));
  }

  @Setup(Level.Invocation)
  public void fillWithRandomData() {
    switch (sizeIdx) {
      case 0:
        fillRandomData(1);
        break;
      case 1:
        fillRandomData(2);
        break;
      case 2:
        fillRandomData(3);
        break;
      case 3:
        fillRandomData(4);
        break;
      case 4:
        fillRandomData(6);
        break;
      case 5:
        fillRandomData(8);
        break;
      case 6:
        fillRandomData(32);
        break;
      case 7:
        fillRandomData(121);
        break;
      default:
        break;
    }
  }

  @Benchmark
  public void vanilla(Blackhole hole) {
    this.hole = hole;
    var counts = new Int2IntOpenHashMap();
    storage.forEach(key -> counts.addTo(key, 1));
    counts
        .int2IntEntrySet()
        .forEach(entry -> counter.accept(palette.get(entry.getIntKey()), entry.getIntValue()));
  }

  @Benchmark
  public void optimized(Blackhole hole) {
    this.hole = hole;
    FastPaletteCount.fastCount(counter, palette, storage);
  }
}
