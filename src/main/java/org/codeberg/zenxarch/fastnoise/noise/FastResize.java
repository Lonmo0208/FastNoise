package org.codeberg.zenxarch.fastnoise.noise;

public final class FastResize {
  private static long expand16bit(long in) {
    var a =
        ((in & 0b0000_0101_0000_0101l) * 0b0000_0101_0000_0101l)
            & Long.expand(0b0000_0101_0000_0101l, 0x5555555555555555l);
    var b =
        ((in & 0b0000_1010_0000_1010l) * 0b0000_1010_0000_1010l)
            & Long.expand(0b0000_1010_0000_1010l, 0x5555555555555555l);
    var c =
        ((in & 0b0101_0000_0101_0000l) * 0b0101_0000_0101_0000l)
            & Long.expand(0b0101_0000_0101_0000l, 0x5555555555555555l);
    var d =
        ((in & 0b1010_0000_1010_0000l) * 0b1010_0000_1010_0000l)
            & Long.expand(0b1010_0000_1010_0000l, 0x5555555555555555l);
    return a | b | c | d;
  }

  private static long expand(long in) {
    return expand16bit(in & 0xFFFF) | (expand16bit(in >>> 16) << 32);
  }

  public static void fastResize1to2bits(long[] small, long[] large) {
    for (int i = 0; i < small.length; i++) {
      large[i + i] = expand(small[i]);
      large[i + i + 1] = expand(small[i] >>> 32);
    }
  }
}
