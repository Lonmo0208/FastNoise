package org.codeberg.zenxarch.fastnoise.surface;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.HeightContext;
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

    final var defaultState = builder.zenxarch$getDefaultState();

    if (canSkipSurfaceBuilder(materialRule, defaultState)) {
      return;
    }

    final BlockPos.Mutable columnPos = new BlockPos.Mutable();
    final ChunkPos chunkPos = chunk.getPos();
    int minBlockX = chunkPos.getStartX();
    int minBlockZ = chunkPos.getStartZ();
    var column = new FastBlockColumn(chunk, columnPos);
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

    final int endY = chunk.getBottomY();
    final int topY = chunk.getTopYInclusive();

    for (int x = 0; x < 16; x++) {
      for (int z = 0; z < 16; z++) {
        int blockX = minBlockX + x;
        int blockZ = minBlockZ + z;
        int startingHeight = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, x, z) + 1;
        RegistryEntry<Biome> surfaceBiome =
            biomeAccess.getBiome(
                blockPos.set(blockX, useLegacyRandom ? 0 : startingHeight, blockZ));
        if (surfaceBiome.matchesKey(BiomeKeys.ERODED_BADLANDS)) {
          columnPos.setX(blockX).setZ(blockZ);
          builder.zenxarch$placeBadlandsPillar(column, blockX, blockZ, startingHeight, chunk);
        }

        int height = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, x, z) + 1;
        context.initHorizontalContext(blockX, blockZ);
        int stoneAboveDepth = 0;
        int waterHeight = Integer.MIN_VALUE;
        int nextCeilingStoneY = Integer.MAX_VALUE;
        var sections = chunk.getSectionArray();

        int y = height;
        if (y >= topY) { // assuming void air is air
          stoneAboveDepth = 0;
          waterHeight = Integer.MIN_VALUE;
          y = topY;
        }

        for (; y >= endY; y--) {
          var section = column.getSection(y);
          if (section.isEmpty()) { // skip whole section
            y = y - (y & 0xF); // lowest y in current section;
            stoneAboveDepth = 0;
            waterHeight = Integer.MIN_VALUE;
            continue;
          }
          BlockState old = section.getBlockState(x, y & 0xF, z);
          if (old.isAir()) {
            stoneAboveDepth = 0;
            waterHeight = Integer.MIN_VALUE;
          } else if (!old.getFluidState().isEmpty()) {
            if (waterHeight == Integer.MIN_VALUE) {
              waterHeight = y + 1;
            }
          } else {
            if (nextCeilingStoneY >= y)
              nextCeilingStoneY = nextNonDefaultBlock(builder, sections, y, endY, x, z);

            stoneAboveDepth++;
            int stoneBelowDepth = y - nextCeilingStoneY + 1;
            context.initVerticalContext(
                stoneAboveDepth, stoneBelowDepth, waterHeight, blockX, y, blockZ);
            if (old == defaultState) {
              BlockState state = rule.tryApply(blockX, y, blockZ);
              if (state != null) {
                if (y < endY) continue;
                column.getSection(y).setBlockState(x, y & 0xF, z, state, false);
                column.fastUpdateHeightmap(x, z, y & 0xF, state);
                if (state.getFluidState().isEmpty()) {
                  columnPos.setX(blockX).setZ(blockZ).setY(y);
                  chunk.markBlockForPostProcessing(columnPos);
                }
              }
            }
          }
        }

        if (surfaceBiome.matchesKey(BiomeKeys.FROZEN_OCEAN)
            || surfaceBiome.matchesKey(BiomeKeys.DEEP_FROZEN_OCEAN)) {
          columnPos.setX(blockX).setZ(blockZ);
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

  private static final BlockState VOID_AIR = Blocks.VOID_AIR.getDefaultState();

  private static int nextNonDefaultBlock(
      SurfaceBuilderAccessor builder,
      ChunkSection[] sections,
      int startY,
      int minY,
      int lx,
      int lz) {
    final int wayBelowMinY = DimensionType.field_35479;
    if (startY <= minY) {
      if (builder.zenxarch$isDefaultBlock(VOID_AIR)) return minY - 1;
      return wayBelowMinY;
    }
    var y = startY - 1;
    var cy = (y - minY) >> 4;
    var section = sections[cy];

    {
      var next = getNextNonDefaultBlock(section, lx, y & 0xF, lz, builder);
      if (next != -1) return (cy << 4) + next + minY;
    }

    cy--;

    while (cy >= 0) {
      section = sections[cy];
      var next = getNextNonDefaultBlock(section, lx, 0xF, lz, builder);
      if (next != -1) return (cy << 4) + next + minY;
      cy--;
    }

    if (builder.zenxarch$isDefaultBlock(VOID_AIR)) return minY - 1;
    return wayBelowMinY;
  }

  private static int getNextNonDefaultBlock(
      ChunkSection section, int lx, int ly, int lz, SurfaceBuilderAccessor builder) {
    var index = lx + (lz << 4) + (ly << 8);
    var palette = section.blockStateContainer.data.palette();
    var storage = section.blockStateContainer.data.storage();
    while (index >= 0) {
      if (builder.zenxarch$isDefaultBlock(palette.get(storage.get(index)))) return index >> 8;
      index -= 256;
    }
    return -1;
  }

  private static boolean canSkipSurfaceBuilder(
      MaterialRules.MaterialRule rule, BlockState defaultState) {
    if (rule instanceof MaterialRules.BlockMaterialRule block) {
      return block.resultState() == defaultState;
    }
    return false;
  }
}
