package org.codeberg.zenxarch.example.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.codeberg.zenxarch.example.ExampleMod;
import org.jetbrains.annotations.Nullable;

public class ExampleModDataGenerator implements DataGeneratorEntrypoint {
  @Override
  public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {}

  @Override
  public @Nullable String getEffectiveModId() {
    return ExampleMod.MOD_ID;
  }
}
