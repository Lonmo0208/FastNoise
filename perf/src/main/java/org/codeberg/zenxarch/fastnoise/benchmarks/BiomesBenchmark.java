package org.codeberg.zenxarch.fastnoise.benchmarks;

import net.minecraft.world.chunk.ProtoChunk;
import org.codeberg.zenxarch.fastnoise.BenchmarkSettings;
import org.codeberg.zenxarch.fastnoise.TestWorld;
import org.codeberg.zenxarch.fastnoise.Worldgen;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class BiomesBenchmark {
  private TestWorld world;
  private ProtoChunk[] chunks;

  @Param(value = {"overworld", "nether", "end"})
  private String worldName;

  @Setup(Level.Trial)
  public void init() {
    var settings =
        switch (worldName) {
          case "overworld" -> BenchmarkSettings.overworld();
          case "nether" -> BenchmarkSettings.nether();
          default -> BenchmarkSettings.end();
        };
    world = new TestWorld(settings);
    chunks = new ProtoChunk[settings.region().pos().length];
    for (int i = 0; i < chunks.length; i++) {
      chunks[i] = world.createChunk(settings.region().pos()[i]);
    }
  }

  @Benchmark
  public void optimizedBiomes() {
    for (int i = 0; i < chunks.length; i++) {
      Worldgen.optimizedBiomes(world, chunks[i]);
    }
  }

  @Benchmark
  public void vanillaBiomes() {
    for (int i = 0; i < chunks.length; i++) {
      Worldgen.vanillaBiomes(world, chunks[i]);
    }
  }
}
