package org.codeberg.zenxarch.fastnoise.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import org.codeberg.zenxarch.fastnoise.FastNoiseConstants;

public final class FastNoiseConfigLoader {
  private static final String configFileName = FastNoiseConstants.MOD_ID + ".mixin.toml";

  public static final CommentedFileConfig CONFIG = getConfig();

  static {
    loadDefaults();
  }

  public static CommentedFileConfig getConfig() {
    return CommentedFileConfig.of(
        FabricLoader.getInstance().getConfigDir().resolve(configFileName));
  }

  private static void loadBoolean(BooleanConfigEntry entry) {
    Optional<Boolean> value = CONFIG.getOptional(entry.key());
    if (value.isEmpty()) {
      CONFIG.set(entry.key(), entry.defaultValue());
    }
    if (!CONFIG.containsComment(entry.key())) {
      CONFIG.setComment(entry.key(), entry.comment());
    }
  }

  private static void loadDefaults() {
    CONFIG.load();
    for (var entry : FastNoiseConfigEntries.ENTRIES) {
      loadBoolean(entry);
    }
    CONFIG.save();
  }
}
