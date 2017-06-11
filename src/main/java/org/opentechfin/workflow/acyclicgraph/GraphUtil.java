package org.opentechfin.workflow.acyclicgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * This class provide util, all static methods
 */
public class GraphUtil {

  private GraphUtil() {
    // disable initialization
  }

  /**
   * @param edges a list of graphEdge
   * @return a list of entry nodes to the graph
   */
  public static List<GraphNode> buildAcyclicGraphFromEdges(List<GraphEdge> edges) {
    HashMap<GraphNode, Integer> inDegreeOf = new HashMap<>();
    // buildGraph
    for (GraphEdge graphEdge : edges) {
      GraphNode parentNode = graphEdge.getFromNode();
      GraphNode childNode = graphEdge.getToNode();
      parentNode.addChildNode(childNode);
      childNode.addParentNode(parentNode);
      inDegreeOf.put(parentNode, 0);
      inDegreeOf.put(childNode, 0);
    }

    List<GraphNode> potentialResult = new ArrayList<>();
    Queue<GraphNode> queue = new LinkedList<>();
    // populate value in map inDegreeOf
    for (Map.Entry<GraphNode, Integer> entry : inDegreeOf.entrySet()) {
      GraphNode node = entry.getKey();
      inDegreeOf.put(node, node.getParentNodes().size());
      if (node.getParentNodes().isEmpty()) {
        potentialResult.add(node);
        queue.offer(node);
      }
    }

    int countOfNodesPolledFromQueue = 0;
    while (!queue.isEmpty()) {
      GraphNode polled = queue.poll();
      countOfNodesPolledFromQueue++;
      Set<GraphNode> children = polled.getChildNodes();
      if (children.isEmpty()) {
        continue;
      }
      for (GraphNode child : children) {
        inDegreeOf.compute(child, (stillThisChildNode, oldIndegree) -> {
          Integer newIndegree = oldIndegree - 1;
          if (newIndegree == 0) {
            queue.offer(stillThisChildNode);
          }
          return newIndegree;
        });
      }
    }
    if (countOfNodesPolledFromQueue != inDegreeOf.size()) {
      throw new IllegalStateException("the graph has cycle");
    }
    return potentialResult;
  }

  public static boolean isAcyclic(List<GraphEdge> edges) {
    try {
      buildAcyclicGraphFromEdges(edges);
      return true;
    } catch (IllegalStateException ise) {
      if (ise.getMessage().equals("the graph has cycle")) {
        return false;
      }
      throw ise;
    }
  }

  public static List<GraphNode> edgesToNodes(Collection<? extends GraphEdge> edges) {
    if (edges == null || edges.isEmpty()) {
      throw new IllegalArgumentException("edges cannot be null or empty");
    }
    HashSet<GraphNode> resSet = new HashSet<>();
    for (GraphEdge graphEdge : edges) {
      GraphNode parentNode = graphEdge.getFromNode();
      GraphNode childNode = graphEdge.getToNode();
      parentNode.addChildNode(childNode);
      childNode.addParentNode(parentNode);
      resSet.add(parentNode);
      resSet.add(childNode);
    }
    return new ArrayList<>(resSet);
  }

  public static List<GraphEdge> nodesToEdges(Collection<? extends GraphNode> nodes) {
    if (nodes == null || nodes.isEmpty()) {
      throw new IllegalArgumentException("nodes cannot be null or empty");
    }
    HashSet<GraphNode> visitedNodes = new HashSet<>();
    List<GraphEdge> res = new ArrayList<>();
    Queue<GraphNode> queue = new LinkedList<>();
    for (GraphNode node : nodes) {
      if (node.getParentNodes().isEmpty()) {
        visitedNodes.add(node);
        queue.offer(node);
      }
    }
    while (!queue.isEmpty()) {
      GraphNode parentNode = queue.poll();
      for (GraphNode childNode : parentNode.getChildNodes()) {
        if (visitedNodes.add(childNode)) {
          res.add(new GraphEdge(parentNode, childNode));
          queue.offer(childNode);
        }
      }
    }
    return res;
  }
}
