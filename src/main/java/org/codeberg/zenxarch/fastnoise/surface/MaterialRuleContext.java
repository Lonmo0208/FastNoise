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

  public MaterialRuleContext(
      SurfaceBuilder surfaceBuilder,
      NoiseConfig noiseConfig,
      Chunk chunk,
      ChunkNoiseSampler chunkNoiseSampler,
      Function<BlockPos, RegistryEntry<Biome>> posToBiome,
      Registry<Biome> biomeRegistry,
      HeightContext heightContext) {
    super(
        surfaceBuilder,
        noiseConfig,
        chunk,
        chunkNoiseSampler,
        posToBiome,
        biomeRegistry,
        heightContext);
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
  }

  @Override
  public int estimateSurfaceHeight() {
    return super.estimateSurfaceHeight();
  }
}
