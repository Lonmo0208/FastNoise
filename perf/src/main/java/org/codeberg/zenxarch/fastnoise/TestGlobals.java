package org.codeberg.zenxarch.fastnoise;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.chunk.PalettesFactory;

public final class TestGlobals {
  private static DynamicRegistryManager manager;
  private static PalettesFactory factory;

  public static DynamicRegistryManager getManager() {
    return manager;
  }

  public static PalettesFactory getFactory() {
    return factory;
  }

  public static void setManager(DynamicRegistryManager manager) {
    TestGlobals.manager = manager;
    TestGlobals.factory = PalettesFactory.fromRegistryManager(manager);
  }
}
