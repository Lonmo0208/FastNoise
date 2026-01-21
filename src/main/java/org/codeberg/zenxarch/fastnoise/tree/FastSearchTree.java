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
    var params = new ParameterRange[6];
    params[0] = getParameter(ix[0]);
    params[1] = getParameter(ix[1]);
    params[2] = getParameter(ix[2]);
    params[3] = getParameter(ix[3]);
    params[4] = getParameter(ix[4]);
    params[5] = getParameter(ix[5]);

    var sqOffset = MathHelper.square(ix[6].getDistance(0));
    return new Parameters(params, sqOffset);
  }

  private static final class Parameters {

    private final ParameterRange[] params;
    private final long sqOffset;

    public Parameters(ParameterRange[] params, long sqOffset) {
      if (params.length != 6) {
        throw new IllegalStateException("Params must be 6 in length");
      }
      this.params = params;
      this.sqOffset = sqOffset;
    }

    public long getSquaredDistance(long[] noise) {
      var m0 = noise[0] - params[0].max;
      var m1 = noise[1] - params[1].max;
      var m2 = noise[2] - params[2].max;
      var m3 = noise[3] - params[3].max;
      var m4 = noise[4] - params[4].max;
      var m5 = noise[5] - params[5].max;

      if (m0 < 0) m0 = params[0].min - noise[0];
      if (m1 < 0) m1 = params[1].min - noise[1];
      if (m2 < 0) m2 = params[2].min - noise[2];
      if (m3 < 0) m3 = params[3].min - noise[3];
      if (m4 < 0) m4 = params[4].min - noise[4];
      if (m5 < 0) m5 = params[5].min - noise[5];

      if (m0 < 0) m0 = 0;
      if (m1 < 0) m1 = 0;
      if (m2 < 0) m2 = 0;
      if (m3 < 0) m3 = 0;
      if (m4 < 0) m4 = 0;
      if (m5 < 0) m5 = 0;

      m0 *= m0;
      m1 *= m1;
      m2 *= m2;
      m3 *= m3;
      m4 *= m4;
      m5 *= m5;

      return m0 + m1 + m2 + m3 + m4 + m5 + sqOffset;
    }
  }

  private static ParameterRange getParameter(MultiNoiseUtil.ParameterRange range) {
    return new ParameterRange(range.min, range.max);
  }

  private static final class ParameterRange {
    public final long min;
    public final long max;

    public ParameterRange(long min, long max) {
      this.min = min;
      this.max = max;
    }
  }
}
