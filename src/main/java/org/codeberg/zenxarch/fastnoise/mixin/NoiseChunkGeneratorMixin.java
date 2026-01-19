package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
    for (int i = start; i <= end; i++) chunk.getSection(i).lock();

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
    } finally {
      for (int i = start; i <= end; i++) chunk.getSection(i).unlock();
    }
    return result;
  }

  @Redirect(
      method =
          "populateBiomes(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/chunk/Chunk;populateBiomes(Lnet/minecraft/world/biome/source/BiomeSupplier;Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;)V"))
  private void zenxarch$populateBiomes(
      Chunk chunk, BiomeSupplier supplier, MultiNoiseUtil.MultiNoiseSampler sampler) {
    FastWorldgen.populateBiomes(chunk, supplier, sampler);
  }
}
