package org.opentechfin.workflow.acyclicgraph;


import java.util.Objects;

/**
 * GraphEdge is what will be persisted in database.
 * <p> GraphEdge has limited immutability, it can only guarantee two nodes on both sides of edge do not change. Can not
 * guarantee topology beyond the edge does not change.
 */
public class GraphEdge implements Comparable<GraphEdge> {

  private final GraphNode fromNode;

  private final GraphNode toNode;

  public GraphEdge(GraphNode fromNode, GraphNode toNode) {
    this.fromNode = fromNode;
    this.toNode = toNode;
  }

  public GraphNode getFromNode() {
    return fromNode;
  }

  public GraphNode getToNode() {
    return toNode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof GraphEdge)) {
      return false;
    }

    GraphEdge graphEdge = (GraphEdge) o;

    if (!fromNode.equals(graphEdge.fromNode)) {
      return false;
    }
    return toNode.equals(graphEdge.toNode);
  }

  @Override
  public int hashCode() {
    int result = fromNode.hashCode();
    result = 31 * result + toNode.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("edge: %10s -> %10s.", fromNode.getGraphNodeId(), toNode.getGraphNodeId());
  }

  @Override
  public int compareTo(GraphEdge o) {
    Objects.requireNonNull(o, "compareTo does not accept null argument");
    int res = fromNode.getGraphNodeId().compareTo(o.getFromNode().getGraphNodeId());
    if (res != 0) {
      return res;
    }
    return toNode.getGraphNodeId().compareTo(o.toNode.getGraphNodeId());
  }
}
