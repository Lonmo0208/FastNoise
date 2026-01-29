package org.codeberg.zenxarch.fastnoise.noise;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.EmptyPaletteStorage;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.collection.PaletteStorage;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PaletteType;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.PalettedContainer.Data;
import net.minecraft.world.chunk.SingularPalette;

public final class FastNoisePaletteHelper {
  private FastNoisePaletteHelper() {
    throw new IllegalStateException("Utility class");
  }

  private static final Palette.Factory ARRAY = ArrayPalette::create;
  private static final Palette.Factory SINGULAR = SingularPalette::create;

  private static final PaletteType SINGULAR_TYPE = new PaletteType.Static(SINGULAR, 0);
  private static final PaletteType ARRAY_1_TYPE = new PaletteType.Static(ARRAY, 1);
  private static final PaletteType ARRAY_2_TYPE = new PaletteType.Static(ARRAY, 2);
  private static final PaletteType ARRAY_3_TYPE = new PaletteType.Static(ARRAY, 3);
  private static final PaletteType ARRAY_4_TYPE = new PaletteType.Static(ARRAY, 4);
  private static final PaletteType ARRAY_5_TYPE = new PaletteType.Static(ARRAY, 5);
  private static final PaletteType ARRAY_6_TYPE = new PaletteType.Static(ARRAY, 6);

  private static final PaletteType[] biomePaletteTypes =
      new PaletteType[] {
        ARRAY_1_TYPE, ARRAY_2_TYPE, ARRAY_3_TYPE, ARRAY_4_TYPE, ARRAY_5_TYPE, ARRAY_6_TYPE
      };

  public static void initBlockStateContainer(
      ChunkSection section, ArrayPalette<BlockState> palette, long[] storage) {
    section.blockStateContainer.data =
        new Data<BlockState>(ARRAY_4_TYPE, new PackedIntegerArray(4, 4096, storage), palette);
  }

  public static void pack(
      PalettedContainer<RegistryEntry<Biome>> container,
      RegistryEntry<Biome>[] biomes,
      int size,
      byte[] storage) {
    if (size == 1) {
      if (container.data.palette() instanceof SingularPalette<RegistryEntry<Biome>> palette) {
        palette.entry = biomes[0];
      } else {
        container.data =
            new Data<>(
                SINGULAR_TYPE,
                new EmptyPaletteStorage(64),
                new SingularPalette<>(List.of(biomes[0])));
      }
    } else {
      int bits = MathHelper.ceilLog2(size);
      @SuppressWarnings("unchecked")
      RegistryEntry<Biome>[] downSizedBiomes = new RegistryEntry[1 << bits];
      System.arraycopy(biomes, 0, downSizedBiomes, 0, size);
      container.data =
          new Data<RegistryEntry<Biome>>(
              biomePaletteTypes[bits],
              repackBiomeStorage(biomes, bits, storage),
              new ArrayPalette<>(downSizedBiomes, bits, size));
    }
  }

  private static final int[] biomeStorageSizes = new int[] {-1, 1, 2, 4, 4, 6, 7};

  private static PaletteStorage repackBiomeStorage(
      RegistryEntry<Biome>[] palette, int bits, byte[] data) {
    var storage = new long[biomeStorageSizes[bits]];

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
