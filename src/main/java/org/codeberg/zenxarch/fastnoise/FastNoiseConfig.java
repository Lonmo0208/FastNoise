package org.codeberg.zenxarch.fastnoise;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;

public class FastNoiseConfig {
  private static final String configFileName = FastNoiseConstants.MOD_ID + ".mixin.properties";

  private static final String[] keys = new String[] {"mixin.perf.noise", "mixin.perf.biome"};

  private static final Object2BooleanMap<String> defaults =
      Util.make(
          () -> {
            var m = new Object2BooleanArrayMap<String>();
            m.put(keys[0], true);
            m.put(keys[1], true);
            return Object2BooleanMaps.unmodifiable(m);
          });

  private static Path getConfigPath() {
    var configDir = FabricLoader.getInstance().getConfigDir();
    var configPath = configDir.resolve(configFileName);

    if (!Files.exists(configPath)) {
      try {
        Files.createFile(configPath);
      } catch (Exception e) {
        FastNoiseConstants.LOGGER.error("Cannot create file " + configPath.toString(), e);
        return null;
      }
    }

    if (!Files.isRegularFile(configPath)) {
      FastNoiseConstants.LOGGER.error("Config file " + configPath.toString() + " is not a file");
      return null;
    }

    return configPath;
  }

  private static Properties loadProperties() {
    var result = new Properties();
    var configPath = getConfigPath();

    if (configPath == null) return result;

    try {
      result.load(Files.newInputStream(configPath));
    } catch (Exception e) {
      FastNoiseConstants.LOGGER.error("Cannot read file " + configPath.toString(), e);
      return result;
    }

    return result;
  }

  private static void saveProperties(Properties props) {
    var configPath = getConfigPath();

    if (configPath == null) return;

    try {
      props.store(Files.newOutputStream(configPath), "");
    } catch (Exception e) {
      FastNoiseConstants.LOGGER.error("Cannot write file " + configPath.toString(), e);
    }
  }

  public static Object2BooleanMap<String> loadConfig() {
    var props = loadProperties();
    var result = new Object2BooleanArrayMap<String>();
    for (var key : keys) {
      boolean r = defaults.getOrDefault(key, true);
      try {
        r = Boolean.parseBoolean(props.getProperty(key, Boolean.toString(r)));
      } catch (Exception e) {

      }
      props.setProperty(key, Boolean.toString(r));
      result.put(key, r);
    }

    saveProperties(props);
    return result;
  }
}
