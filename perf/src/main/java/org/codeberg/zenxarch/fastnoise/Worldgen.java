package org.codeberg.zenxarch.fastnoise;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.codeberg.zenxarch.fastnoise.noise.FastChunkSection;
import org.codeberg.zenxarch.fastnoise.noise.FastWorldgen;

public interface Worldgen {

  public static void optimized(
      ChunkNoiseSampler chunkNoiseSampler,
      ChunkGeneratorSettings settings,
      ProtoChunk chunk,
      int minimumCellY,
      int cellHeight,
      int start,
      int end) {
    var fastSections = new FastChunkSection[end + 1];
    for (int i = start; i <= end; i++) fastSections[i] = new FastChunkSection(chunk.getSection(i));
    FastWorldgen.populateNoise(
        chunkNoiseSampler, settings.defaultBlock(), chunk, minimumCellY, cellHeight, fastSections);
  }

  public static final BlockState AIR = Blocks.AIR.getDefaultState();

  public static void vanilla(
      ChunkNoiseSampler chunkNoiseSampler,
      ChunkGeneratorSettings settings,
      ProtoChunk chunk,
      int minimumCellY,
      int cellHeight,
      int start,
      int end) {

    ChunkPos chunkPos = chunk.getPos();
    int chunkStartX = chunkPos.getStartX();
    int chunkStartZ = chunkPos.getStartZ();
    AquiferSampler aquiferSampler = chunkNoiseSampler.getAquiferSampler();
    chunkNoiseSampler.sampleStartDensity();
    BlockPos.Mutable mutable = new BlockPos.Mutable();
    int horizontalCellBlockCount = chunkNoiseSampler.getHorizontalCellBlockCount();
    int verticalCellBlockCount = chunkNoiseSampler.getVerticalCellBlockCount();
    int cellWidth = 16 / horizontalCellBlockCount;

    var defaultBlockState = settings.defaultBlock();

    var oceanWg = chunk.getHeightmap(Type.OCEAN_FLOOR_WG);
    var surfaceWg = chunk.getHeightmap(Type.WORLD_SURFACE_WG);

    ChunkSection section = null;

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

            section = chunk.getSectionArray()[chunk.getSectionIndex(blockY)];

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
                section.setBlockState(
                    blockXInSection, blockYInSection, blockZInSection, state, false);

                oceanWg.trackUpdate(blockXInSection, blockY, blockZInSection, state);
                surfaceWg.trackUpdate(blockXInSection, blockY, blockZInSection, state);

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
  }

  @FunctionalInterface
  public static interface PopulateNoiseFunction {
    public void populateNoise(
        ChunkNoiseSampler chunkNoiseSampler,
        ChunkGeneratorSettings settings,
        ProtoChunk chunk,
        int minimumCellY,
        int cellHeight,
        int start,
        int end);
  }
}
