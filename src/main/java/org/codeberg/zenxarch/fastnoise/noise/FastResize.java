package org.codeberg.zenxarch.fastnoise.noise;

public final class FastResize {
  private static long expand2bit(long in) {
    return in + (in & 0x2);
  }

  private static long expand(long in) {
    long result = 0x0l;
    for (int i = 0; i < 16; i++) {
      result |= expand2bit((in >>> i >>> i) & 0x3) << (4 * i);
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
