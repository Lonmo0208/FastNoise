package org.codeberg.zenxarch.fastnoise;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.AquiferSampler;
import org.codeberg.zenxarch.fastnoise.noise.FastChunkSection;
import org.codeberg.zenxarch.fastnoise.noise.FastWorldgen;

public interface Worldgen {

  public static void optimizedNoise(TestWorld world, ProtoChunk chunk) {

    var settings = world.settings;
    var shapeConfig = settings.generationShapeConfig().trimHeight(chunk.getHeightLimitView());

    int minY = shapeConfig.minimumY();
    int minimumCellY = MathHelper.floorDiv(minY, shapeConfig.verticalCellBlockCount());
    int cellHeight =
        MathHelper.floorDiv(shapeConfig.height(), shapeConfig.verticalCellBlockCount());

    var start = chunk.getSectionIndex(minY);
    var end = chunk.getSectionIndex(cellHeight * shapeConfig.verticalCellBlockCount() - 1 + minY);

    var chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(null);

    var fastSections = new FastChunkSection[end + 1];
    for (int i = start; i <= end; i++) fastSections[i] = new FastChunkSection(chunk.getSection(i));
    FastWorldgen.populateNoise(
        chunkNoiseSampler, settings.defaultBlock(), chunk, minimumCellY, cellHeight, fastSections);
  }

  public static final BlockState AIR = Blocks.AIR.getDefaultState();

  public static void vanillaNoise(TestWorld world, ProtoChunk chunk) {

    var settings = world.settings;
    var shapeConfig = settings.generationShapeConfig().trimHeight(chunk.getHeightLimitView());

    int minY = shapeConfig.minimumY();
    int minimumCellY = MathHelper.floorDiv(minY, shapeConfig.verticalCellBlockCount());
    int cellHeight =
        MathHelper.floorDiv(shapeConfig.height(), shapeConfig.verticalCellBlockCount());

    var chunkNoiseSampler = chunk.getOrCreateChunkNoiseSampler(null);

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

  public static void optimizedBiomes(TestWorld world, ProtoChunk chunk) {
    FastWorldgen.populateBiomes(
        chunk, world.getBiomeSupplier(chunk), world.createMultiNoiseSampler(chunk));
  }

  public static void vanillaBiomes(TestWorld world, ProtoChunk chunk) {
    var supplier = world.getBiomeSupplier(chunk);
    var sampler = world.createMultiNoiseSampler(chunk);
    var chunkPos = chunk.getPos();
    int x = BiomeCoords.fromBlock(chunkPos.getStartX());
    int z = BiomeCoords.fromBlock(chunkPos.getStartZ());
    var fakeWorld = chunk.getHeightLimitView();

    for (int cy = fakeWorld.getBottomSectionCoord(); cy <= fakeWorld.getTopSectionCoord(); cy++) {
      var section = chunk.getSection(chunk.sectionCoordToIndex(cy));
      int y = BiomeCoords.fromChunk(cy);
      populateBiomesVanilla(section, supplier, sampler, x, y, z);
    }
  }

  private static void populateBiomesVanilla(
      ChunkSection section,
      BiomeSupplier supplier,
      MultiNoiseUtil.MultiNoiseSampler sampler,
      int x,
      int y,
      int z) {
    PalettedContainer<RegistryEntry<Biome>> palettedContainer = section.biomeContainer.slice();

    for (int ix = 0; ix < 4; ix++) {
      for (int iy = 0; iy < 4; iy++) {
        for (int iz = 0; iz < 4; iz++) {
          palettedContainer.swapUnsafe(
              ix, iy, iz, supplier.getBiome(x + ix, y + iy, z + iz, sampler));
        }
      }
    }

    section.biomeContainer = palettedContainer;
  }
}
