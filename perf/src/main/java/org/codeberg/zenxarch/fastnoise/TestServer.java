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
}
