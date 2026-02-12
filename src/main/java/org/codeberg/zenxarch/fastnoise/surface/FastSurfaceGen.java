package org.codeberg.zenxarch.fastnoise.surface;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.codeberg.zenxarch.fastnoise.mixin.SurfaceBuilderAccessor;

public class FastSurfaceGen {
  public static void buildSurface(
      SurfaceBuilderAccessor builder,
      final NoiseConfig noiseConfig,
      final BiomeAccess biomeAccess,
      final Registry<Biome> biomeRegistry,
      final boolean useLegacyRandom,
      final HeightContext heightContext,
      final Chunk chunk,
      final ChunkNoiseSampler chunkNoiseSampler,
      final MaterialRules.MaterialRule materialRule) {

    final BlockPos.Mutable columnPos = new BlockPos.Mutable();
    final ChunkPos chunkPos = chunk.getPos();
    int minBlockX = chunkPos.getStartX();
    int minBlockZ = chunkPos.getStartZ();
    BlockColumn column = new FastBlockColumn(chunk, columnPos);
    var context =
        new MaterialRuleContext(
            (SurfaceBuilder) (Object) builder,
            noiseConfig,
            chunk,
            chunkNoiseSampler,
            biomeAccess::getBiome,
            biomeRegistry,
            heightContext);
    var rule = materialRule.apply(context);
    BlockPos.Mutable blockPos = new BlockPos.Mutable();

    final var defaultState = builder.zenxarch$getDefaultState();

    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        int blockX = minBlockX + x;
        int blockZ = minBlockZ + z;
        int startingHeight = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, x, z) + 1;
        columnPos.setX(blockX).setZ(blockZ);
        RegistryEntry<Biome> surfaceBiome =
            biomeAccess.getBiome(
                blockPos.set(blockX, useLegacyRandom ? 0 : startingHeight, blockZ));
        if (surfaceBiome.matchesKey(BiomeKeys.ERODED_BADLANDS)) {
          builder.zenxarch$placeBadlandsPillar(column, blockX, blockZ, startingHeight, chunk);
        }

        int height = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, x, z) + 1;
        context.initHorizontalContext(blockX, blockZ);
        int stoneAboveDepth = 0;
        int waterHeight = Integer.MIN_VALUE;
        int nextCeilingStoneY = Integer.MAX_VALUE;
        int endY = chunk.getBottomY();

        for (int y = height; y >= endY; y--) {
          BlockState old = column.getState(y);
          if (old.isAir()) {
            stoneAboveDepth = 0;
            waterHeight = Integer.MIN_VALUE;
          } else if (!old.getFluidState().isEmpty()) {
            if (waterHeight == Integer.MIN_VALUE) {
              waterHeight = y + 1;
            }
          } else {
            if (nextCeilingStoneY >= y) {
              nextCeilingStoneY = DimensionType.field_35479;

              for (int lookaheadY = y - 1; lookaheadY >= endY - 1; lookaheadY--) {
                BlockState nextState = column.getState(lookaheadY);
                if (!builder.zenxarch$isDefaultBlock(nextState)) {
                  nextCeilingStoneY = lookaheadY + 1;
                  break;
                }
              }
            }

            stoneAboveDepth++;
            int stoneBelowDepth = y - nextCeilingStoneY + 1;
            context.initVerticalContext(
                stoneAboveDepth, stoneBelowDepth, waterHeight, blockX, y, blockZ);
            if (old == defaultState) {
              BlockState state = rule.tryApply(blockX, y, blockZ);
              if (state != null) {
                column.setState(y, state);
              }
            }
          }
        }

        if (surfaceBiome.matchesKey(BiomeKeys.FROZEN_OCEAN)
            || surfaceBiome.matchesKey(BiomeKeys.DEEP_FROZEN_OCEAN)) {
          builder.zenxarch$placeIceberg(
              context.estimateSurfaceHeight(),
              surfaceBiome.value(),
              column,
              blockPos,
              blockX,
              blockZ,
              startingHeight);
        }
      }
    }
  }
}
