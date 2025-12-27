package org.codeberg.zenxarch.fastnoise;

import java.util.Arrays;
import net.minecraft.block.Blocks;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettesFactory;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.AquiferSampler.FluidLevelSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes.Beardifying;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.codeberg.zenxarch.fastnoise.Worldgen.PopulateNoiseFunction;

public final class TestWorld {
  private final ProtoChunk[] chunks;
  private final PopulateNoiseFunction function;
  private final ChunkGeneratorSettings settings;
  private final PalettesFactory factory;

  public TestWorld(DynamicRegistryManager manager, BenchmarkSettings settings) {
    this(
        manager,
        settings.settings(),
        settings.function(),
        settings.region().pos(),
        settings.seed());
  }

  public TestWorld(
      DynamicRegistryManager manager,
      RegistryKey<ChunkGeneratorSettings> settingIp,
      PopulateNoiseFunction function,
      ChunkPos[] pos,
      long seed) {
    this.function = function;
    this.settings = manager.getEntryOrThrow(settingIp).value();
    var world =
        new FakeWorld(
            this.settings.generationShapeConfig().height(),
            this.settings.generationShapeConfig().minimumY());

    this.factory = PalettesFactory.fromRegistryManager(manager);

    var noiseConfig =
        NoiseConfig.create(this.settings, manager.getOrThrow(RegistryKeys.NOISE_PARAMETERS), seed);

    var lava = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
    var water =
        new AquiferSampler.FluidLevel(this.settings.seaLevel(), this.settings.defaultFluid());
    int cutoff = Math.min(-54, this.settings.seaLevel());
    FluidLevelSampler fluidLevelSampler = (x, y, z) -> y < cutoff ? lava : water;

    this.chunks = new ProtoChunk[pos.length];
    var beardifying = new BeardifyingImpl();
    for (int i = 0; i < pos.length; i++) {
      this.chunks[i] =
          new ProtoChunk(pos[i], UpgradeData.NO_UPGRADE_DATA, world, this.factory, null);

      this.chunks[i].getOrCreateChunkNoiseSampler(
          chunk ->
              ChunkNoiseSampler.create(
                  chunk,
                  noiseConfig,
                  beardifying,
                  this.settings,
                  fluidLevelSampler,
                  Blender.getNoBlending()));
    }
  }

  public void noise() {
    for (int i = 0; i < this.chunks.length; i++) {
      runForChunk(this.chunks[i]);
    }
  }

  public void clear() {
    for (int i = 0; i < this.chunks.length; i++) {
      clearSections(this.chunks[i]);
    }
  }

  private void runForChunk(ProtoChunk chunk) {
    var shapeConfig = this.settings.generationShapeConfig().trimHeight(chunk.getHeightLimitView());

    int minY = shapeConfig.minimumY();
    int minimumCellY = MathHelper.floorDiv(minY, shapeConfig.verticalCellBlockCount());
    int cellHeight =
        MathHelper.floorDiv(shapeConfig.height(), shapeConfig.verticalCellBlockCount());

    var start = chunk.getSectionIndex(minY);
    var end = chunk.getSectionIndex(cellHeight * shapeConfig.verticalCellBlockCount() - 1 + minY);

    var sampler = chunk.getOrCreateChunkNoiseSampler(null);

    this.function.populateNoise(sampler, settings, chunk, minimumCellY, cellHeight, start, end);
  }

  private void clearSections(ProtoChunk chunk) {
    var data = chunk.getSectionArray();
    for (int i = 0; i < data.length; i++) data[i] = new ChunkSection(factory);
    Arrays.setAll(chunk.getHeightmap(Type.OCEAN_FLOOR_WG).asLongArray(), t -> 0);
    Arrays.setAll(chunk.getHeightmap(Type.WORLD_SURFACE_WG).asLongArray(), t -> 0);
  }

  private static record FakeWorld(int height, int bottomY) implements HeightLimitView {

    @Override
    public int getHeight() {
      return height;
    }

    @Override
    public int getBottomY() {
      return bottomY;
    }
  }

  private static class BeardifyingImpl implements Beardifying {

    @Override
    public double maxValue() {
      return 1.0;
    }

    @Override
    public double minValue() {
      return 0.0;
    }

    @Override
    public double sample(NoisePos pos) {
      return 0.5;
    }
  }
}
