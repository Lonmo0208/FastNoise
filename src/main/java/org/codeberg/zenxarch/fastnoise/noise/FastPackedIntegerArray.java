package org.codeberg.zenxarch.fastnoise.noise;

public interface FastPackedIntegerArray {
  /**
   * Assumes the index is empty
   *
   * @param index Array index
   * @param value value to put in array
   */
  public void zenxarch$unsafeSet(int index, int value);
}
