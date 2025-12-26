package org.codeberg.zenxarch.fastnoise.noise;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FastPackedIntegerArrayTest {
  void testSet(int maxElement) {
    int bits = Math.max(MathHelper.ceilLog2(maxElement), 1);
    var random = new Random();
    var array = new ArrayList<Integer>();
    var packedArray = new PackedIntegerArray(bits, 4096);

    for (int i = 0; i < 4096; i++) {
      var value = random.nextInt(maxElement);
      array.add(value);
      ((FastPackedIntegerArray) packedArray).zenxarch$unsafeSet(i, value);
    }

    for (int i = 0; i < 4096; i++) {
      Assertions.assertEquals(array.get(i), packedArray.get(i));
    }
  }

  @Test
  void testSet() {
    testSet(1);
    testSet(2);
    testSet(3);

    testSet(5);
    testSet(17);
    testSet(330);
  }
}
