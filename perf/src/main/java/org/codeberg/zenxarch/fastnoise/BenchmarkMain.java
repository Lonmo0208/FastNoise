package org.codeberg.zenxarch.fastnoise;

import java.util.concurrent.TimeUnit;
import net.minecraft.registry.DynamicRegistryManager;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class BenchmarkMain {
  public static void runTest(DynamicRegistryManager manager) {
    TestGlobals.setManager(manager);

    if (System.getProperty("zperfbenchmark", "").equals("parity")) {
      doParityTest();
      return;
    }

    runFor(5, 5, "Cold ");
    runFor(20, 60, "Hot ");
  }

  private static void doParityTest() {
    FastNoiseMod.LOGGER.info("Starting parity test");
    doParityTest(BenchmarkSettings.overworld());
    doParityTest(BenchmarkSettings.nether());
    doParityTest(BenchmarkSettings.end());
  }

  private static void doParityTest(BenchmarkSettings settings) {
    var fakeWorld = new TestWorld(settings);

    var name = settings.dimensionOptions().getValue().getPath();

    FastNoiseMod.LOGGER.info("Generating " + name);
    for (var pos : settings.region().pos()) {
      var a = fakeWorld.createChunk(pos);
      var b = fakeWorld.createChunk(pos);

      a.getOrCreateChunkNoiseSampler(fakeWorld::createSampler);
      b.getOrCreateChunkNoiseSampler(fakeWorld::createSampler);

      Worldgen.vanillaNoise(fakeWorld, a);
      Worldgen.optimizedNoise(fakeWorld, b);

      Worldgen.vanillaBiomes(fakeWorld, a);
      Worldgen.optimizedBiomes(fakeWorld, b);

      TestWorld.matches(a, b);
    }
    FastNoiseMod.LOGGER.info(name + " matches");
  }

  private static void runFor(int warmupSeconds, int benchmarkSeconds, String outputPrefix) {
    var options =
        new OptionsBuilder()
            .forks(0)
            .mode(Mode.AverageTime)
            .warmupTime(TimeValue.seconds(warmupSeconds))
            .measurementTime(TimeValue.seconds(benchmarkSeconds))
            .timeUnit(TimeUnit.MILLISECONDS);

    var benchmarkName = "";

    if (System.getProperty("zperfbenchmark") != null) {
      var benchRegex = System.getProperty("zperfbenchmark");
      options = options.include(benchRegex);
      benchmarkName = benchRegex;
    }

    if (System.getProperty("zworldname") != null) {
      var worldName = System.getProperty("zworldname");
      options = options.param("worldName", worldName.split(","));
      if (worldName != "") benchmarkName += " ";
      benchmarkName += worldName;
    }

    if (benchmarkName == "") benchmarkName = "All";

    options =
        options.result(outputPrefix + benchmarkName + ".txt").resultFormat(ResultFormatType.TEXT);

    if (System.getProperty("zuseasync") != null) {
      options =
          options.addProfiler(
              "async",
              "libPath=" + System.getProperty("zuseasync") + ";sig=true;output=flamegraph");
    }

    var runner = new Runner(options.build());
    try {
      runner.run();
    } catch (RunnerException exception) {
      FastNoiseMod.LOGGER.info("Cannot run jmh: {}", exception.getMessage());
    }
  }
}
