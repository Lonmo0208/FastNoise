package org.codeberg.zenxarch.fastnoise.noise;

public final class FastResize {
  private static long expand(long in) {
    long result = 0x0l;
    for (int i = 0; i < 32; i++) {
      result |= (in & 0x1) << (i << 1);
      in = in >>> 1;
    }
    return result;
  }

  public static void fastResize1to2bits(long[] small, long[] large) {
    for (int i = 0; i < small.length; i++) {
      large[i + i] = expand(small[i]);
      large[i + i + 1] = expand(small[i] >>> 32);
    }
  }
}
