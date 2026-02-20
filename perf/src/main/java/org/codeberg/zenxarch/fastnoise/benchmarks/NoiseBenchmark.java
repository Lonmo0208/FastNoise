package org.codeberg.zenxarch.fastnoise.benchmarks;

import net.minecraft.world.chunk.ProtoChunk;
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

@State(Scope.Thread)
public class NoiseBenchmark {
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
    var region = ChunkRegion.of(world, settings.region());
    for (var chunk : region.chunks()) chunk.getOrCreateChunkNoiseSampler(world::createSampler);
    this.chunks = region.chunks();
  }

  @TearDown(Level.Invocation)
  public void clearChunk() {
    for (int i = 0; i < chunks.length; i++) {
      TestWorld.resetNoise(chunks[i]);
    }
  }

  @Benchmark
  public void noisegen() {
    for (int i = 0; i < chunks.length; i++) {
      world.noise(chunks[i]);
    }
  }
}
