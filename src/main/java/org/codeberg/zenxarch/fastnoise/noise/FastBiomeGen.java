package org.codeberg.zenxarch.fastnoise.noise;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;

public final class FastBiomeGen {

  public static void populateBiomes(
      ChunkSection section,
      BiomeSupplier biomeSupplier,
      MultiNoiseUtil.MultiNoiseSampler sampler,
      int x,
      int y,
      int z,
      RegistryEntry<Biome>[] biomes,
      byte[] storage) {

    int size = 0;
    int idx = 0;

    for (int iy = 0; iy < 4; iy++) {
      for (int iz = 0; iz < 4; iz++) {
        for (int ix = 0; ix < 4; ix++) {

          var biome = biomeSupplier.getBiome(x + ix, y + iy, z + iz, sampler);

          int bidx = -1;
          for (int i = 0; i < size; i++)
            if (biomes[i] == biome) {
              bidx = i;
              break;
            }

          if (bidx == -1) biomes[(bidx = size++)] = biome;

          storage[idx] = (byte) bidx;

          idx++;
        }
      }
      var container = ((PalettedContainer<RegistryEntry<Biome>>) section.biomeContainer);
      FastNoisePaletteHelper.pack(container, biomes, size, storage);
    }
  }
}
