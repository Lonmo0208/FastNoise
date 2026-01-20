package org.codeberg.zenxarch.fastnoise.mixin;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.Entries;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.NoiseHypercube;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree;
import org.codeberg.zenxarch.fastnoise.FastNoiseMod;
import org.codeberg.zenxarch.fastnoise.tree.FastSearchTree;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entries.class)
public abstract class EntriesMixin<T> {
  private @Final SearchTree<T> tree;

  @Unique @Final private FastSearchTree<T> zenxarch$searchtree;

  @Inject(method = "<init>(Ljava/util/List;)V", at = @At("TAIL"))
  private void zenxarch$initFastTree(List<Pair<NoiseHypercube, T>> entries, CallbackInfo ci) {
    FastSearchTree<T> tree;
    try {
      tree = new FastSearchTree<T>(this.tree);
    } catch (Exception e) {
      FastNoiseMod.LOGGER.info("Cannot generate search tree: {}", e.getMessage());
      tree = null;
    }
    this.zenxarch$searchtree = tree;
  }

  @Inject(
      method =
          "getValue(Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$NoiseValuePoint;)Ljava/lang/Object;",
      at = @At("HEAD"),
      cancellable = true)
  private void zenxarch$redirectToFastTree(
      MultiNoiseUtil.NoiseValuePoint point, CallbackInfoReturnable<T> cir) {
    if (this.zenxarch$searchtree == null) return;
    try {
      cir.setReturnValue(this.zenxarch$searchtree.search(point));
    } catch (Exception e) {
      FastNoiseMod.LOGGER.info("Failed to search tree: {}", e.getMessage());
    }
  }
}
