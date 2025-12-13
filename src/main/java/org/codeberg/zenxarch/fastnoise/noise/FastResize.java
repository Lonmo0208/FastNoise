package org.codeberg.zenxarch.fastnoise.noise;

public final class FastResize {
    
  // fast expand bits using mask
  public static void fastResize1to2bits(long[] small, long[] large) {
    // is 0b010101....
    long mask = 0x5555_5555_5555_5555L;

    for (int i = 0; i < small.length; i++) {
      large[(i << 1)] = Long.expand(small[i], mask);
      large[(i << 1) | 1] = Long.expand(small[i] >> 32, mask);
    }
  }
}
