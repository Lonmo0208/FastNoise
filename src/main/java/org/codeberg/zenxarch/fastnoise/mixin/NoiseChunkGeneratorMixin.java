package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {

  @Shadow
  private Chunk populateNoise(
      Blender blender,
      StructureAccessor structureAccessor,
      NoiseConfig noiseConfig,
      Chunk chunk,
      int minimumCellY,
      int cellHeight) {
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
    var start = chunk.getSectionIndex(minimumY);
    var end =
        chunk.getSectionIndex(
            cellHeight * generationShapeConfig.verticalCellBlockCount() - 1 + minimumY);
    for (int i = start; i <= end; i++) chunk.getSection(i).lock();

    var result = chunk;
    try {
      result =
          this.populateNoise(
              blender, structureAccessor, noiseConfig, chunk, minimumCellY, cellHeight);
    } finally {
      for (int i = start; i <= end; i++) chunk.getSection(i).unlock();
    }
    return result;
  }
}
