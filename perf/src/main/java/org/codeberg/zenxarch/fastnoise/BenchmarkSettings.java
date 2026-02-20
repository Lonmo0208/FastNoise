package org.codeberg.zenxarch.fastnoise;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionOptions;

public record BenchmarkSettings(
    ChunkRegion region,
    ChunkRegion biomeRegion,
    long seed,
    RegistryKey<DimensionOptions> dimensionOptions) {

  private static int getIntProperty(String name, int def, int min) {
    var value = System.getProperty(name);
    if (value == null) return def;
    try {
      return Math.max(Integer.parseInt(value), min);
    } catch (Exception e) {
      return def;
    }
  }

  private static final long defaultSeed = getIntProperty("zseed", 100, 0);
  private static final int WorldRadiusInChunks = getIntProperty("zworldradius", 16, 0);
  private static final int endCenter = getIntProperty("zendcenter", 128, Integer.MIN_VALUE);

  private static ChunkRegion overworldRegion() {
    return ChunkRegion.of(-WorldRadiusInChunks, WorldRadiusInChunks);
  }

  private static ChunkRegion overworldBiomeRegion() {
    return ChunkRegion.of(-WorldRadiusInChunks - 1, WorldRadiusInChunks + 1);
  }

  private static ChunkRegion endRegion() {
    return ChunkRegion.of(endCenter - WorldRadiusInChunks, endCenter + WorldRadiusInChunks);
  }

  private static ChunkRegion endBiomeRegion() {
    return ChunkRegion.of(endCenter - WorldRadiusInChunks - 1, endCenter + WorldRadiusInChunks + 1);
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
