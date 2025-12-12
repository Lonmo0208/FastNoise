package org.codeberg.zenxarch.fastnoise.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.codeberg.zenxarch.fastnoise.noise.FastChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {

  @Shadow @Final private RegistryEntry<ChunkGeneratorSettings> settings;
  @Shadow @Final private static BlockState AIR;

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
    ChunkPos chunkPos = chunk.getPos();
    int chunkStartX = chunkPos.getStartX();
    int chunkStartZ = chunkPos.getStartZ();
    AquiferSampler aquiferSampler = chunkNoiseSampler.getAquiferSampler();
    chunkNoiseSampler.sampleStartDensity();
    BlockPos.Mutable mutable = new BlockPos.Mutable();
    int horizontalCellBlockCount = chunkNoiseSampler.getHorizontalCellBlockCount();
    int verticalCellBlockCount = chunkNoiseSampler.getVerticalCellBlockCount();
    int cellWidth = 16 / horizontalCellBlockCount;

    FastChunkSection fastSection = fastSections[fastSections.length - 1];
    BlockState defaultBlockState = settings.value().defaultBlock();

    for (int cellX = 0; cellX < cellWidth; cellX++) {
      chunkNoiseSampler.sampleEndDensity(cellX);

      for (int cellZ = 0; cellZ < cellWidth; cellZ++) {
        for (int cellY = cellHeight - 1; cellY >= 0; cellY--) {
          chunkNoiseSampler.onSampledCellCorners(cellY, cellZ);

          for (int verticalCellBlock = verticalCellBlockCount - 1;
              verticalCellBlock >= 0;
              verticalCellBlock--) {
            int blockY = (minimumCellY + cellY) * verticalCellBlockCount + verticalCellBlock;
            int blockYInSection = blockY & 15;

            double verticalCellProgress =
                (double) verticalCellBlock / (double) verticalCellBlockCount;
            chunkNoiseSampler.interpolateY(blockY, verticalCellProgress);

            fastSection = fastSections[chunk.getSectionIndex(blockY)];

            for (int cellBlockX = 0; cellBlockX < horizontalCellBlockCount; cellBlockX++) {
              int blockX = chunkStartX + cellX * horizontalCellBlockCount + cellBlockX;
              int blockXInSection = blockX & 15;
              double cellXProgress = (double) cellBlockX / (double) horizontalCellBlockCount;
              chunkNoiseSampler.interpolateX(blockX, cellXProgress);

              for (int cellBlockZ = 0; cellBlockZ < horizontalCellBlockCount; cellBlockZ++) {
                int blockZ = chunkStartZ + cellZ * horizontalCellBlockCount + cellBlockZ;
                int blockZInSection = blockZ & 15;
                double cellZProgress = (double) cellBlockZ / (double) horizontalCellBlockCount;

                chunkNoiseSampler.interpolateZ(blockZ, cellZProgress);

                var state = chunkNoiseSampler.sampleBlockState();
                if (state == null) state = defaultBlockState;
                if (state == AIR) continue;
                fastSection.setBlockState(blockXInSection, blockYInSection, blockZInSection, state);

                if (aquiferSampler.needsFluidTick() && !state.getFluidState().isEmpty()) {
                  mutable.set(blockX, blockY, blockZ);
                  chunk.markBlockForPostProcessing(mutable);
                }
              }
            }
          }
        }
      }

      chunkNoiseSampler.swapBuffers();
    }

    chunkNoiseSampler.stopInterpolation();

    for (int i = 0; i < fastSections.length; i++)
      if (fastSections[i] != null) fastSections[i].recalculateCounts();

    Heightmap.populateHeightmaps(
        chunk, ObjectArraySet.of(Heightmap.Type.OCEAN_FLOOR_WG, Heightmap.Type.WORLD_SURFACE_WG));
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
}
