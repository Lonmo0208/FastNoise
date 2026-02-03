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
import net.minecraft.world.gen.densityfunction.DensityFunction;

public final class FastBiomeGen {

  public static void populateBiomes(
      Chunk chunk, BiomeSupplier supplier, MultiNoiseSampler sampler) {

    var sections = chunk.getSectionArray();

    if (supplier instanceof FixedBiomeSource fixed) {
      packSingleBiome(sections, fixed.biome);
      return;
    }

    final var chunkPos = chunk.getPos();
    final int cx = chunkPos.x();
    final int cz = chunkPos.z();

    if (supplier instanceof TheEndBiomeSource theEnd) {
      populateEndBiomes(theEnd, chunk, sections, cx, cz, sampler);
      return;
    }

    final int minY = chunk.getBottomY();
    final int x = cx << 2;
    int y = minY >> 2;
    final int z = cx << 2;

    @SuppressWarnings("unchecked")
    final RegistryEntry<Biome>[] biomes = new RegistryEntry[64];
    final var storage = new byte[64];

    for (int i = 0; i < sections.length; i++) {
      var section = sections[i];
      FastBiomeGen.populateBiomes(section, supplier, sampler, x, y, z, biomes, storage);
      y += 4;
    }
  }

  private static void packSingleBiome(ChunkSection[] sections, RegistryEntry<Biome> biome) {
    for (int i = 0; i < sections.length; i++) {
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

  private static class EndBiomeNoisePos implements DensityFunction.NoisePos {
    private final int x;
    private final int z;
    public int y;
    private final DensityFunction sampler;

    public EndBiomeNoisePos(int x, int y, int z, DensityFunction sampler) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.sampler = sampler;
    }

    @Override
    public int blockX() {
      return x;
    }

    @Override
    public int blockY() {
      return y;
    }

    @Override
    public int blockZ() {
      return z;
    }

    public double sampleAndStep() {
      var result = sampler.sample(this);
      this.y += 4;
      return result;
    }
  }

  private static RegistryEntry<Biome> getEndBiomeFromHeight(
      TheEndBiomeSource source, double height) {
    if (height > 0.25) return source.highlandsBiome;
    if (height >= -0.0625) return source.midlandsBiome;
    if (height < -0.21875) return source.smallIslandsBiome;
    return source.barrensBiome;
  }

  private static void populateEndBiomes(
      TheEndBiomeSource source,
      Chunk chunk,
      ChunkSection[] sections,
      int cx,
      int cz,
      MultiNoiseUtil.MultiNoiseSampler sampler) {
    if ((Math.abs(cx) <= 64) && ((Math.abs(cz) <= 64)) && ((cx * cx + cz * cz) <= 4096)) {
      packSingleBiome(sections, source.centerBiome);
      return;
    }

    final int x = (cx << 4) + 8;
    final int z = (cz << 4) + 8;

    var noisePos = new EndBiomeNoisePos(x, chunk.getBottomY(), z, sampler.erosion());

    for (int i = 0; i < sections.length; i++) {
      var a = getEndBiomeFromHeight(source, noisePos.sampleAndStep());
      var b = getEndBiomeFromHeight(source, noisePos.sampleAndStep());
      var c = getEndBiomeFromHeight(source, noisePos.sampleAndStep());
      var d = getEndBiomeFromHeight(source, noisePos.sampleAndStep());

      FastNoisePaletteHelper.packFourEntries(
          (PalettedContainer<RegistryEntry<Biome>>) sections[i].biomeContainer, a, b, c, d);
    }
  }
}
