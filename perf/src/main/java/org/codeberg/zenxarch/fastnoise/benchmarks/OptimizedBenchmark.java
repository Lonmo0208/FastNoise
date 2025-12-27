package org.codeberg.zenxarch.fastnoise.benchmarks;

import org.codeberg.zenxarch.fastnoise.BenchmarkSettings;
import org.codeberg.zenxarch.fastnoise.PerfTest;
import org.codeberg.zenxarch.fastnoise.TestServer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class OptimizedBenchmark {
  TestServer server;

  @Setup(Level.Trial)
  public void setup() {
    var centeredRegion = BenchmarkSettings.ChunkRegion.of(-256 / 16, 256 / 16);
    var offsetRegion = BenchmarkSettings.ChunkRegion.of((2048 - 256) / 16, 2048 / 16);

    var manager = PerfTest.getManager();

    this.server = TestServer.optimized(manager, 100, centeredRegion, centeredRegion, offsetRegion);
  }

  @Setup(Level.Invocation)
  public void clear() {
    server.overworld().clear();
    server.nether().clear();
    server.end().clear();
  }

  @Benchmark
  public void overworldOptimized() {
    server.overworld().noise();
  }

  @Benchmark
  public void netherOptimized() {
    server.nether().noise();
  }

  @Benchmark
  public void endOptimized() {
    server.end().noise();
  }
}
