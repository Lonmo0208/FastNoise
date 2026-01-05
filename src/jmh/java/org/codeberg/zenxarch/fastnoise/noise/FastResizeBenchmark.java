package org.codeberg.zenxarch.fastnoise.noise;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class FastResizeBenchmark {

  @Benchmark
  public void vanilla(ArrayState state, Blackhole hole) {
    for (int i = 0; i < (16 * 16 * 16); i++) {
      state.bigArray.set(i, state.smallArray.get(i));
    }
    hole.consume(state);
  }

  @Benchmark
  public void optimized(ArrayState state, Blackhole hole) {
    FastResize.fastResize1to2bits(state.small, state.big);
    hole.consume(state);
  }

  public static void fastResize1to2bits(long[] small, long[] large) {}

  private long expand(long ix) {
    long result = 0x0L;
    for (int i = 0; i < 32; i++) {
      result |= ((ix >> i) & 0x1L) << (i << 1);
    }
    return result;
  }

  @Benchmark
  public void optimizedSet(ArrayState state, Blackhole hole) {
    for (int i = 0; i < state.small.length; i++) {
      state.big[i + i] = expand(state.small[i]);
      state.big[i + i + 1] = expand(state.small[i] >>> 32);
    }
    hole.consume(state);
  }
}
