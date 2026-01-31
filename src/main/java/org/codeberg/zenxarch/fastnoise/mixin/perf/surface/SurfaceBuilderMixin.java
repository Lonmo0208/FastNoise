package org.codeberg.zenxarch.fastnoise.mixin.perf.surface;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net/minecraft/world/gen/surfacebuilder/SurfaceBuilder$1"})
public abstract class SurfaceBuilderMixin {
  @Unique @Final Chunk chunk;
  @Unique @Final BlockPos.Mutable columnPos;
  @Unique @Final int minY;
  @Unique @Final int maxY;

  private final BlockState VOID_AIR = Blocks.VOID_AIR.getDefaultState();

  @Inject(method = "<init>", at = @At("TAIL"))
  private void zenxarch$init(
      final SurfaceBuilder this$0,
      final Chunk val$protoChunk,
      final BlockPos.Mutable val$columnPos,
      final ChunkPos val$chunkPos,
      CallbackInfo ci) {
    this.chunk = val$protoChunk;
    this.columnPos = val$columnPos;
    this.minY = this.chunk.getBottomY();
    this.maxY = this.chunk.getTopYInclusive();
  }

  @Unique
  private ChunkSection zenxarch$getSection(final int y) {
    columnPos.setY(y);
    if (y < minY || y > maxY) return null;
    var pos = (y - minY) >> 4;
    return chunk.getSectionArray()[pos];
  }

  @Overwrite
  public BlockState getState(final int y) {
    var section = zenxarch$getSection(y);
    if (section == null) return VOID_AIR;
    return section.getBlockState(
        columnPos.getX() & 0xF, columnPos.getY() & 0xF, columnPos.getZ() & 0xF);
  }

  @Overwrite
  public void setState(final int y, final BlockState state) {
    var section = zenxarch$getSection(y);
    if (section == null) return;

    int lx = columnPos.getX() & 0xF;
    int lz = columnPos.getZ() & 0xF;

    section.setBlockState(lx, y & 15, lz, state, false);

    for (var heightmap : chunk.getStatus().getHeightmapTypes()) {
      chunk.getHeightmap(heightmap).trackUpdate(lx, y, lz, state);
    }

    if (!state.getFluidState().isEmpty()) {
      chunk.markBlockForPostProcessing(columnPos);
    }
  }
}
