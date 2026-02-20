package org.codeberg.zenxarch.fastnoise.benchmarks;

import net.minecraft.world.chunk.ProtoChunk;
import org.codeberg.zenxarch.fastnoise.BenchmarkSettings;
import org.codeberg.zenxarch.fastnoise.ChunkRegion;
import org.codeberg.zenxarch.fastnoise.TestWorld;
import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
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
    var region = ChunkRegion.of(world, settings.region());
    this.chunks = region.chunks();
  }

  @TearDown(Level.Invocation)
  public void clearChunk() {
    for (int i = 0; i < chunks.length; i++) {
      TestWorld.resetBiomes(chunks[i]);
    }
  }

  @Benchmark
  public void biomegen() {
    for (int i = 0; i < chunks.length; i++) {
      world.biomes(chunks[i]);
    }
  }
}
