package org.codeberg.zenxarch.fastnoise;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionOptions;

public record BenchmarkSettings(
    ChunkRegion region,
    ChunkRegion biomeRegion,
    long seed,
    RegistryKey<DimensionOptions> dimensionOptions) {

  private static final long defaultSeed = 100;
  private static final int WorldRadiusInChunks = 16;

  private static ChunkRegion overworldRegion() {
    return ChunkRegion.of(-WorldRadiusInChunks, WorldRadiusInChunks);
  }

  private static ChunkRegion overworldBiomeRegion() {
    return ChunkRegion.of(-WorldRadiusInChunks - 1, WorldRadiusInChunks + 1);
  }

  private static ChunkRegion endRegion() {
    return ChunkRegion.of(128 - WorldRadiusInChunks, 128 + WorldRadiusInChunks);
  }

  private static ChunkRegion endBiomeRegion() {
    return ChunkRegion.of(128 - WorldRadiusInChunks - 1, 128 + WorldRadiusInChunks + 1);
  }

  public static BenchmarkSettings overworld() {
    return new BenchmarkSettings(
        overworldRegion(), overworldBiomeRegion(), defaultSeed, DimensionOptions.OVERWORLD);
  }

  public static BenchmarkSettings nether() {
    return new BenchmarkSettings(
        overworldRegion(), overworldBiomeRegion(), defaultSeed, DimensionOptions.NETHER);
  }

  public static BenchmarkSettings end() {
    return new BenchmarkSettings(endRegion(), endBiomeRegion(), defaultSeed, DimensionOptions.END);
  }

  public static record ChunkRegion(ChunkPos[] pos, ChunkPos min, ChunkPos max) {
    public static ChunkRegion of(int min, int max) {
      var pos = new ChunkPos[(max + 1 - min) * (max + 1 - min)];
      int idx = 0;
      for (int x = min; x <= max; x++) {
        for (int z = min; z <= max; z++) {
          pos[idx++] = new ChunkPos(x, z);
        }
      }
      return new ChunkRegion(pos, new ChunkPos(min, min), new ChunkPos(max, max));
    }
  }
}
