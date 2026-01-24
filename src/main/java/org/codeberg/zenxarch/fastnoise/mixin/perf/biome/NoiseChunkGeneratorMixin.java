package org.codeberg.zenxarch.fastnoise.mixin.perf.biome;

import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.codeberg.zenxarch.fastnoise.noise.FastWorldgen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(NoiseChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin {

  @Redirect(
      method =
          "populateBiomes(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/chunk/Chunk;)V",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/chunk/Chunk;populateBiomes(Lnet/minecraft/world/biome/source/BiomeSupplier;Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;)V"))
  private void zenxarch$populateBiomes(
      Chunk chunk, BiomeSupplier supplier, MultiNoiseUtil.MultiNoiseSampler sampler) {
    FastWorldgen.populateBiomes(chunk, supplier, sampler);
  }
}
