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

  private static final Object2ObjectMap<String, String> COMMENTS =
      new Object2ObjectArrayMap<>(
          Map.of(
              MIXIN_KEYS[0],
              "Replace populateNoise with optimized implementation",
              MIXIN_KEYS[1],
              "Replace populateBiomes with optimized implementation",
              MIXIN_KEYS[2],
              "Replace buildSurface with optimized implementation"));

  static {
    loadDefaults();
  }

  public static CommentedFileConfig getConfig() {
    return CommentedFileConfig.of(
        FabricLoader.getInstance().getConfigDir().resolve(configFileName));
  }

  private static void loadDefaults() {
    CONFIG.load();
    for (var key : MIXIN_KEYS) {
      Optional<Boolean> value = CONFIG.getOptional(key);
      FastNoiseConstants.LOGGER.info("{} -> {}", key, value);
      if (value.isEmpty()) {
        CONFIG.set(key, true);
      }
      if (!CONFIG.containsComment(key)) {
        CONFIG.setComment(key, COMMENTS.get(key));
      }
    }
    CONFIG.save();
  }
}
