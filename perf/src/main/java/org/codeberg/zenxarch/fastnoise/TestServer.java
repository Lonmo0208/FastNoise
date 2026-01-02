package org.codeberg.zenxarch.fastnoise;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.codeberg.zenxarch.fastnoise.BenchmarkSettings.ChunkRegion;

public record TestServer(TestWorld overworld, TestWorld nether, TestWorld end) {
  public static TestServer vanilla(
      DynamicRegistryManager manager,
      long seed,
      ChunkRegion overworld,
      ChunkRegion nether,
      ChunkRegion end) {
    return new TestServer(
        new TestWorld(
            manager, BenchmarkSettings.vanilla(ChunkGeneratorSettings.OVERWORLD, seed, overworld)),
        new TestWorld(
            manager, BenchmarkSettings.vanilla(ChunkGeneratorSettings.NETHER, seed, nether)),
        new TestWorld(manager, BenchmarkSettings.vanilla(ChunkGeneratorSettings.END, seed, end)));
  }

  public static TestServer optimized(
      DynamicRegistryManager manager,
      long seed,
      ChunkRegion overworld,
      ChunkRegion nether,
      ChunkRegion end) {
    return new TestServer(
        new TestWorld(
            manager,
            BenchmarkSettings.optimized(ChunkGeneratorSettings.OVERWORLD, seed, overworld)),
        new TestWorld(
            manager, BenchmarkSettings.optimized(ChunkGeneratorSettings.NETHER, seed, nether)),
        new TestWorld(manager, BenchmarkSettings.optimized(ChunkGeneratorSettings.END, seed, end)));
  }

  private static final long seed = 100;
  private static final int WorldRadiusInChunks = 16;

  public static TestServer vanillaDefault() {
    var centeredRegion =
        BenchmarkSettings.ChunkRegion.of(-WorldRadiusInChunks, WorldRadiusInChunks);
    var offsetRegion = BenchmarkSettings.ChunkRegion.of(128 - WorldRadiusInChunks, 128);
    return vanilla(PerfTest.getManager(), seed, centeredRegion, centeredRegion, offsetRegion);
  }

  public static TestServer optimizedDefault() {
    var centeredRegion =
        BenchmarkSettings.ChunkRegion.of(-WorldRadiusInChunks, WorldRadiusInChunks);
    var offsetRegion = BenchmarkSettings.ChunkRegion.of(128 - WorldRadiusInChunks, 128);
    return optimized(PerfTest.getManager(), seed, centeredRegion, centeredRegion, offsetRegion);
  }
}
