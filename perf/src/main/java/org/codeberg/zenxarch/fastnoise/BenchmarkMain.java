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

    runBenchmark("Benchmark ");
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

  private static int getIntProperty(String name, int def, int min) {
    var value = System.getProperty(name);
    if (value == null) return def;
    try {
      return Math.max(Integer.parseInt(value), min);
    } catch (Exception e) {
      return def;
    }
  }

  private static void runBenchmark(String outputPrefix) {
    var options = new OptionsBuilder().forks(0);

    try {
      options = options.mode(Mode.deepValueOf(System.getProperty("zbenchmode")));
    } catch (Exception e) {
      options = options.mode(Mode.AverageTime);
    }

    options =
        options
            .warmupTime(TimeValue.seconds(getIntProperty("zwarmuptime", 5, 1)))
            .measurementTime(TimeValue.seconds(getIntProperty("zmeasuretime", 5, 1)))
            .warmupIterations(getIntProperty("zwarmups", 5, 1))
            .measurementIterations(getIntProperty("zmeasures", 5, 1))
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
