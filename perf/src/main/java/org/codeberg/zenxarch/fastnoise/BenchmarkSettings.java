package org.codeberg.zenxarch.fastnoise;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionOptions;

public record BenchmarkSettings(
    ChunkRegion region, long seed, RegistryKey<DimensionOptions> dimensionOptions) {

  private static final long defaultSeed = 100;
  private static final int WorldRadiusInChunks = 16;

  private static ChunkRegion overworldRegion() {
    return ChunkRegion.of(-WorldRadiusInChunks, WorldRadiusInChunks);
  }

  private static ChunkRegion endRegion() {
    return ChunkRegion.of(128 - WorldRadiusInChunks, 128 + WorldRadiusInChunks);
  }

  public static BenchmarkSettings overworld() {
    return new BenchmarkSettings(overworldRegion(), defaultSeed, DimensionOptions.OVERWORLD);
  }

  public static BenchmarkSettings nether() {
    return new BenchmarkSettings(overworldRegion(), defaultSeed, DimensionOptions.NETHER);
  }

  public static BenchmarkSettings end() {
    return new BenchmarkSettings(endRegion(), defaultSeed, DimensionOptions.END);
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
