package com.leantaas.workflow.acyclicgraph;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
public class AcyclicGraph {

  protected Set<? extends GraphNode> nodes;

  protected AcyclicGraph() {

  }

  public AcyclicGraph(List<GraphEdge> edges) {
    if (!GraphUtil.isAcyclic(edges)) {
      throw new IllegalArgumentException("edgeList cannot form an acyclic graph");
    }
    this.nodes = new HashSet<>(GraphUtil.edgesToNodes(edges));
  }

  public Set<? extends GraphNode> getNodes() {
    return nodes;
  }

  public void setNodes(Set<GraphNode> nodes) {
    Objects.requireNonNull(nodes, "nodes should not be null");
    this.nodes = nodes;
  }

  public HashSet<? extends GraphNode> getEntryNode() {
    HashSet<GraphNode> res = new HashSet<>();
    for (GraphNode graphNode : nodes) {
      if (graphNode.getParentNodes().isEmpty()) {
        res.add(graphNode);
      }
    }
    return res;
  }

  public boolean isInGraph(GraphNode node) {
    return nodes.contains(node);
  }

  public boolean isReachableFromTo(GraphNode startingPoint, GraphNode endingPoint) {
    Objects.requireNonNull(startingPoint, "startingPoint node cannot be null");
    Objects.requireNonNull(endingPoint, "endingPoint node cannot be null");
    if (startingPoint.equals(endingPoint)) {
      return true;
    }
    // startingPoint is leaf or endingPoint cannot be reached by any node.
    if (startingPoint.getChildNodes().isEmpty() || endingPoint.getParentNodes().isEmpty()) {
      return false;
    }
    return dfs(startingPoint, new HashSet<>(), endingPoint);
  }

  @VisibleForTesting
  boolean dfs(GraphNode root, Set<GraphNode> visitedNodes, GraphNode target) {
    if (root.equals(target)) {
      return true;
    }
    if (!visitedNodes.add(root)) { // contains
      return false;
    } else {  // visitedNodes originally does not contain, now contains
      for (GraphNode child : root.getChildNodes()) {
        if (dfs(child, visitedNodes, target)) {
          return true;
        }
      }
      return false;
    }
  }
}
