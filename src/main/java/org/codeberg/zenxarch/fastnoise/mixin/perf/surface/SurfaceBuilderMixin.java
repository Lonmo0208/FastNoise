package org.codeberg.zenxarch.fastnoise.mixin.perf.surface;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.noise.NoiseConfig;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.SurfaceBuilder;
import org.codeberg.zenxarch.fastnoise.config.FastNoiseConfig;
import org.codeberg.zenxarch.fastnoise.surface.FastSurfaceGen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SurfaceBuilder.class)
public abstract class SurfaceBuilderMixin {

  @Shadow @Final private BlockState defaultState;

  @Inject(method = "buildSurface", at = @At("HEAD"), cancellable = true)
  public void zenxarch$buildSurface(
      final NoiseConfig noiseConfig,
      final BiomeAccess biomeAccess,
      final Registry<Biome> biomeRegistry,
      final boolean useLegacyRandom,
      final HeightContext heightContext,
      final Chunk chunk,
      final ChunkNoiseSampler chunkNoiseSampler,
      final MaterialRules.MaterialRule materialRule,
      CallbackInfo ci) {
    if (!FastNoiseConfig.ENABLED) return;
    if (FastSurfaceGen.canSkipSurfaceBuilder(materialRule, defaultState)) ci.cancel();
  }
}
