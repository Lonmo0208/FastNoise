package org.codeberg.zenxarch.fastnoise.surface;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import org.codeberg.zenxarch.fastnoise.config.FastNoiseConfig;

public class FastSurfaceGen {
  public static boolean canSkipSurfaceBuilder(
      MaterialRules.MaterialRule rule, BlockState defaultState) {
    if (!FastNoiseConfig.SKIP_TRIVIAL_SURFACE_BUILDER) return false;
    if (rule instanceof MaterialRules.BlockMaterialRule block) {
      return block.resultState() == defaultState;
    }
    return false;
  }
}
