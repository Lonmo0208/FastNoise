package org.codeberg.zenxarch.fastnoise.mixin.perf.surface;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.BlockColumn;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.codeberg.zenxarch.fastnoise.FastNoiseConfig;
import org.codeberg.zenxarch.fastnoise.surface.FastBlockColumn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SurfaceBuilder.class)
public abstract class SurfaceBuilderMixin {
  @Definition(id = "blockColumn", local = @Local(type = BlockColumn.class))
  @Expression("blockColumn = new ?(?, ?, ?, ?)")
  @ModifyVariable(
      method = "buildSurface",
      at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
  public BlockColumn zenxarch$modifyColumn(
      BlockColumn og, @Local Chunk chunk, @Local(ordinal = 0) BlockPos.Mutable columnPos) {
    if (!FastNoiseConfig.ENABLED) {
      return og;
    }
    return new FastBlockColumn(chunk, columnPos);
  }
}
