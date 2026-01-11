package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.world.biome.source.util.MultiNoiseUtil$SearchTree$TreeNode")
public abstract class TreeNodeMixin {
  @Shadow @Final protected MultiNoiseUtil.ParameterRange[] parameters;

  @Overwrite
  public long getSquaredDistance(long[] otherParameters) {
    long result = 0L;

    for (int i = 0; i < 7; i++) {
      long noise = otherParameters[i];
      long min = this.parameters[i].min();
      long max = this.parameters[i].max();

      if (noise > max) {
        long distance = noise - max;
        result += distance * distance;
      }
      if (min > noise) {
        long distance = min - noise;
        result += distance * distance;
      }
    }

    return result;
  }
}
