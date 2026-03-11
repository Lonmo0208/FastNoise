package org.codeberg.zenxarch.fastnoise;

import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class WithinBoundsBenchmark {

  Random random = new Random();

  @Benchmark
  public void withinBoundsOpt(Blackhole hole) {
    hole.consume(((0x1 << (random.nextInt() & 0xF)) & ~0b0011_1111_1111_1100) == 0);
  }

  @Benchmark
  public void withinBoundsSimple(Blackhole hole) {
    var newInt = random.nextInt() & 0xF;
    hole.consume(newInt < 2 || newInt > 13);
  }
}
