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

  private final FastBiomeProvider posToBiomeProvider;

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
    this.posToBiomeProvider = new FastBiomeProvider(chunk, posToBiome);
    this.biomeSupplier = this.posToBiomeProvider;
  }

  @Override
  public void initHorizontalContext(int blockX, int blockZ) {
    super.initHorizontalContext(blockX, blockZ);
    this.posToBiomeProvider.updateXZ(blockX, blockZ);
  }

  @Override
  public void initVerticalContext(
      int stoneDepthAbove,
      int stoneDepthBelow,
      int fluidHeight,
      int blockX,
      int blockY,
      int blockZ) {
    this.uniquePosValue++;
    this.blockY = blockY;
    this.fluidHeight = fluidHeight;
    this.stoneDepthBelow = stoneDepthBelow;
    this.stoneDepthAbove = stoneDepthAbove;
    this.posToBiomeProvider.updateY(blockY);
  }

  @Override
  public int estimateSurfaceHeight() {
    return super.estimateSurfaceHeight();
  }
}
