package tech.picnic.errorprone.refaster.runner;

import static java.util.Comparator.comparingInt;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A node in an immutable tree.
 *
 * <p>The tree's edges are string-labeled, while its leaves store values of type {@code T}.
 */
@AutoValue
abstract class Node<T> {
  static <T> Node<T> create(
      List<T> values, Function<? super T, ? extends Set<? extends Set<String>>> pathExtractor) {
    BuildNode<T> tree = BuildNode.create();
    tree.register(values, pathExtractor);
    return tree.immutable();
  }

  abstract ImmutableMap<String, Node<T>> children();

  abstract ImmutableList<T> values();

  // XXX: Consider having `RefasterRuleSelector` already collect the candidate edges into a
  // `SortedSet`, as that would likely speed up `ImmutableSortedSet#copyOf`.
  void collectReachableValues(Set<String> candidateEdges, Consumer<T> sink) {
    collectReachableValues(ImmutableSortedSet.copyOf(candidateEdges).asList(), sink);
  }

  private void collectReachableValues(ImmutableList<String> candidateEdges, Consumer<T> sink) {
    values().forEach(sink);

    if (candidateEdges.isEmpty() || children().isEmpty()) {
      return;
    }

    // For performance reasons we iterate over the smallest set of edges. In case there are fewer
    // children than candidate edges we iterate over the former, at the cost of not pruning the
    // set of candidate edges if a transition is made.
    int candidateEdgeCount = candidateEdges.size();
    if (children().size() < candidateEdgeCount) {
      for (Map.Entry<String, Node<T>> e : children().entrySet()) {
        if (candidateEdges.contains(e.getKey())) {
          e.getValue().collectReachableValues(candidateEdges, sink);
        }
      }
    } else {
      for (int i = 0; i < candidateEdgeCount; i++) {
        Node<T> child = children().get(candidateEdges.get(i));
        if (child != null) {
          child.collectReachableValues(candidateEdges.subList(i + 1, candidateEdgeCount), sink);
        }
      }
    }
  }

  @AutoValue
  @SuppressWarnings("AutoValueImmutableFields" /* Type is used only during `Node` construction. */)
  abstract static class BuildNode<T> {
    private static <T> BuildNode<T> create() {
      return new AutoValue_Node_BuildNode<>(new HashMap<>(), new ArrayList<>());
    }

    abstract Map<String, BuildNode<T>> children();

    abstract List<T> values();

    /**
     * Registers all paths to each of the given values.
     *
     * <p>Shorter paths are registered first, so that longer paths can be skipped if a strict prefix
     * leads to the same value.
     */
    private void register(
        List<T> values, Function<? super T, ? extends Set<? extends Set<String>>> pathsExtractor) {
      for (T value : values) {
        pathsExtractor.apply(value).stream()
            .sorted(comparingInt(Set::size))
            .forEach(path -> registerPath(value, ImmutableList.sortedCopyOf(path)));
      }
    }

    private void registerPath(T value, ImmutableList<String> path) {
      if (values().contains(value)) {
        /* Another (shorter) path already leads to this value. */
        return;
      }

      path.stream()
          .findFirst()
          .ifPresentOrElse(
              edge ->
                  children()
                      .computeIfAbsent(edge, k -> create())
                      .registerPath(value, path.subList(1, path.size())),
              () -> values().add(value));
    }

    private Node<T> immutable() {
      return new AutoValue_Node<>(
          ImmutableMap.copyOf(Maps.transformValues(children(), BuildNode::immutable)),
          ImmutableList.copyOf(values()));
    }
  }
}
