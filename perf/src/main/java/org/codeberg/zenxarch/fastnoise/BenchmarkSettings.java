package org.codeberg.zenxarch.fastnoise;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
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

  public static record ChunkRegion(ChunkPos[] pos) {
    public static ChunkRegion of(int min, int max) {
      var pos = new ChunkPos[(max + 1 - min) * (max + 1 - min)];
      int idx = 0;
      for (int x = min; x <= max; x++) {
        for (int z = min; z <= max; z++) {
          pos[idx++] = new ChunkPos(x, z);
        }
      }
      return new ChunkRegion(pos);
    }
  }
}
