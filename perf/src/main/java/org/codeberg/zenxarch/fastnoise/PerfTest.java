package org.codeberg.zenxarch.fastnoise;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

public class PerfTest {
  public static void runTest(DynamicRegistryManager manager) {
    Benchmark.setManager(manager);

    var centeredRegion = BenchmarkSettings.ChunkRegion.of(-64 / 16, 64 / 16);
    var offsetRegion = BenchmarkSettings.ChunkRegion.of((2048 - 64) / 16, 2048 / 16);

    var overworldVanilla =
        BenchmarkSettings.vanilla(ChunkGeneratorSettings.OVERWORLD, 0, centeredRegion);
    var overworldOptimized =
        BenchmarkSettings.optimized(ChunkGeneratorSettings.OVERWORLD, 0, centeredRegion);

    var netherdVanilla =
        BenchmarkSettings.vanilla(ChunkGeneratorSettings.NETHER, 0, centeredRegion);
    var netherdOptimized =
        BenchmarkSettings.optimized(ChunkGeneratorSettings.NETHER, 0, centeredRegion);

    var endVanilla = BenchmarkSettings.vanilla(ChunkGeneratorSettings.END, 0, offsetRegion);
    var endOptimized = BenchmarkSettings.optimized(ChunkGeneratorSettings.END, 0, offsetRegion);

    while (true) {
      Benchmark.benchmark("Overworld Vanilla", overworldVanilla);
      Benchmark.benchmark("Overworld Optimized", overworldOptimized);

      Benchmark.benchmark("Nether Vanilla", netherdVanilla);
      Benchmark.benchmark("Nether Optimized", netherdOptimized);

      Benchmark.benchmark("End Vanilla", endVanilla);
      Benchmark.benchmark("End Optimized", endOptimized);
    }
  }
}
