package org.codeberg.zenxarch.fastnoise.noise;

import java.util.Random;
import net.minecraft.util.collection.PackedIntegerArray;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class ArrayState {
  long[] small = new long[64];
  long[] big = new long[128];
  PackedIntegerArray packedArray = new PackedIntegerArray(2, 16 * 16 * 16, big);

  @Param(value = {"0", "1", "2", "3", "4", "5"})
  int seed;

  Random random = new Random(this.seed);

  @Setup(Level.Invocation)
  public void initArray() {
    for (int i = 0; i < 64; i++) {
      this.small[i] = random.nextLong();
    }
  }
}
