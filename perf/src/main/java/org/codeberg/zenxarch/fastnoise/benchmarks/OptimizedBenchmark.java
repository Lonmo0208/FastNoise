package org.codeberg.zenxarch.fastnoise.benchmarks;

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
    this.server = TestServer.optimizedDefault();
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
