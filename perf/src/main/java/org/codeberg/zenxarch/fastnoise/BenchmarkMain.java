package org.codeberg.zenxarch.fastnoise;

import java.util.concurrent.TimeUnit;
import net.minecraft.registry.DynamicRegistryManager;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.ForkedRunnerAccessor;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkMain {
  public static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkMain.class);

  public static void runTest(String[] args) {
    runBenchmark("Benchmark ");
  }

  public static boolean isParityTest() {
    return System.getProperty("zparity") != null;
  }

  public static boolean isForked() {
    return System.getProperty("zforked") != null;
  }

  public static void runForked(String[] args, DynamicRegistryManager manager) {
    TestGlobals.setManager(manager);

    try {
      ForkedRunnerAccessor.main(args);
    } catch (Exception e) {
      LOGGER.error("Huh?", e);
    }
  }

  public static void runParityTest(DynamicRegistryManager manager) {
    TestGlobals.setManager(manager);

    doParityTest();
  }

  private static void doParityTest() {
    LOGGER.info("Starting parity test");
    doParityTest(BenchmarkSettings.overworld());
    doParityTest(BenchmarkSettings.nether());
    doParityTest(BenchmarkSettings.end());
  }

  private static void doParityTest(BenchmarkSettings settings) {
    var fakeWorld = new TestWorld(settings);

    var name = settings.dimensionOptions().getValue().getPath();

    LOGGER.info("Generating " + name);
    for (var pos : settings.region().pos()) {
      var a = fakeWorld.createChunk(pos);
      var b = fakeWorld.createChunk(pos);

      a.getOrCreateChunkNoiseSampler(fakeWorld::createSampler);
      b.getOrCreateChunkNoiseSampler(fakeWorld::createSampler);

      // TODO: fix parity test
      // Worldgen.vanillaNoise(fakeWorld, a);
      // Worldgen.optimizedNoise(fakeWorld, b);
      //
      // Worldgen.vanillaBiomes(fakeWorld, a);
      // Worldgen.optimizedBiomes(fakeWorld, b);

      TestWorld.matches(a, b);
    }
    LOGGER.info(name + " matches");
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

  private static boolean getBooleanProperty(String name, boolean def) {
    var value = System.getProperty(name);
    if (value == null) return def;
    try {
      return Boolean.parseBoolean(value);
    } catch (Exception e) {
      return def;
    }
  }

  private static String jvmArgs(int forks, boolean zmod) {
    return "-Dzforked=true -Dzmod=" + zmod;
  }

  private static void runBenchmark(String outputPrefix) {
    ChainedOptionsBuilder options = new OptionsBuilder();
    var zmod = getBooleanProperty("zmod", true);

    {
      var forks = getIntProperty("zforks", 1, 1);
      options = options.forks(forks);
      var args = jvmArgs(forks, zmod);
      if (args != null) {
        options = options.jvmArgsAppend(args);
      }
    }

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
            .timeUnit(TimeUnit.MILLISECONDS)
            .threads(getIntProperty("zthreads", 1, 1));

    var benchmarkName = "Vanilla";

    if (zmod) benchmarkName = "Modded";

    if (System.getProperty("zperfbenchmark") != null) {
      var benchRegex = System.getProperty("zperfbenchmark");
      options = options.include(benchRegex);
      benchmarkName += " ";
      benchmarkName += benchRegex;
    }

    if (System.getProperty("zworldname") != null) {
      var worldName = System.getProperty("zworldname");
      options = options.param("worldName", worldName.split(","));
      benchmarkName += " ";
      benchmarkName += worldName;
    }

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
      LOGGER.info("Cannot run jmh: {}", exception.getMessage());
    }
  }
}
