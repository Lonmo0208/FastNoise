package org.codeberg.zenxarch.fastnoise;

import java.util.concurrent.TimeUnit;
import net.minecraft.registry.DynamicRegistryManager;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

public class PerfTest {
  private static DynamicRegistryManager manager;

  public static void runTest(DynamicRegistryManager manager) {
    PerfTest.manager = manager;

    if (System.getProperty("zperfbenchmark").equals("parity")) {
      doParityTest();
      return;
    }

    runFor(5, 5, "Cold");
    runFor(20, 60, "Hot");
  }

  private static void doParityTest() {
    FastNoiseMod.LOGGER.info("Starting parity test");
    var vanilla = TestServer.vanillaDefault();
    var optimized = TestServer.optimizedDefault();

    FastNoiseMod.LOGGER.info("Generating Overworld");
    vanilla.overworld().noise();
    optimized.overworld().noise();
    assert (vanilla.overworld().equals(optimized.overworld()));
    FastNoiseMod.LOGGER.info("Overworld matches");

    FastNoiseMod.LOGGER.info("Generating Nether");
    vanilla.nether().noise();
    optimized.nether().noise();
    assert (vanilla.nether().equals(optimized.nether()));
    FastNoiseMod.LOGGER.info("Nether matches");

    FastNoiseMod.LOGGER.info("Generating End");
    vanilla.end().noise();
    optimized.end().noise();
    assert (vanilla.end().equals(optimized.end()));
    FastNoiseMod.LOGGER.info("End matches");
  }

  private static void runFor(int warmupSeconds, int benchmarkSeconds, String output) {
    var benchmarkName = System.getProperty("zperfbenchmark");
    var options =
        new OptionsBuilder()
            .forks(0)
            .mode(Mode.AverageTime)
            .warmupTime(TimeValue.seconds(warmupSeconds))
            .measurementTime(TimeValue.seconds(benchmarkSeconds))
            .timeUnit(TimeUnit.MILLISECONDS)
            .include(benchmarkName)
            .result(output + " " + benchmarkName + ".txt")
            .build();
    var runner = new Runner(options);
    try {
      runner.run();
    } catch (RunnerException exception) {

    }
  }

  public static DynamicRegistryManager getManager() {
    return manager;
  }
}
