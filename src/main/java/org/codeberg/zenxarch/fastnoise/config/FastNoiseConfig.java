package org.codeberg.zenxarch.fastnoise.config;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue.CvArray;
import net.fabricmc.loader.api.metadata.CustomValue.CvObject;
import net.fabricmc.loader.api.metadata.CustomValue.CvType;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.codeberg.zenxarch.fastnoise.FastNoiseConstants;

public class FastNoiseConfig {
  private static final String overridesKey = FastNoiseConstants.MOD_ID + ":overrides";

  private static boolean getProperty(String property, boolean def) {
    var prop = System.getProperty(property, Boolean.toString(def));
    if (prop == null) return def;
    try {
      return Boolean.parseBoolean(prop);
    } catch (Exception e) {
      return def;
    }
  }

  public static boolean ENABLED = getProperty("zmixin", true);

  static {
    FastNoiseConstants.LOGGER.info("Mod Enabled: {}", ENABLED);
  }

  public static final boolean OPTIMIZE_END_BIOMES = FastNoiseConfigLoader.optimizeEndBiomes();
  public static final boolean OPTIMIZE_FIXED_BIOMES = FastNoiseConfigLoader.optimizeFixedBiomes();

  private static void collectOverrides(
      Object2BooleanArrayMap<String> map, ModMetadata meta, String key, boolean value) {
    if (!map.containsKey(key)) {
      FastNoiseConstants.LOGGER.error("Mod {} tried to override unknown key {}", meta.getId(), key);
    } else {
      FastNoiseConstants.LOGGER.info(
          "Mod {} override key {} with value {}", meta.getId(), key, value);
      map.put(key, value);
    }
  }

  private static void collectOverrides(
      Object2BooleanArrayMap<String> map, ModMetadata meta, CvArray array) {
    for (var value : array) {
      if (value.getType() != CvType.STRING) {
        FastNoiseConstants.LOGGER.error(
            "Mod {} has array of not strings as overrides", meta.getId());
        continue;
      }
      collectOverrides(map, meta, value.getAsString(), false);
    }
  }

  private static void collectOverrides(
      Object2BooleanArrayMap<String> map, ModMetadata meta, CvObject object) {
    for (var value : object) {
      if (value.getValue().getType() != CvType.BOOLEAN) {
        FastNoiseConstants.LOGGER.error(
            "Mod {} has object of not booleans as overrides", meta.getId());
        continue;
      }
      collectOverrides(map, meta, value.getKey(), value.getValue().getAsBoolean());
    }
  }

  private static void collectOverrides(Object2BooleanArrayMap<String> map) {
    for (var container : FabricLoader.getInstance().getAllMods()) {
      var meta = container.getMetadata();
      if (!meta.containsCustomValue(overridesKey)) continue;

      var value = meta.getCustomValue(overridesKey);
      switch (value.getType()) {
        case CvType.OBJECT -> collectOverrides(map, meta, value.getAsObject());
        case CvType.ARRAY -> collectOverrides(map, meta, value.getAsArray());
        case CvType.STRING -> collectOverrides(map, meta, value.getAsString(), false);
        default ->
            FastNoiseConstants.LOGGER.error(
                "Mod {} has unsupported overrides of type {}", meta.getId(), value.getType());
      }
    }
  }

  public static Object2BooleanMap<String> loadConfig() {
    var result = new Object2BooleanArrayMap<String>();
    for (var key : FastNoiseConfigLoader.MIXIN_KEYS) {
      boolean r = FastNoiseConfigLoader.CONFIG.get(key);
      result.put(key, r);
    }

    collectOverrides(result);
    return Object2BooleanMaps.unmodifiable(result);
  }
}
