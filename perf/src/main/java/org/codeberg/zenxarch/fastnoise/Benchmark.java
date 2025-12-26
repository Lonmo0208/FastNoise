package org.codeberg.zenxarch.fastnoise;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.LongSummaryStatistics;
import java.util.stream.LongStream;
import net.minecraft.registry.DynamicRegistryManager;

public final class Benchmark {
  private static final Object2ObjectMap<BenchmarkSettings, TestWorld> WORLD_CACHE =
      new Object2ObjectOpenHashMap<>();
  private static final Object2IntMap<BenchmarkSettings> TIME = new Object2IntOpenHashMap<>();
  private static DynamicRegistryManager manager;

  private static final int staticWarmupTime = 2;
  private static final int initBenchmarkTime = 2;

  public static void setManager(DynamicRegistryManager manager) {
    Benchmark.manager = manager;
  }

  public static void benchmark(String name, BenchmarkSettings settings) {
    preetyPrint(name, settings);
    var world =
        WORLD_CACHE.computeIfAbsent(
            settings, settingsx -> new TestWorld(manager, (BenchmarkSettings) settingsx));
    TIME.put(settings, TIME.getOrDefault(settings, initBenchmarkTime / 2) * 2);
    var benchmarkTime = TIME.getInt(settings);

    var warmupState = benchmark(world, staticWarmupTime);
    preetyPrint(name + " Warmup", warmupState);
    var stats = benchmark(world, benchmarkTime);
    preetyPrint(name + " Benchmark", stats);
  }

  private static void preetyPrint(String name, BenchmarkSettings settings) {
    var logger = LogUtils.getLogger();

    logger.info(
        "{} with settings: {} seed: {} and region: {}",
        name,
        settings.settings().getValue(),
        settings.seed(),
        settings.region());
  }

  private static void preetyPrint(String name, LongSummaryStatistics stats) {
    var minTime = toMs(stats.getMin());
    var maxTime = toMs(stats.getMax());
    var avgTime = toMs((long) stats.getAverage());

    var logger = LogUtils.getLogger();

    logger.info("{} results: ", name);
    logger.info("Min {} | Max {} | Avg {} ", minTime, maxTime, avgTime);
  }

  private static double toMs(long nano) {
    return (double) nano / 1000_000.0;
  }

  private static LongSummaryStatistics benchmark(TestWorld fakeWorld, int time) {
    var stats = new LongSummaryStatistics();

    var startTime = System.nanoTime();
    while ((System.nanoTime() - startTime) / 1000_000_000 < time) {
      stats.combine(LongStream.of(fakeWorld.run()).summaryStatistics());
    }

    return stats;
  }
}
