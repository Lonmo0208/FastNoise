package org.codeberg.zenxarch.fastnoise;

import net.minecraft.world.chunk.ChunkStatus;
import org.codeberg.zenxarch.fastnoise.config.FastNoiseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ParityTest {
  public static final Logger LOGGER = LoggerFactory.getLogger(ParityTest.class);

  public static void doParityTest() {
    LOGGER.info("Starting parity test");
    doParityTest(BenchmarkSettings.overworld());
    doParityTest(BenchmarkSettings.nether());
    doParityTest(BenchmarkSettings.end());
  }

  private static void doParityTest(BenchmarkSettings settings) {
    var fakeWorld = new TestWorld(settings);

    var name = settings.dimensionOptions().getValue().getPath();
    LOGGER.info("Generating " + name);

    FastNoiseConfig.ENABLED = false;

    var original = ChunkRegion.of(fakeWorld, settings.region());
    var originalBiomes = ChunkRegion.of(fakeWorld, settings.biomeRegion());

    generateChunk(fakeWorld, original, originalBiomes);

    FastNoiseConfig.ENABLED = true;

    var modded = ChunkRegion.of(fakeWorld, settings.region());
    var moddedBiomes = ChunkRegion.of(fakeWorld, settings.biomeRegion());

    generateChunk(fakeWorld, modded, moddedBiomes);

    for (int i = 0; i < modded.chunks().length; i++) {
      var a = modded.chunks()[i];
      var b = original.getChunk(modded.chunks()[i].getPos());

      assert (TestWorld.matches(a, b));
    }
    LOGGER.info(name + " matches");
  }

  private static void generateChunk(TestWorld world, ChunkRegion base, ChunkRegion biomeRegion) {
    for (int i = 0; i < biomeRegion.chunks().length; i++) {
      world.biomes(biomeRegion.chunks()[i]);
      biomeRegion.chunks()[i].setStatus(ChunkStatus.BIOMES);
    }

    base.copyBiomes(biomeRegion);

    for (int i = 0; i < base.chunks().length; i++) {
      base.chunks()[i].getOrCreateChunkNoiseSampler(world::createSampler);
      world.noise(base.chunks()[i]);
      world.surface(base.chunks()[i], biomeRegion);
    }
  }
}
