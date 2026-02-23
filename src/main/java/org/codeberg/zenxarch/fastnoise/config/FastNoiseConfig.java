package org.codeberg.zenxarch.fastnoise.config;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import java.util.List;
import java.util.Map;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.codeberg.zenxarch.fastnoise.FastNoiseConstants;

public class FastNoiseConfig {
  private static final String overridesKey = FastNoiseConstants.MOD_ID + ":overrides";

  static boolean get(BooleanConfigEntry entry) {
    return FastNoiseConfigLoader.CONFIG.get(entry.key());
  }

  public static final boolean OPTIMIZE_END_BIOMES = get(FastNoiseConfigEntries.OPTIMIZE_END_BIOMES);
  public static final boolean OPTIMIZE_FIXED_BIOMES =
      get(FastNoiseConfigEntries.OPTIMIZE_FIXED_BIOMES);
  public static final boolean SKIP_TRIVIAL_SURFACE_BUILDER =
      get(FastNoiseConfigEntries.SKIP_TRIVIAL_SURFACE_BUILDER);
  public static final boolean OPTIMIZE_BIOME_ACCESS =
      get(FastNoiseConfigEntries.OPTIMIZE_BIOME_ACCESS);

  private static void collectOverrides(
      Object2BooleanArrayMap<String> map, ModInfo meta, String key, boolean value) {
    if (!map.containsKey(key)) {
      FastNoiseConstants.LOGGER.error(
          "Mod {} tried to override unknown key {}", meta.getModId(), key);
    } else {
      FastNoiseConstants.LOGGER.info(
          "Mod {} override key {} with value {}", meta.getModId(), key, value);
      map.put(key, value);
    }
  }

  private static void collectOverrides(
      Object2BooleanArrayMap<String> map, ModInfo meta, List<?> array) {
    for (var value : array) {
      if (!(value instanceof String string)) {
        FastNoiseConstants.LOGGER.error(
            "Mod {} has array of not strings as overrides", meta.getModId());
        continue;
      }
      collectOverrides(map, meta, string, false);
    }
  }

  private static void collectOverrides(
      Object2BooleanArrayMap<String> map, ModInfo meta, Map<?, ?> object) {
    for (var value : object.keySet()) {
      if (!(value instanceof String key)) continue;
      if (!(object.get(value) instanceof Boolean bl)) {
        FastNoiseConstants.LOGGER.error(
            "Mod {} has object of not booleans as overrides", meta.getModId());
        continue;
      }
      collectOverrides(map, meta, key, bl);
    }
  }

  private static void collectOverrides(Object2BooleanArrayMap<String> map) {
    for (var container : FMLLoader.getCurrent().getLoadingModList().getMods()) {
      var meta = container.getConfigElement(overridesKey);
      if (meta.isEmpty()) continue;

      var value = meta.get();
      switch (value) {
        case Map<?, ?> otherMap -> collectOverrides(map, container, otherMap);
        case List<?> list -> collectOverrides(map, container, list);
        case String string -> collectOverrides(map, container, string, false);
        default ->
            FastNoiseConstants.LOGGER.error(
                "Mod {} has unsupported overrides of type {}",
                container.getModId(),
                value.getClass().getSimpleName());
      }
    }
  }

  public static Object2BooleanMap<String> loadConfig() {
    var result = new Object2BooleanArrayMap<String>();
    for (var entry : FastNoiseConfigEntries.ENTRIES) {
      if (!entry.isMixin()) continue;
      boolean r = get(entry);
      result.put(entry.key(), r);
    }

    collectOverrides(result);
    return Object2BooleanMaps.unmodifiable(result);
  }
}
