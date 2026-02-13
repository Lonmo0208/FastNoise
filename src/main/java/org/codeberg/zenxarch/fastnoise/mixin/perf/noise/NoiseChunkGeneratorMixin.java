package org.codeberg.zenxarch.fastnoise.mixin.perf.noise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.codeberg.zenxarch.fastnoise.noise.FastChunkSection;
import org.codeberg.zenxarch.fastnoise.noise.FastWorldgen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {

  @Shadow @Final private RegistryEntry<ChunkGeneratorSettings> settings;

  @Shadow
  protected abstract ChunkNoiseSampler createChunkNoiseSampler(
      Chunk chunk, StructureAccessor world, Blender blender, NoiseConfig noiseConfig);

  @Unique
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
  public CompletableFuture<Chunk> populateNoise(
      Executor executor,
      Blender blender,
      NoiseConfig noiseConfig,
      StructureAccessor structureAccessor,
      Chunk chunk) {
    if (SharedConstants.method_37896(chunk.getPos()))
      return CompletableFuture.completedFuture(chunk);

    var generationShapeConfig =
        this.settings.value().generationShapeConfig().method_42368(chunk.getHeightLimitView());
    var minimumY = generationShapeConfig.minimumY();
    var minimumCellY = MathHelper.floorDiv(minimumY, generationShapeConfig.verticalBlockSize());
    var cellHeight =
        MathHelper.floorDiv(
            generationShapeConfig.height(), generationShapeConfig.verticalBlockSize());

    if (cellHeight <= 0) {
      return CompletableFuture.completedFuture(chunk);
    }

    return CompletableFuture.supplyAsync(
        Util.debugSupplier(
            "wgen_fill_noise",
            () -> {
              var start = chunk.getSectionIndex(minimumY);
              var end =
                  chunk.getSectionIndex(
                      cellHeight * generationShapeConfig.verticalBlockSize() - 1 + minimumY);

              var fastSections = new FastChunkSection[end + 1];
              for (int i = start; i <= end; i++)
                fastSections[i] = new FastChunkSection(chunk.getSection(i));

              var result = chunk;
              try {
                result =
                    this.zenxarch$populateNoise(
                        blender,
                        structureAccessor,
                        noiseConfig,
                        chunk,
                        minimumCellY,
                        cellHeight,
                        fastSections);
              } finally {
              }
              return result;
            }),
        Util.getMainWorkerExecutor());
  }
}
