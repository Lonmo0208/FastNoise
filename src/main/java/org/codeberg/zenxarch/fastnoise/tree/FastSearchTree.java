package org.codeberg.zenxarch.fastnoise.tree;

import java.util.ArrayList;
import java.util.stream.Stream;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeBranchNode;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeLeafNode;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeNode;
import org.apache.commons.lang3.mutable.MutableObject;

public final class FastSearchTree<T> {
  private Node rootNode;
  private ThreadLocal<LeafNode> lastResult = new ThreadLocal<>();
  private T[] values;

  public FastSearchTree(SearchTree<T> tree) {
    this(tree.firstNode);
  }

  @SuppressWarnings("unchecked")
  public FastSearchTree(TreeNode<T> node) {
    var values = new ArrayList<T>();
    this.rootNode = fromTreeNode(node, values);
    this.values = (T[]) values.toArray();
  }

  public T search(MultiNoiseUtil.NoiseValuePoint point) {
    var lastResult = new MutableObject<>(this.lastResult.get());
    var result = this.search(point, lastResult);
    this.lastResult.set(lastResult.get());
    return result;
  }

  public T search(MultiNoiseUtil.NoiseValuePoint point, MutableObject<LeafNode> lastResult) {
    var noise =
        new int[] {
          (int) point.temperatureNoise(),
          (int) point.humidityNoise(),
          (int) point.continentalnessNoise(),
          (int) point.erosionNoise(),
          (int) point.depth(),
          (int) point.weirdnessNoise()
        };
    var lastDistance =
        lastResult.get() == null
            ? Long.MAX_VALUE
            : lastResult.get().params.getSquaredDistance(noise);
    var leaf = this.rootNode.getClosestNode(noise, lastResult.get(), lastDistance);
    lastResult.setValue(leaf.node);
    return this.values[leaf.node.value];
  }

  private static <T> Node fromTreeNode(TreeNode<T> node, ArrayList<T> values) {
    return switch (node) {
      case TreeBranchNode<T> nodex -> fromTreeBranchNode(nodex, values);
      case TreeLeafNode<T> nodex -> fromTreeLeafNode(nodex, values);
      default ->
          throw new IllegalStateException(
              "Unknown node type with class: " + node.getClass().descriptorString());
    };
  }

  private static <T> BranchNode fromTreeBranchNode(TreeBranchNode<T> node, ArrayList<T> values) {
    var params =
        Stream.of(node.subTree).map(nodex -> getParameters(nodex)).toArray(Parameters[]::new);
    var nodes =
        Stream.of(node.subTree).map(nodex -> fromTreeNode(nodex, values)).toArray(Node[]::new);
    return new BranchNode(params, nodes);
  }

  private static <T> LeafNode fromTreeLeafNode(TreeLeafNode<T> node, ArrayList<T> values) {
    var params = getParameters(node);
    var value = values.indexOf(node.value);
    if (value == -1) {
      value = values.size();
      values.add(node.value);
    }
    return new LeafNode(value, params);
  }

  private static final class BranchNode implements Node {
    private final Node[] nodes;
    private final int[] min;
    private final int[] max;
    private final long[] sqOffset;

    public BranchNode(Parameters[] params, Node[] nodes) {
      this.nodes = nodes;
      this.min = new int[this.nodes.length * 6];
      this.max = new int[this.nodes.length * 6];
      this.sqOffset = new long[this.nodes.length];

      for (int i = 0; i < this.nodes.length; i++) {
        System.arraycopy(params[i].min, 0, this.min, i * 6, 6);
        System.arraycopy(params[i].max, 0, this.max, i * 6, 6);
        this.sqOffset[i] = params[i].sqOffset;
      }
    }

    private static void repeat(int[] src, int[] dest, int length, int stride) {
      for (int i = 0; i < length; i += stride) System.arraycopy(src, 0, dest, i, stride);
    }

