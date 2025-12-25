package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;
import org.codeberg.zenxarch.fastnoise.noise.FastPackedIntegerArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin {

  @Shadow private ReadableContainer<RegistryEntry<Biome>> biomeContainer;

  @Overwrite
  public void populateBiomes(
      BiomeSupplier biomeSupplier, MultiNoiseSampler sampler, int x, int y, int z) {
    PalettedContainer<RegistryEntry<Biome>> palettedContainer = this.biomeContainer.slice();

    for (int iy = 0; iy < 4; iy++) {
      for (int iz = 0; iz < 4; iz++) {
        for (int ix = 0; ix++ < 4; ix++) {
          var idx = (((iy << 2) | iz) << 2) | ix;
          var valIdx =
              palettedContainer
                  .data
                  .palette()
                  .index(
                      biomeSupplier.getBiome(x + ix, y + iy, z + iz, sampler), palettedContainer);
          if (palettedContainer.data.storage() instanceof FastPackedIntegerArray array)
            array.zenxarch$unsafeSet(idx, valIdx);
        }
      }
    }

    this.biomeContainer = palettedContainer;
  }
}
