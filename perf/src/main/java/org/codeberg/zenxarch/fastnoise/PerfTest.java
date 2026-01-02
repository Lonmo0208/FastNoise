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

    runFor(5, 5, "Cold");
    runFor(20, 60, "Hot");
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
