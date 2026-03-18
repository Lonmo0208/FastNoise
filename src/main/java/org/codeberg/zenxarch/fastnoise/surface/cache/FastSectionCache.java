package org.codeberg.zenxarch.fastnoise.surface.cache;

import it.unimi.dsi.fastutil.shorts.ShortShortPair;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.ChunkSection;

public final class FastSectionCache {
  /*
    DEFAULT + !SOLID = AIR 1 0
    !DEFAULT + !SOLID = WATER 0 0
    DEFAULT + SOLID = STONE 1 1
    !DEFAULT + SOLID = ORE 0 1
  */
  private final short[] IS_SOLID = new short[256];
  private final short[] IS_DEFAULT = new short[256];

  private static int index(int x, int z) {
    return x + z << 4;
  }

  public FastSectionCache(ChunkSection section, BlockState defaultState) {
    for (int z = 0; z < 16; z++) {
      for (int x = 0; x < 16; x++) {
        var index = index(x, z);
        var pair = fromColumn(section, defaultState, x, z);
        IS_SOLID[index] = pair.leftShort();
        IS_DEFAULT[index] = pair.rightShort();
      }
    }
  }

  private ShortShortPair fromColumn(ChunkSection section, BlockState defaultState, int x, int z) {
    short isSolid = 0x0, isDefault = 0x0;

    for (int y = 0; y < 16; y++) {
      var state = section.getBlockState(x, y, z);
      if (state.isAir()) isDefault = setBit(isDefault, y);
      else if (!state.getFluidState().isEmpty()) {
      } else {
        isSolid = setBit(isSolid, y);
        if (state == defaultState) isDefault = setBit(isDefault, y);
      }
    }

    return ShortShortPair.of(isSolid, isDefault);
  }

  private short setBit(short in, int bitIdx) {
    return (short) (in | (0x1 << bitIdx));
  }

  private boolean getBit(short in, int bitIdx) {
    return ((in >> bitIdx) & 0x1) != 0x0;
  }

  public STATE getState(int x, int y, int z) {
    var index = index(x, z);
    if (getBit(IS_SOLID[index], y)) {
      return getBit(IS_DEFAULT[index], y) ? STATE.STONE : STATE.ORE;
    } else {
      return getBit(IS_DEFAULT[index], y) ? STATE.AIR : STATE.WATER;
    }
  }

  public static enum STATE {
    AIR,
    WATER,
    STONE,
    ORE
  }
}
