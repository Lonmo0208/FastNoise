package org.codeberg.zenxarch.fastnoise.tree;

import java.util.ArrayList;
import java.util.stream.Stream;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeBranchNode;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeLeafNode;
import net.minecraft.world.biome.source.util.MultiNoiseUtil.SearchTree.TreeNode;

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
    var lastResult = this.lastResult.get();
    var noise =
        new long[] {
          point.temperatureNoise(),
          point.humidityNoise(),
          point.continentalnessNoise(),
          point.erosionNoise(),
          point.depth(),
          point.weirdnessNoise()
        };
    var lastDistance = lastResult == null ? Long.MAX_VALUE : lastResult.getDistance(noise);
    var leaf = this.rootNode.getClosestNode(noise, lastResult, lastDistance);
    this.lastResult.set(leaf);
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

  private static final class LeafNode implements Node {
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
    var params = new Parameter[6];
    params[0] = getParameter(ix[0]);
    params[1] = getParameter(ix[1]);
    params[2] = getParameter(ix[2]);
    params[3] = getParameter(ix[3]);
    params[4] = getParameter(ix[4]);
    params[5] = getParameter(ix[5]);

    var sqOffset = MathHelper.square(ix[6].getDistance(0));

    if (sqOffset == 0) {
      return new NoOffsetParameters(params);
    }
    return new OffsetParameters(params, sqOffset);
  }

  private static final class NoOffsetParameters implements Parameters {
    private final Parameter[] params;

    public NoOffsetParameters(Parameter[] params) {
      if (params.length != 6) {
        throw new IllegalStateException("Params must be 6 in length");
      }
      this.params = params;
    }

    @Override
    public long getSquaredDistance(long[] noise) {
      return params[0].getSquaredDistance(noise[0])
          + params[1].getSquaredDistance(noise[1])
          + params[2].getSquaredDistance(noise[2])
          + params[3].getSquaredDistance(noise[3])
          + params[4].getSquaredDistance(noise[4])
          + params[5].getSquaredDistance(noise[5]);
    }
  }

  private static final class OffsetParameters implements Parameters {

    private final Parameter[] params;
    private final long sqOffset;

    public OffsetParameters(Parameter[] params, long sqOffset) {
      if (params.length != 6) {
        throw new IllegalStateException("Params must be 6 in length");
      }
      this.params = params;
      this.sqOffset = sqOffset;
    }

    @Override
    public long getSquaredDistance(long[] noise) {
      return params[0].getSquaredDistance(noise[0])
          + params[1].getSquaredDistance(noise[1])
          + params[2].getSquaredDistance(noise[2])
          + params[3].getSquaredDistance(noise[3])
          + params[4].getSquaredDistance(noise[4])
          + params[5].getSquaredDistance(noise[5])
          + sqOffset;
    }
  }

  private static sealed interface Parameters permits NoOffsetParameters, OffsetParameters {
    public long getSquaredDistance(long[] noise);
  }

  private static Parameter getParameter(MultiNoiseUtil.ParameterRange range) {
    if (range.min == range.max) return new ParameterValue(range.min);
    return new ParameterRange(range.min, range.max);
  }

  private static final class ParameterRange implements Parameter {
    public final long min;
    public final long max;

    public ParameterRange(long min, long max) {
      this.min = min;
      this.max = max;
    }

    @Override
    public long getSquaredDistance(long noise) {
      if (noise > max) return square(noise - max);
      if (noise < min) return square(noise - min);
      return 0L;
    }
  }

  private static final class ParameterValue implements Parameter {
    public final long value;

    public ParameterValue(long value) {
      this.value = value;
    }

    @Override
    public long getSquaredDistance(long noise) {
      return square(noise - value);
    }
  }

  private static sealed interface Parameter permits ParameterValue, ParameterRange {
    public long getSquaredDistance(long noise);

    default long square(long v) {
      return v * v;
    }
  }
}
