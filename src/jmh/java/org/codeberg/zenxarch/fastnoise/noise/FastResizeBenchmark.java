package org.codeberg.zenxarch.fastnoise.noise;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class FastResizeBenchmark {

  @Benchmark
  public void vanilla(ArrayState state, Blackhole hole) {
    for (int i = 0; i < (16 * 16 * 16); i++) {
      state.packedArray.set(i, (int) ((state.small[i >> 6] >> (i & 0x3F)) & 0x1));
    }
    hole.consume(state);
  }

  @Benchmark
  public void optimized(ArrayState state, Blackhole hole) {
    FastResize.fastResize1to2bits(state.small, state.big);
    hole.consume(state);
  }
}
