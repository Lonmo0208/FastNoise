package org.codeberg.zenxarch.fastnoise.config;

import static org.codeberg.zenxarch.fastnoise.config.BooleanConfigEntry.*;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;

public class FastNoiseConfigEntries {
  public static final BooleanConfigEntry OPTIMIZE_END_BIOMES =
      of(
          "perf.biomes.end",
          "Optimize end biome generation (2x speed) but may cause mod incompatibility",
          false);

  public static final BooleanConfigEntry OPTIMIZE_FIXED_BIOMES =
      of(
          "perf.biomes.fixed",
          "Optimize single biome generation (major speedup) but may cause mod incompatibility",
          false);

  public static final BooleanConfigEntry SKIP_TRIVIAL_SURFACE_BUILDER =
      of(
          "perf.surface.trivial",
          "Skip trivial surface builder. Major speedup for end dimension. Depends on surface"
              + " builder",
          true);

  public static final BooleanConfigEntry OPTIMIZE_BIOME_ACCESS =
      of(
          "perf.surface.biome",
          "Predict biomes in advance in surface builder. Major speedup for surface builder but may"
              + " cause mod incompatibility",
          false);

  public static final BooleanConfigEntry MIXIN_PERF_BIOMES =
      of("mixin.perf.biome", "Replace populateBiomes with optimized implementation", true);

  public static final BooleanConfigEntry MIXIN_PERF_NOISE =
      of("mixin.perf.noise", "Replace populateNoise with optimized implementation", true);

  public static final BooleanConfigEntry MIXIN_PERF_SURFACE =
      of("mixin.perf.surface", "Replace buildSurface with optimized implementation", false);

  public static final List<BooleanConfigEntry> ENTRIES =
      new ObjectArrayList<>(
          List.of(
              OPTIMIZE_END_BIOMES,
              OPTIMIZE_FIXED_BIOMES,
              SKIP_TRIVIAL_SURFACE_BUILDER,
              OPTIMIZE_BIOME_ACCESS,
              MIXIN_PERF_BIOMES,
              MIXIN_PERF_NOISE,
              MIXIN_PERF_SURFACE));
}
