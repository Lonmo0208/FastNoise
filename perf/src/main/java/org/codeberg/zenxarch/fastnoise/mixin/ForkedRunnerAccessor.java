package org.codeberg.zenxarch.fastnoise.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = {"org/openjdk/jmh/runner/ForkedMain"})
public interface ForkedRunnerAccessor {
  @Invoker("main")
  public static void zenxarch$main(String[] args) {}
}
