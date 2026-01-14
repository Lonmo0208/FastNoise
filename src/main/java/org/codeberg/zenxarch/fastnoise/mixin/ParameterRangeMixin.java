package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.world.biome.source.util.MultiNoiseUtil.ParameterRange;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ParameterRange.class)
public abstract class ParameterRangeMixin {
  @Shadow @Final private long min;
  @Shadow @Final private long max;

  @Overwrite
  public long getDistance(long noise) {
    return Math.max(Math.max(noise - max, min - noise), 0);
  }

  @Overwrite
  public long getDistance(ParameterRange range) {
    return Math.max(Math.max(range.min - max, min - range.max), 0);
  }
}
