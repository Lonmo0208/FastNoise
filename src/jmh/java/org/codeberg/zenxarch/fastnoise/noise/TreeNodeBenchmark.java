package org.codeberg.zenxarch.fastnoise.noise;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.ParameterRange;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@Threads(value = 4)
@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class TreeNodeBenchmark {
  private static final int size = 8192;
  ParameterRange[][] parameters;
  ParameterRangeImpl[][] parameterImpls;
  long[][] noise;
  Random random;

  private static record ParameterRangeImpl(long min, long max) {
    public long distance(long noise) {
      return Math.max(Math.max(noise - max, min - noise), 0);
    }
  }

  @Setup(Level.Trial)
  public void init() {
    random = new Random(0);
    parameters = new ParameterRange[size][7];
    parameterImpls = new ParameterRangeImpl[size][7];
    noise = new long[size][7];
  }

  @Setup(Level.Iteration)
  public void update() {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 7; j++) {
        var min = random.nextLong(20000) - 10000;
        var max = min + random.nextLong(10001 - min);
        parameters[i][j] = new ParameterRange(min, max);
        parameterImpls[i][j] = new ParameterRangeImpl(min, max);
      }
    }

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < 7; j++) {
        noise[i][j] = random.nextLong(20000) - 10000;
      }
    }
  }

  private long vanilla(ParameterRange[] parameters, long[] noise) {
    long l = 0L;

    for (int i = 0; i < 7; i++) {
      l += MathHelper.square(parameters[i].getDistance(noise[i]));
    }

    return l;
  }

  private long optimized(ParameterRange[] parameters, long[] noise) {
    long result = 0L;

    for (int i = 0; i < 7; i++) {
      final long noiseVal = noise[i];
      final long max = parameters[i].max();

      if (noiseVal > max) {
        long distance = noiseVal - max;
        result += distance * distance;
      } else {
        final long min = parameters[i].min();
        if (min > noiseVal) {
          long distance = min - noiseVal;
          result += distance * distance;
        }
      }
    }

    return result;
  }

  private long alternate(ParameterRangeImpl[] parameters, long[] noise) {
    long result = 0L;

    for (int i = 0; i < 7; i++) {
      result += MathHelper.square(parameters[i].distance(noise[i]));
    }

    return result;
  }

  @Benchmark
  @OperationsPerInvocation(8192 * 8192)
  public void vanillaBenchmark(Blackhole hole) {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        hole.consume(this.vanilla(this.parameters[i], this.noise[j]));
      }
    }
  }

  @Benchmark
  @OperationsPerInvocation(8192 * 8192)
  public void optimizedBenchmark(Blackhole hole) {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        hole.consume(this.optimized(this.parameters[i], this.noise[j]));
      }
    }
  }

  @Benchmark
  @OperationsPerInvocation(8192 * 8192)
  public void alternateBenchmark(Blackhole hole) {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        hole.consume(this.alternate(this.parameterImpls[i], this.noise[j]));
      }
    }
  }
}
