package org.codeberg.zenxarch.fastnoise.surface;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.SingularPalette;
import org.codeberg.zenxarch.fastnoise.config.FastNoiseConfig;

public final class MaterialRuleContextHelper {
  private MaterialRuleContextHelper() {}

  public static RegistryEntry<Biome>[] calculateSingleBiomes(Chunk chunk) {
    if (!FastNoiseConfig.OPTIMIZE_BIOME_ACCESS) return null;
    var sections = chunk.getSectionArray();
    @SuppressWarnings("unchecked")
    RegistryEntry<Biome>[] result = new RegistryEntry[sections.length];
    for (int i = 0; i < sections.length; i++) {
      result[i] = singleBiome(sections[i]);
    }
    return result;
  }

  private static RegistryEntry<Biome> singleBiome(ChunkSection section) {
    if (section.getBiomeContainer()
        instanceof PalettedContainer<RegistryEntry<Biome>> palettedContainer) {
      if (palettedContainer.data.palette()
          instanceof SingularPalette<RegistryEntry<Biome>> single) {
        return single.entry;
      }
    }
    return null;
  }

  public static RegistryEntry<Biome> getSingleBiome(
      int blockX, int blockY, int blockZ, int minY, RegistryEntry<Biome>[] singleBiomes) {
    var x = blockX & 15;
    if (x < 2 || x > 14) return null;
    var z = blockZ & 15;
    if (z < 2 || z > 14) return null;

    var y = blockY - minY;
    var ly = y & 0xF;
    var cy = y >> 4;

    var single = singleBiomes[cy];

    if (single == null) return null;

    if (ly < 2) {
      if (cy == 0) return null;
      if (singleBiomes[cy] != singleBiomes[cy - 1]) return null;
    }

    if (ly > 14) {
      if (cy == (singleBiomes.length - 1)) return null;
      if (singleBiomes[cy] != singleBiomes[cy + 1]) return null;
    }

    return single;
  }
}
