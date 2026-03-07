package org.codeberg.zenxarch.fastnoise.mixin.perf.surface;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.function.Function;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.MaterialRules.MaterialRuleContext;
import org.codeberg.zenxarch.fastnoise.surface.MaterialRuleContextHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MaterialRuleContext.class)
public abstract class MaterialRuleContextMixin {
  @Unique @Final private RegistryEntry<Biome>[] zenxarch$singleBiomes;

  @Unique @Final private int zenxarch$minY;
  @Unique @Final private Function<BlockPos, RegistryEntry<Biome>> ogPosToBiome;

  @Definition(
      id = "posToBiome",
      field =
          "Lnet/minecraft/world/gen/surfacebuilder/MaterialRules$MaterialRuleContext;posToBiome:Ljava/util/function/Function;")
  @Expression("this.posToBiome = @(?)")
  @ModifyExpressionValue(method = "<init>", at = @At("MIXINEXTRAS:EXPRESSION"))
  private Function<BlockPos, RegistryEntry<Biome>> zenxarch$init(
      final Function<BlockPos, RegistryEntry<Biome>> posToBiome,
      @Local(argsOnly = true) Chunk chunk) {
    this.zenxarch$minY = chunk.getBottomY();
    this.zenxarch$singleBiomes = MaterialRuleContextHelper.calculateSingleBiomes(chunk);
    if (zenxarch$singleBiomes == null) return posToBiome;
    this.ogPosToBiome = posToBiome;
    return this::zenxarch$getBiome;
  }

  @Unique
  private RegistryEntry<Biome> zenxarch$getBiome(BlockPos pos) {
    var single =
        MaterialRuleContextHelper.getSingleBiome(
            pos.getX(), pos.getY(), pos.getZ(), zenxarch$minY, zenxarch$singleBiomes);
    if (single != null) return single;
    return this.ogPosToBiome.apply(pos);
  }
}
