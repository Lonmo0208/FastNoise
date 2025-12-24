package org.codeberg.zenxarch.fastnoise;

import com.mojang.logging.LogUtils;
import java.util.Arrays;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettesFactory;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.AquiferSampler.FluidLevelSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes.Beardifying;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.codeberg.zenxarch.fastnoise.noise.FastChunkSection;
import org.codeberg.zenxarch.fastnoise.noise.FastWorldgen;

public class PerfTest {
  public static void runTest(DynamicRegistryManager manager) {
    var pos = new ChunkPos(0, 0);
    var world =
        new HeightLimitView() {

          @Override
          public int getHeight() {
            return 384;
          }

          @Override
          public int getBottomY() {
            return -64;
          }
        };
    var factory = PalettesFactory.fromRegistryManager(manager);
    ProtoChunk chunk = new ProtoChunk(pos, UpgradeData.NO_UPGRADE_DATA, world, factory, null);

    var settings = manager.getEntryOrThrow(ChunkGeneratorSettings.OVERWORLD);
    var noiseConfig =
        NoiseConfig.create(settings.value(), manager.getOrThrow(RegistryKeys.NOISE_PARAMETERS), 0);

    var lava = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
    var water = new AquiferSampler.FluidLevel(-54, settings.value().defaultFluid());
    FluidLevelSampler fluidLevelSampler =
        (x, y, z) -> {
          return y < -54 ? lava : water;
        };

    var chunkNoiseSampler =
        ChunkNoiseSampler.create(
            chunk,
            noiseConfig,
            new Impl(),
            settings.value(),
            fluidLevelSampler,
            Blender.getNoBlending());

    var shapeConfig =
        settings.value().generationShapeConfig().trimHeight(chunk.getHeightLimitView());

    int minY = shapeConfig.minimumY();
    int minimumCellY = MathHelper.floorDiv(minY, shapeConfig.verticalCellBlockCount());
    int cellHeight =
        MathHelper.floorDiv(shapeConfig.height(), shapeConfig.verticalCellBlockCount());

    var start = chunk.getSectionIndex(minY);
    var end = chunk.getSectionIndex(cellHeight * shapeConfig.verticalCellBlockCount() - 1 + minY);

    int warmups = 5000;
    int benchmarks = 25000;
    doBenchmark(
        () -> {
          clearSections(chunk, factory);
        },
        () -> {
          vanilla(chunkNoiseSampler, settings.value(), chunk, minimumCellY, cellHeight, start, end);
        },
        warmups,
        benchmarks,
        "Vanilla");

    doBenchmark(
        () -> {
          clearSections(chunk, factory);
        },
        () -> {
          optimized(
              chunkNoiseSampler, settings.value(), chunk, minimumCellY, cellHeight, start, end);
        },
        warmups,
        benchmarks,
        "Optimized");
  }

  private static void doBenchmark(
      Runnable setup, Runnable benchmark, int warmups, int benchmarks, String name) {
    doBenchmark(setup, benchmark, warmups, "warmups", name);
    doBenchmark(setup, benchmark, benchmarks, "benchmarks", name);
  }

  private static void doBenchmark(
      Runnable setup, Runnable benchmark, int count, String type, String name) {
    var logger = LogUtils.getLogger();
    var time = 0l;
    var max = Long.MIN_VALUE;
    var min = Long.MAX_VALUE;

    for (int i = 0; i < count; i++) {
      setup.run();
      var startTime = Util.getMeasuringTimeNano() / 1000;
      benchmark.run();
      var endTime = Util.getMeasuringTimeNano() / 1000;
      var thisTime = (endTime - startTime);
      max = Long.max(max, thisTime);
      min = Long.min(min, thisTime);
      time += thisTime;
    }
    logger.info("{} {} {} took {} us", count, name, type, time);
    logger.info("(Min / Average / Max) time: ({} / {} / {}) us", min, (float) time / count, max);
  }

  private static void clearSections(ProtoChunk chunk, PalettesFactory factory) {
    var data = chunk.getSectionArray();
    for (int i = 0; i < data.length; i++) data[i] = new ChunkSection(factory);
    Arrays.setAll(chunk.getHeightmap(Type.OCEAN_FLOOR_WG).asLongArray(), t -> 0);
    Arrays.setAll(chunk.getHeightmap(Type.WORLD_SURFACE_WG).asLongArray(), t -> 0);
  }

  private static void optimized(
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

  private static final BlockState AIR = Blocks.AIR.getDefaultState();

  private static void vanilla(
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

  private static class Impl implements Beardifying {

    @Override
    public double maxValue() {
      return 1.0;
    }

    @Override
    public double minValue() {
      return 0.0;
    }

    @Override
    public double sample(NoisePos pos) {
      return 0.5;
    }
  }
}
