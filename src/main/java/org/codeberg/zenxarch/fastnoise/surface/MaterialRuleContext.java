package org.codeberg.zenxarch.fastnoise.surface;

import java.util.function.Function;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;

public class MaterialRuleContext extends MaterialRules.MaterialRuleContext {

  private final RegistryEntry<Biome>[] singleBiomes;
  private final int minY;

  public MaterialRuleContext(
      SurfaceBuilder surfaceBuilder,
      NoiseConfig noiseConfig,
      Chunk chunk,
      ChunkNoiseSampler chunkNoiseSampler,
      Function<BlockPos, RegistryEntry<Biome>> posToBiome,
      Registry<Biome> biomeRegistry,
      HeightContext heightContext,
      RegistryEntry<Biome>[] singleBiomes) {
    super(
        surfaceBuilder,
        noiseConfig,
        chunk,
        chunkNoiseSampler,
        posToBiome,
        biomeRegistry,
        heightContext);
    this.singleBiomes = singleBiomes;
    this.minY = chunk.getBottomY();
  }

  @Override
  public void initHorizontalContext(int blockX, int blockZ) {
    super.initHorizontalContext(blockX, blockZ);
  }

  @Override
  public void initVerticalContext(
      int stoneDepthAbove,
      int stoneDepthBelow,
      int fluidHeight,
      int blockX,
      int blockY,
      int blockZ) {
    super.initVerticalContext(
        stoneDepthAbove, stoneDepthBelow, fluidHeight, blockX, blockY, blockZ);

    var x = blockX & 15;
    if (x < 2 || x > 14) return;
    var z = blockZ & 15;
    if (z < 2 || z > 14) return;
    var y = blockY - minY;
    var ly = y & 0x15;
    if (ly < 2 || ly > 14) return;
    var single = singleBiomes[y >> 4];
    if (single == null) return;
    this.biomeSupplier = () -> single;
  }

  @Override
  public int estimateSurfaceHeight() {
    return super.estimateSurfaceHeight();
  }
}
