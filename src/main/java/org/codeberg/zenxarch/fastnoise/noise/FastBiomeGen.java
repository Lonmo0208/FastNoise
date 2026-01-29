package org.codeberg.zenxarch.fastnoise.noise;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;

public final class FastBiomeGen {

  public static void populateBiomes(
      Chunk chunk, BiomeSupplier supplier, MultiNoiseSampler sampler) {
    var chunkPos = chunk.getPos();
    var world = chunk.getHeightLimitView();

    int x = chunkPos.x() * 4;
    int y = world.getBottomY() >> 2;
    int z = chunkPos.z() * 4;

    final int maxIdx = world.getHeight() >> 4;
    var sections = chunk.getSectionArray();

    {
      var singleBiome = getSingleBiome(supplier, chunkPos.x(), chunkPos.z());
      if (singleBiome != null) {
        packSingleBiome(sections, maxIdx, singleBiome);
        return;
      }
    }

    @SuppressWarnings("unchecked")
    final RegistryEntry<Biome>[] biomes = new RegistryEntry[64];
    final var storage = new byte[64];

    for (int i = 0; i < maxIdx; i++) {
      var section = sections[i];
      FastBiomeGen.populateBiomes(section, supplier, sampler, x, y, z, biomes, storage);
      y += 4;
    }
  }

  private static RegistryEntry<Biome> getSingleBiome(BiomeSupplier supplier, int x, int z) {
    if (supplier instanceof TheEndBiomeSource theEnd) {
      if (Math.abs(x) > 64) return null;
      if (Math.abs(z) > 64) return null;
      if (x * x + z * z < 4096) return theEnd.centerBiome;
    }

    if (supplier instanceof FixedBiomeSource fixed) {
      return fixed.biome;
    }

    return null;
  }

  private static void packSingleBiome(
      ChunkSection[] sections, final int maxIdx, RegistryEntry<Biome> biome) {
    for (int i = 0; i < maxIdx; i++) {
      FastNoisePaletteHelper.packSingleElement(
          (PalettedContainer<RegistryEntry<Biome>>) sections[i].biomeContainer, biome);
    }
  }

  private static void populateBiomes(
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
