package org.codeberg.zenxarch.fastnoise.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import org.codeberg.zenxarch.fastnoise.FastNoiseConstants;

public final class FastNoiseConfigLoader {
  private static final String configFileName = FastNoiseConstants.MOD_ID + ".mixin.toml";

  public static final CommentedFileConfig CONFIG = getConfig();

  static final String[] MIXIN_KEYS =
      new String[] {"mixin.perf.noise", "mixin.perf.biome", "mixin.perf.surface"};

  static final String[] DISABLED_BY_DEFAULT_KEYS =
      new String[] {"perf.biomes.end", "perf.biomes.fixed", "mixin.perf.surface"};

  static boolean optimizeEndBiomes() {
    return CONFIG.get(DISABLED_BY_DEFAULT_KEYS[0]);
  }

  static boolean optimizeFixedBiomes() {
    return CONFIG.get(DISABLED_BY_DEFAULT_KEYS[1]);
  }

  private static final Object2ObjectMap<String, String> COMMENTS =
      new Object2ObjectArrayMap<>(
          Map.of(
              MIXIN_KEYS[0],
              "Replace populateNoise with optimized implementation",
              MIXIN_KEYS[1],
              "Replace populateBiomes with optimized implementation",
              MIXIN_KEYS[2],
              "Replace buildSurface with optimized implementation"));

  private static void putComment(String value, String comment) {
    COMMENTS.put(value, comment);
  }

  static {
    putComment(
        DISABLED_BY_DEFAULT_KEYS[0],
        "Optimize end biome generation (2x speed) but may cause mod incompatibility");
    putComment(
        DISABLED_BY_DEFAULT_KEYS[1],
        "Optimize single biome generation (major speedup) but may cause mod incompatibility");
    loadDefaults();
  }

  public static CommentedFileConfig getConfig() {
    return CommentedFileConfig.of(
        FabricLoader.getInstance().getConfigDir().resolve(configFileName));
  }

  private static void loadBoolean(String key, boolean defaultValue) {
    Optional<Boolean> value = CONFIG.getOptional(key);
    if (value.isEmpty()) {
      CONFIG.set(key, defaultValue);
    }
    if (!CONFIG.containsComment(key)) {
      CONFIG.setComment(key, COMMENTS.get(key));
    }
  }

  private static void loadDefaults() {
    CONFIG.load();
    for (var key : DISABLED_BY_DEFAULT_KEYS) loadBoolean(key, false);
    for (var key : MIXIN_KEYS) loadBoolean(key, true);
    CONFIG.save();
  }
}
