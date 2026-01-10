package org.codeberg.zenxarch.fastnoise;

import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.MultiNoiseSampler;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.AquiferSampler.FluidLevelSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes.Beardifying;
import net.minecraft.world.gen.noise.NoiseConfig;

public final class TestWorld {
  public final ChunkGeneratorSettings settings;
  private final BiomeSource biomeSource;
  private final NoiseConfig noiseConfig;
  private final FluidLevelSampler fluidLevelSampler;

  private static final Beardifying beardifying = new BeardifyingImpl();

  private final FakeWorld world;

  public TestWorld(BenchmarkSettings settings) {
    this(settings.dimensionOptions(), settings.seed());
  }

  public TestWorld(RegistryKey<DimensionOptions> optionsKey, long seed) {
    var manager = TestGlobals.getManager();
    var options =
        manager
            .getEntryOrThrow(WorldPresets.DEFAULT)
            .value()
            .createDimensionsRegistryHolder()
            .dimensions()
            .get(optionsKey);

    if (!(options.chunkGenerator() instanceof NoiseChunkGenerator chunkGenerator)) {
      throw new IllegalStateException("Chunk generator must be noise chunk generator");
    }

    this.settings = chunkGenerator.getSettings().value();
    this.world =
        new FakeWorld(
            this.settings.generationShapeConfig().height(),
            this.settings.generationShapeConfig().minimumY());

    this.noiseConfig =
        NoiseConfig.create(this.settings, manager.getOrThrow(RegistryKeys.NOISE_PARAMETERS), seed);

    var lava = new AquiferSampler.FluidLevel(-54, Blocks.LAVA.getDefaultState());
    var water =
        new AquiferSampler.FluidLevel(this.settings.seaLevel(), this.settings.defaultFluid());
    int cutoff = Math.min(-54, this.settings.seaLevel());
    this.fluidLevelSampler = (x, y, z) -> y < cutoff ? lava : water;

    this.biomeSource = chunkGenerator.getBiomeSource();
  }

  public ProtoChunk createChunk(ChunkPos pos) {
    return new ProtoChunk(
        pos, UpgradeData.NO_UPGRADE_DATA, this.world, TestGlobals.getFactory(), null);
  }

  public ChunkNoiseSampler createSampler(Chunk chunk) {
    return ChunkNoiseSampler.create(
        chunk, noiseConfig, beardifying, this.settings, fluidLevelSampler, Blender.getNoBlending());
  }

  public MultiNoiseSampler createMultiNoiseSampler(ProtoChunk chunk) {
    // new sampler is created for chunk during biome phase
    return this.createSampler(chunk)
        .createMultiNoiseSampler(this.noiseConfig.getNoiseRouter(), this.settings.spawnTarget());
  }

  public BiomeSupplier getBiomeSupplier(ProtoChunk chunk) {
    return BelowZeroRetrogen.getBiomeSupplier(
        Blender.getNoBlending().getBiomeSupplier(this.biomeSource), chunk);
  }

  public static void resetNoise(ProtoChunk chunk) {
    var data = chunk.getSectionArray();
    for (int i = 0; i < data.length; i++)
      data[i].blockStateContainer = data[i].blockStateContainer.slice();
    var len = chunk.getHeightmap(Type.OCEAN_FLOOR_WG).asLongArray().length;
    chunk.setHeightmap(Type.OCEAN_FLOOR_WG, new long[len]);
    len = chunk.getHeightmap(Type.WORLD_SURFACE_WG).asLongArray().length;
    chunk.setHeightmap(Type.WORLD_SURFACE_WG, new long[len]);
  }

  public static boolean matches(ProtoChunk a, ProtoChunk b) {
    var self = a.getSectionArray();
    var other = b.getSectionArray();

    if (self.length != other.length) return false;

    for (int i = 0; i < self.length; i++) {
      if (!matches(self[i].blockStateContainer, other[i].blockStateContainer)) return false;
      if (!matches(
          (PalettedContainer<RegistryEntry<Biome>>) self[i].biomeContainer,
          (PalettedContainer<RegistryEntry<Biome>>) other[i].biomeContainer)) return false;
    }
    return true;
  }

  private static <T> boolean matches(PalettedContainer<T> self, PalettedContainer<T> other) {
    if (self.data.storage().getSize() != other.data.storage().getSize()) return false;
    for (int i = 0; i < self.data.storage().getSize(); i++) {
      if (self.data.palette().get(self.data.storage().get(i))
          != other.data.palette().get(other.data.storage().get(i))) {
        return false;
      }
    }
    return true;
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
