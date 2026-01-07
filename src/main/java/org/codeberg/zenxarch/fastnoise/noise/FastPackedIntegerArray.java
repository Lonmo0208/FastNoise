package org.codeberg.zenxarch.fastnoise.noise;

public interface FastPackedIntegerArray {
  /**
   * @param index Array index
   * @param value value to put in array
   */
  public void zenxarch$unsafeSet(int index, int value);

  /**
   * @param index Array index
   */
  public int zenxarch$unsafeGet(int index);

  public default void zenxarch$copy(FastPackedIntegerArray from, int upto) {
    for (int i = 0; i < upto; i++) this.zenxarch$unsafeSet(i, from.zenxarch$unsafeGet(i));
  }

  public default void zenxarch$copy(FastPackedIntegerArray from, int upto, int[] mapping) {
    for (int i = 0; i < upto; i++) this.zenxarch$unsafeSet(i, mapping[from.zenxarch$unsafeGet(i)]);
  }
}