    private static void subtract(int[] a, int[] b, int[] dest, int length) {
      for (int i = 0; i < length; i++) {
        dest[i] = a[i] - b[i];
      }
    }

    private static void max(int[] a, int[] b, int[] dest, int length) {
      for (int i = 0; i < length; i++) {
        dest[i] = a[i] > b[i] ? a[i] : b[i];
      }
    }

    private static void max(int[] a, int b, int[] dest, int length) {
      for (int i = 0; i < length; i++) {
        dest[i] = a[i] > b ? a[i] : b;
      }
    }

    private static long sqSum(int[] src, int start, int end) {
      long result = 0L;
      for (int i = start; i < end; i++) {
        result += (long) src[i] * (long) src[i];
      }
      return result;
    }

    @Override
    public SearchResult getClosestNode(int[] noise, LeafNode alternative, long distance) {
      long minDist = distance;
      LeafNode result = alternative;

      long[] distances = new long[this.nodes.length];
      int[] mins = new int[this.min.length];
      int[] maxs = new int[this.max.length];

      repeat(noise, mins, mins.length, 6);
      repeat(noise, maxs, maxs.length, 6);

      subtract(this.min, mins, mins, this.min.length);
      subtract(maxs, this.max, maxs, this.max.length);

      max(mins, maxs, mins, mins.length);
      max(mins, 0, mins, mins.length);

      int distanceIdx = 0;
      for (int i = 0; i < distances.length; i++) {
        distances[i] = sqSum(mins, i * 6, i * 6 + i) + sqOffset[distanceIdx];
      }

      for (int i = 0; i < distances.length; i++) {
        var nextDist = distances[i];
        if (nextDist < minDist) {
          switch (nodes[i]) {
            case BranchNode nodex -> {
              var searchResult = nodex.getClosestNode(noise, alternative, minDist);
              if (searchResult.distance < minDist) {
                minDist = searchResult.distance;
                result = searchResult.node;
              }
            }
            case LeafNode nodex -> {
              minDist = nextDist;
              result = nodex;
            }
          }
          ;
        }
      }

      return new SearchResult(minDist, result);
    }
  }

  public static final class LeafNode implements Node {
    public final int value;
    public final Parameters params;

    public LeafNode(int value, Parameters params) {
      this.value = value;
      this.params = params;
    }

    @Override
    public SearchResult getClosestNode(int[] noise, LeafNode alternative, long distance) {
      return null;
    }
  }

  private static final class SearchResult {
    public final long distance;
    public final LeafNode node;

    public SearchResult(long distance, LeafNode node) {
      this.distance = distance;
      this.node = node;
    }
  }

  private static sealed interface Node permits BranchNode, LeafNode {
    public SearchResult getClosestNode(int[] noise, LeafNode alternative, long distance);
  }

  private static Parameters getParameters(MultiNoiseUtil.SearchTree.TreeNode<?> node) {
    var ix = node.parameters;
    var min = new int[6];
    var max = new int[6];
    for (int i = 0; i < 6; i++) min[i] = (int) ix[i].min;
    for (int i = 0; i < 6; i++) max[i] = (int) ix[i].max;

    var sqOffset = MathHelper.square(ix[6].getDistance(0));
    return new Parameters(min, max, sqOffset);
  }

  private static final class Parameters {

    public final int[] min;
    public final int[] max;
    public final long sqOffset;

    public Parameters(int[] min, int[] max, long sqOffset) {
      if (min.length != 6 || max.length != 6) {
        throw new IllegalStateException("Params must be 6 in length");
      }
      this.min = min;
      this.max = max;
      this.sqOffset = sqOffset;
    }

    public long getSquaredDistance(int[] noise) {
      var values = new int[6];
      for (int i = 0; i < 6; i++)
        values[i] = Math.max(Math.max(noise[i] - this.max[i], this.min[i] - noise[i]), 0);

      long result = 0L;
      for (int i = 0; i < 6; i++) {
        result += (long) values[i] * (long) values[i];
      }

      return result + sqOffset;
    }
  }
}
