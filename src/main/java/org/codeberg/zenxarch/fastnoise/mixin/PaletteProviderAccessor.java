package org.codeberg.zenxarch.fastnoise.mixin;

import net.minecraft.world.chunk.PaletteProvider;
import net.minecraft.world.chunk.PaletteType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PaletteProvider.class)
public interface PaletteProviderAccessor {
  @Invoker("createType")
  public PaletteType zenxarch$createType(int bitsInStorage);
}
