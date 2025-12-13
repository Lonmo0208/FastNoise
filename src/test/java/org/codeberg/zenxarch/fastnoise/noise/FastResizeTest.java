package org.codeberg.zenxarch.fastnoise.noise;

import java.util.Random;
import net.minecraft.util.collection.PackedIntegerArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FastResizeTest {
  @BeforeAll
  static void init() {}

  @Test
  void testFastResize1to2bits() {
    var random = new Random();
    var smallArray = new PackedIntegerArray(1, 16 * 16 * 16);
    var largeArray = new PackedIntegerArray(2, 16 * 16 * 16);

    for (int x = 0; x < (16 * 16 * 16); x++) {
      smallArray.set(x, random.nextBoolean() ? 0 : 1);
    }

    FastResize.fastResize1to2bits(smallArray.getData(), largeArray.getData());

    for (int x = 0; x < (16 * 16 * 16); x++) {
      assert (smallArray.get(x) == largeArray.get(x));
    }
  }
}
