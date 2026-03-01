package org.codeberg.zenxarch.fastnoise.surface;

import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.BlockColumn;
import org.codeberg.zenxarch.fastnoise.heightmap.HeightmapUtil;
import org.codeberg.zenxarch.fastnoise.mixin.HeightmapAccessor;

public class FastBlockColumn implements BlockColumn {

  private final Chunk chunk;
  private final BlockPos.Mutable columnPos;
  private final int minY;
  private final int maxY;
  private final PaletteStorage[] heightmapData;
  private final ChunkSection[] sections;

  private final BlockState VOID_AIR = Blocks.VOID_AIR.getDefaultState();
  private final BlockState AIR = Blocks.AIR.getDefaultState();

  private static final Heightmap.Type[] heightmaps =
      HeightmapUtil.calculateHeightmaps(ChunkStatus.SURFACE);

  @SuppressWarnings("unchecked")
  private final Predicate<BlockState>[] predicates =
      Stream.of(heightmaps).map(type -> type.getBlockPredicate()).toArray(Predicate[]::new);

  public FastBlockColumn(final Chunk chunk, final BlockPos.Mutable columnPos) {
    this.chunk = chunk;
    this.columnPos = columnPos;
    this.minY = this.chunk.getBottomY();
    this.maxY = this.chunk.getTopYInclusive();
    this.heightmapData = new PaletteStorage[heightmaps.length];

    for (int i = 0; i < heightmapData.length; i++) {
      this.heightmapData[i] =
          ((HeightmapAccessor) chunk.getHeightmap(heightmaps[i])).zenxarch$getStorage();
    }

    this.sections = chunk.getSectionArray();
  }

  public int getSectionIndex(int y) {
    return (y - minY) >> 4;
  }

  public ChunkSection getSection(int y) {
    return this.sections[getSectionIndex(y)];
  }

  private ChunkSection zenxarch$getSection(final int y) {
    columnPos.setY(y);
    if (y < minY || y > maxY) return null;
    return getSection(y);
  }

  @Override
  public BlockState getState(int y) {
    var section = zenxarch$getSection(y);
    if (section == null) return VOID_AIR;
    if (section.isEmpty()) return AIR;
    return section.getBlockState(
        columnPos.getX() & 0xF, columnPos.getY() & 0xF, columnPos.getZ() & 0xF);
  }

  @Override
  public void setState(int y, BlockState state) {
    var section = zenxarch$getSection(y);

    int lx = columnPos.getX() & 0xF;
    int lz = columnPos.getZ() & 0xF;

    section.setBlockState(lx, y & 15, lz, state, false);
    this.fastUpdateHeightmap(lx, lz, y, state);

    if (!state.getFluidState().isEmpty()) {
      chunk.markBlockForPostProcessing(columnPos);
    }
  }

  public void fastSetState(ChunkSection section, int lx, int ly, int lz, BlockState state) {

    if (!state.getFluidState().isEmpty()) {
      chunk.markBlockForPostProcessing(columnPos);
    }
  }

  public void fastUpdateHeightmap(int lx, int lz, int iy, BlockState state) {
    final int y = iy - minY;
    for (int i = 0; i < heightmapData.length; i++) {
      HeightmapUtil.updateHeightmap(lx, lz, heightmapData[i], predicates[i], state, y, sections);
    }
  }
}
