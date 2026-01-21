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
        new long[] {
          point.temperatureNoise(),
          point.humidityNoise(),
          point.continentalnessNoise(),
          point.erosionNoise(),
          point.depth(),
          point.weirdnessNoise()
        };
    var lastDistance =
        lastResult.get() == null ? Long.MAX_VALUE : lastResult.get().getDistance(noise);
    var leaf = this.rootNode.getClosestNode(noise, lastResult.get(), lastDistance);
    lastResult.setValue(leaf);
    return this.values[leaf.value];
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
    var params = getParameters(node);
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
    private final Parameters params;

    public BranchNode(Parameters params, Node[] nodes) {
      this.nodes = nodes;
      this.params = params;
    }

    @Override
    public LeafNode getClosestNode(long[] noise, LeafNode alternative, long distance) {
      long minDist = distance;
      LeafNode result = alternative;

      for (var node : nodes) {
        var nextDist = node.getDistance(noise);
        if (nextDist < minDist) {
          var leafNode = node.getClosestNode(noise, result, minDist);
          if (leafNode == node) {
            result = leafNode;
            minDist = nextDist;
          } else if ((nextDist = leafNode.getDistance(noise)) < minDist) {
            result = leafNode;
            minDist = nextDist;
          }
        }
      }

      return result;
    }

    @Override
    public long getDistance(long[] noise) {
      return params.getSquaredDistance(noise);
    }
  }

  public static final class LeafNode implements Node {
    private final Parameters params;
    public final int value;

    public LeafNode(int value, Parameters params) {
      this.params = params;
      this.value = value;
    }

    @Override
    public LeafNode getClosestNode(long[] noise, LeafNode alternative, long distance) {
      return this;
    }

    @Override
    public long getDistance(long[] noise) {
      return params.getSquaredDistance(noise);
    }
  }

  private static sealed interface Node permits BranchNode, LeafNode {
    public LeafNode getClosestNode(long[] noise, LeafNode alternative, long distance);

    public long getDistance(long[] noise);
  }

  private static Parameters getParameters(MultiNoiseUtil.SearchTree.TreeNode<?> node) {
    var ix = node.parameters;
    var min = new long[] {ix[0].min, ix[1].min, ix[2].min, ix[3].min, ix[4].min, ix[5].min};
    var max = new long[] {ix[0].max, ix[1].max, ix[2].max, ix[3].max, ix[4].max, ix[5].max};

    var sqOffset = MathHelper.square(ix[6].getDistance(0));
    return new Parameters(min, max, sqOffset);
  }

  private static final class Parameters {

    private final long[] min;
    private final long[] max;
    private final long sqOffset;

    public Parameters(long[] min, long[] max, long sqOffset) {
      if (min.length != 6 || max.length != 6) {
        throw new IllegalStateException("Params must be 6 in length");
      }
      this.min = min;
      this.max = max;
      this.sqOffset = sqOffset;
    }

    public long getSquaredDistance(long[] noise) {
      long m0 = Math.max(Math.max(noise[0] - this.max[0], this.min[0] - noise[0]), 0);
      long m1 = Math.max(Math.max(noise[1] - this.max[1], this.min[1] - noise[1]), 0);
      long m2 = Math.max(Math.max(noise[2] - this.max[2], this.min[2] - noise[2]), 0);
      long m3 = Math.max(Math.max(noise[3] - this.max[3], this.min[3] - noise[3]), 0);
      long m4 = Math.max(Math.max(noise[4] - this.max[4], this.min[4] - noise[4]), 0);
      long m5 = Math.max(Math.max(noise[5] - this.max[5], this.min[5] - noise[5]), 0);

      m0 *= m0;
      m1 *= m1;
      m2 *= m2;
      m3 *= m3;
      m4 *= m4;
      m5 *= m5;

      return m0 + m1 + m2 + m3 + m4 + m5 + sqOffset;
    }
  }
}
