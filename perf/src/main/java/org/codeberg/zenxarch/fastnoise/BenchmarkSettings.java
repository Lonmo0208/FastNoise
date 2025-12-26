package org.codeberg.zenxarch.fastnoise;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.codeberg.zenxarch.fastnoise.Worldgen.PopulateNoiseFunction;

public record BenchmarkSettings(
    ChunkRegion region,
    long seed,
    RegistryKey<ChunkGeneratorSettings> settings,
    PopulateNoiseFunction function) {

  public static BenchmarkSettings vanilla(
      RegistryKey<ChunkGeneratorSettings> settings, long seed, ChunkRegion region) {
    return new BenchmarkSettings(region, seed, settings, Worldgen::vanilla);
  }

  public static BenchmarkSettings optimized(
      RegistryKey<ChunkGeneratorSettings> settings, long seed, ChunkRegion region) {
    return new BenchmarkSettings(region, seed, settings, Worldgen::optimized);
  }

  public static record ChunkRegion(int minX, int maxX, int minZ, int maxZ) {
    public static ChunkRegion of(int min, int max) {
      return new ChunkRegion(min, max, min, max);
    }
  }
}
