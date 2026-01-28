package org.codeberg.zenxarch.fastnoise.mixin.perf.noise;

import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.codeberg.zenxarch.fastnoise.noise.FastChunkSection;
import org.codeberg.zenxarch.fastnoise.noise.FastWorldgen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {

  @Shadow @Final private RegistryEntry<ChunkGeneratorSettings> settings;

  @Shadow
  protected abstract ChunkNoiseSampler createChunkNoiseSampler(
      Chunk chunk, StructureAccessor world, Blender blender, NoiseConfig noiseConfig);

  @Shadow
  protected abstract Chunk populateNoise(
      Blender blender,
      StructureAccessor structureAccessor,
      NoiseConfig noiseConfig,
      Chunk chunk,
      int minimumCellY,
      int cellHeight);

  private Chunk zenxarch$populateNoise(
      Blender blender,
      StructureAccessor structureAccessor,
      NoiseConfig noiseConfig,
      Chunk chunk,
      int minimumCellY,
      int cellHeight,
      FastChunkSection[] fastSections) {
    var chunkNoiseSampler =
        chunk.getOrCreateChunkNoiseSampler(
            chunkx ->
                this.createChunkNoiseSampler(chunkx, structureAccessor, blender, noiseConfig));
    BlockState defaultBlockState = settings.value().defaultBlock();

    FastWorldgen.populateNoise(
        chunkNoiseSampler, defaultBlockState, chunk, minimumCellY, cellHeight, fastSections);

    return chunk;
  }

  @Overwrite
  private Chunk method_38332(
      Chunk chunk,
      int cellHeight,
      GenerationShapeConfig generationShapeConfig,
      int minimumY,
      Blender blender,
      StructureAccessor structureAccessor,
      NoiseConfig noiseConfig,
      int minimumCellY) {
    if (SharedConstants.isOutsideGenerationArea(chunk.getPos())) return chunk;

    var start = chunk.getSectionIndex(minimumY);
    var end =
        chunk.getSectionIndex(
            cellHeight * generationShapeConfig.verticalCellBlockCount() - 1 + minimumY);

    var fastSections = new FastChunkSection[end + 1];
    for (int i = start; i <= end; i++) fastSections[i] = new FastChunkSection(chunk.getSection(i));

    var result = chunk;
    try {
      result =
          SharedConstants.AQUIFERS
              ? this.populateNoise(
                  blender, structureAccessor, noiseConfig, chunk, minimumCellY, cellHeight)
              : this.zenxarch$populateNoise(
                  blender,
                  structureAccessor,
                  noiseConfig,
                  chunk,
                  minimumCellY,
                  cellHeight,
                  fastSections);
    } finally {}
    return result;
  }
}
