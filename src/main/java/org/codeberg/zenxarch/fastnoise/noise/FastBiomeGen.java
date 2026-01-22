package org.codeberg.zenxarch.fastnoise.noise;

import java.util.List;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteType;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.PalettedContainer.Data;
import net.minecraft.world.chunk.SingularPalette;
import org.apache.commons.lang3.mutable.MutableObject;
import org.codeberg.zenxarch.fastnoise.tree.FastSearchTree;

public final class FastBiomeGen {

  private static final Palette.Factory ARRAY = ArrayPalette::create;
  private static final Palette.Factory SINGULAR = SingularPalette::create;

  static final PaletteType SINGULAR_TYPE = new PaletteType.Static(SINGULAR, 0);
  private static final PaletteType ARRAY_1_TYPE = new PaletteType.Static(ARRAY, 1);
  private static final PaletteType ARRAY_2_TYPE = new PaletteType.Static(ARRAY, 2);
  private static final PaletteType ARRAY_3_TYPE = new PaletteType.Static(ARRAY, 3);
  private static final PaletteType ARRAY_4_TYPE = new PaletteType.Static(ARRAY, 4);
  private static final PaletteType ARRAY_5_TYPE = new PaletteType.Static(ARRAY, 5);
  private static final PaletteType ARRAY_6_TYPE = new PaletteType.Static(ARRAY, 6);
  private static final PaletteType[] types =
      new PaletteType[] {
        ARRAY_1_TYPE, ARRAY_2_TYPE, ARRAY_3_TYPE, ARRAY_4_TYPE, ARRAY_5_TYPE, ARRAY_6_TYPE
      };

  public static void populateBiomesUsingFastSearchTree(
      MultiNoiseUtil.MultiNoiseSampler sampler,
      MultiNoiseUtil.NoiseValuePoint[] points,
      RegistryEntry<Biome>[] biomes,
      FastSearchTree<RegistryEntry<Biome>> fastSearchTree,
      int x,
      int y,
      int z,
      MutableObject<FastSearchTree.LeafNode> lastLeaf) {

    {
      int idx = 0;
      for (int iy = 0; iy < 4; iy++) {
        for (int iz = 0; iz < 4; iz++) {
          for (int ix = 0; ix < 4; ix++) {
            points[idx++] = sampler.sample(x + ix, y + iy, z + iz);
          }
        }
      }
    }

    for (int idx = 0; idx < 64; idx++) biomes[idx] = fastSearchTree.search(points[idx], lastLeaf);
  }

  public static void populateBiomesUsingSupplier(
      BiomeSupplier supplier,
      MultiNoiseUtil.MultiNoiseSampler sampler,
      RegistryEntry<Biome>[] biomes,
      int x,
      int y,
      int z) {

    {
      int idx = 0;
      for (int iy = 0; iy < 4; iy++) {
        for (int iz = 0; iz < 4; iz++) {
          for (int ix = 0; ix < 4; ix++) {
            biomes[idx++] = supplier.getBiome(x + ix, y + iy, z + iz, sampler);
          }
        }
      }
    }
  }

  public static void populateBiomes(
      ChunkSection section,
      RegistryEntry<Biome>[] biomes,
      RegistryEntry<Biome>[] reusableArray,
      byte[] storage) {

    int size = 0;

    for (int i = 0; i < 64; i++) {
      int idx = 0;
      for (; idx < size; idx++) {
        if (biomes[i] == reusableArray[idx]) break;
      }

      if (idx == size) reusableArray[size++] = biomes[i];

      storage[i] = (byte) idx;
    }

    modifyContainer(section, reusableArray, size, storage);
  }

  private static void modifyContainer(
      ChunkSection section, RegistryEntry<Biome>[] paletteArray, int paletteSize, byte[] storage) {
    var container = ((PalettedContainer<RegistryEntry<Biome>>) section.biomeContainer);
    if (paletteSize == 1) {
      if (container.data.palette() instanceof SingularPalette<RegistryEntry<Biome>> palette) {
        palette.entry = paletteArray[0];
      } else {
        container.data =
            new Data<>(
                SINGULAR_TYPE,
                new EmptyPaletteStorage(64),
                new SingularPalette<>(List.of(paletteArray[0])));
      }
    } else {
      int bits = MathHelper.ceilLog2(paletteSize);
      @SuppressWarnings("unchecked")
      RegistryEntry<Biome>[] downSizedBiomes = new RegistryEntry[1 << bits];
      System.arraycopy(paletteArray, 0, downSizedBiomes, 0, paletteSize);
      ((PalettedContainer<RegistryEntry<Biome>>) section.biomeContainer).data =
          new Data<RegistryEntry<Biome>>(
              types[bits],
              toStorage(paletteArray, bits, storage),
              new ArrayPalette<>(downSizedBiomes, bits, paletteSize));
    }
  }

  private static final int[] sizes = new int[] {-1, 1, 2, 4, 4, 6, 7};

  private static PaletteStorage toStorage(RegistryEntry<Biome>[] palette, int bits, byte[] data) {
    var storage = new long[sizes[bits]];

    int idx = 0;
    for (int i = 0; i < bits; i++) {
      for (int j = 0; (j + bits) < 65; j += bits) {
        storage[i] |= ((long) data[idx++]) << j;
      }
    }
    int j = 0;
    while (idx < 64) {
      storage[bits] |= ((long) data[idx++]) << j;
      j += bits;
    }

    return new PackedIntegerArray(bits, 64, storage);
  }
}
