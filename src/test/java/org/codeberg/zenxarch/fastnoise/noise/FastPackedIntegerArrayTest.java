package org.codeberg.zenxarch.fastnoise.noise;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.MathHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

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

  @RepeatedTest(5)
  void testSet() {
    testSet(1);
    testSet(2);
    testSet(3);

    testSet(5);
    testSet(17);
    testSet(330);
  }

  void testEqual(int maxElement) {
    int bits = Math.max(MathHelper.ceilLog2(maxElement), 1);
    var random = new Random();
    var packedArray = new PackedIntegerArray(bits, 4096);
    var against = new PackedIntegerArray(bits, 4096);

    for (int i = 0; i < 4096; i++) {
      var value = random.nextInt(maxElement);
      ((FastPackedIntegerArray) packedArray).zenxarch$unsafeSet(i, value);
      against.set(i, value);
    }

    Assertions.assertArrayEquals(against.getData(), packedArray.getData());
  }

  @RepeatedTest(5)
  void testEqual() {
    testEqual(1);
    testEqual(2);
    testEqual(3);

    testEqual(5);
    testEqual(17);
    testEqual(330);
  }

  void testGet(int maxElement) {
    int bits = Math.max(MathHelper.ceilLog2(maxElement), 1);
    var random = new Random();
    var packedArray = new PackedIntegerArray(bits, 4096);

    for (int i = 0; i < 4096; i++) {
      var value = random.nextInt(maxElement);
      ((FastPackedIntegerArray) packedArray).zenxarch$unsafeSet(i, value);
    }

    for (int i = 0; i < 4096; i++) {
      Assertions.assertEquals(
          packedArray.get(i), ((FastPackedIntegerArray) packedArray).zenxarch$unsafeGet(i));
    }
  }

  @RepeatedTest(5)
  void testGet() {
    testGet(1);
    testGet(2);
    testGet(3);

    testGet(5);
    testGet(17);
    testGet(330);
  }
}
