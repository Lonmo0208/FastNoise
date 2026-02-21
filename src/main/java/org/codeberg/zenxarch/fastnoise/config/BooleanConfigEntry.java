package org.codeberg.zenxarch.fastnoise.config;

public record BooleanConfigEntry(String key, String comment, boolean defaultValue) {
  public static BooleanConfigEntry of(String key, String comment, boolean defaultValue) {
    return new BooleanConfigEntry(key, comment + "\n" + "default: " + defaultValue, defaultValue);
  }

  public static BooleanConfigEntry mixin(String key, String comment, boolean defaultValue) {
    return of("mixin." + key, comment, defaultValue);
  }

  public boolean isMixin() {
    return this.key.startsWith("mixin.");
  }
}
