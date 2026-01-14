package org.codeberg.zenxarch.fastnoise.test;

import java.util.Random;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.ParameterRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParameterRangeTest {
  public long getDistanceVanilla(ParameterRange range, long noise) {
    long l = noise - range.max();
    long m = range.min() - noise;
    return l > 0L ? l : Math.max(m, 0L);
  }

  public long getDistanceVanilla(ParameterRange self, ParameterRange other) {
    long l = other.min() - self.max();
    long m = self.min() - other.max();
    return l > 0L ? l : Math.max(m, 0L);
  }

  private long randomRangeValue(Random random) {
    return random.nextLong(40000L) - 20000L;
  }

  private ParameterRange randomRange(Random random) {
    var min = randomRangeValue(random);
    return new ParameterRange(min, min + random.nextLong(20001L - min));
  }

  @Test
  public void testNoiseDistance() {
    var random = new Random();
    for (int i = 0; i < 64; i++) {
      var range = randomRange(random);
      var other = randomRangeValue(random);
      Assertions.assertEquals(range.getDistance(other), getDistanceVanilla(range, other));
    }
  }

  @Test
  public void testRangeDistance() {
    var random = new Random();
    for (int i = 0; i < 64; i++) {
      var range = randomRange(random);
      var other = randomRange(random);
      Assertions.assertEquals(range.getDistance(other), getDistanceVanilla(range, other));
    }
  }
}
