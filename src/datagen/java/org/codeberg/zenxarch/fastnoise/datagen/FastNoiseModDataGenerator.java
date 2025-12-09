package org.codeberg.zenxarch.fastnoise.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

import org.codeberg.zenxarch.fastnoise.FastNoiseMod;
import org.jetbrains.annotations.Nullable;

public class FastNoiseModDataGenerator implements DataGeneratorEntrypoint {
  @Override
  public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {}

  @Override
  public @Nullable String getEffectiveModId() {
    return FastNoiseMod.MOD_ID;
  }
}
