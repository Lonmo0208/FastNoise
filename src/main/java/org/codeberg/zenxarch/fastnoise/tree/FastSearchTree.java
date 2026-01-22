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
    public LeafNode getClosestNode(int[] noise, LeafNode alternative, long distance) {
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
    public long getDistance(int[] noise) {
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
    public LeafNode getClosestNode(int[] noise, LeafNode alternative, long distance) {
      return this;
    }

    @Override
    public long getDistance(int[] noise) {
      return params.getSquaredDistance(noise);
    }
  }

  private static sealed interface Node permits BranchNode, LeafNode {
    public LeafNode getClosestNode(int[] noise, LeafNode alternative, long distance);

    public long getDistance(int[] noise);
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

    private final int[] min;
    private final int[] max;
    private final long sqOffset;

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
