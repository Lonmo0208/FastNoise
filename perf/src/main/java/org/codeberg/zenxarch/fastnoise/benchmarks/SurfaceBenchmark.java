package org.codeberg.zenxarch.fastnoise.benchmarks;

import net.minecraft.world.chunk.ChunkStatus;
import org.codeberg.zenxarch.fastnoise.BenchmarkSettings;
import org.codeberg.zenxarch.fastnoise.ChunkRegion;
import org.codeberg.zenxarch.fastnoise.TestWorld;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class SurfaceBenchmark {
  private TestWorld world;
  private ChunkRegion chunks;
  private ChunkRegion biomeSource;

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

    this.chunks = ChunkRegion.of(world, settings.region());
    this.biomeSource = ChunkRegion.of(world, settings.biomeRegion());

    for (int i = 0; i < chunks.chunks().length; i++) {
      world.biomes(chunks.chunks()[i]);
      world.noise(chunks.chunks()[i]);
      chunks.chunks()[i].setStatus(ChunkStatus.NOISE);
    }

    for (int i = 0; i < biomeSource.chunks().length; i++) {
      world.biomes(biomeSource.chunks()[i]);
      world.noise(biomeSource.chunks()[i]);
      biomeSource.chunks()[i].setStatus(ChunkStatus.BIOMES);
    }
  }

  @TearDown(Level.Invocation)
  public void clearChunk() {
    chunks.copyNoiseAndHeightmap(biomeSource);
  }

  @Benchmark
  public void surface() {
    for (int i = 0; i < chunks.chunks().length; i++) {
      world.surface(chunks.chunks()[i], biomeSource);
    }
  }
}
