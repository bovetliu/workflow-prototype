package org.opentechfin.workflow.acyclicgraph;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class traverse logic of one {@link ImmutableAcyclicGraph} traverse. Usually works with queues.
 */
public class AcyclicGraphTraverse {

  protected final ImmutableAcyclicGraph immutableAcyclicGraph;

  // TODO (bowei) should change to HashMap<GraphNode, STATUS>  STATUS is Enum : VISITED, UNVISITED, ERROR
  protected final HashMap<GraphNode, Boolean> visitedMap;

  protected final AtomicInteger visitedNodeNumber = new AtomicInteger();

  protected final Lock hashMapLock = new ReentrantLock(true);

  public AcyclicGraphTraverse(ImmutableAcyclicGraph immutableAcyclicGraphParam) {
    immutableAcyclicGraph = immutableAcyclicGraphParam;
    visitedMap = new HashMap<>(immutableAcyclicGraph.getNodes().size());
    for (GraphNode graphNode : immutableAcyclicGraph.getNodes()) {
      visitedMap.put(graphNode, false);
    }
  }

  public boolean isTraverseFinished() {
    hashMapLock.lock();
    boolean res = visitedNodeNumber.get() == visitedMap.size();
    hashMapLock.unlock();
    return res;
  }

  /**
   * //TODO(bowei), need to change this method to be O(|E|), for now it is O(|E| ^ 2)
   *
   * @param graphNode visit one node
   * @return next visitable nodes.
   */
  public List<GraphNode> visit(GraphNode graphNode) {
    Objects.requireNonNull(graphNode, "graphNode argument is null.");
    if (!immutableAcyclicGraph.isInGraph(graphNode)) {
      throw new IllegalArgumentException(graphNode.getGraphNodeId() + " is not in this acyclic graph");
    }
    try {
      hashMapLock.lock();
      Boolean isThisParentNodeVisited = Preconditions.checkNotNull(visitedMap.get(graphNode),
          "Program logic error, this node is not in visitedMap");
      List<GraphNode> res = new ArrayList<>();
      if (isThisParentNodeVisited) {
        // one distributed message queue might deliver message at least once, when one message is delivered more
        // than once, should not trigger downstream visiting again.
        return res;
      }
      visitedMap.put(graphNode, true);  // visit this node
      visitedNodeNumber.incrementAndGet();
      Set<GraphNode> childNodes = graphNode.getChildNodes();
      if (childNodes.isEmpty()) {
        return res;
      }

      for (GraphNode child : childNodes) {
        if (parentsAllVisited(child)) {
          res.add(child);
        }
      }
      return res;
    } finally {
      hashMapLock.unlock();
    }
  }

  public ImmutableMap<GraphNode, Boolean> copyVisitedMapToImmutableMap() {
    hashMapLock.lock();
    ImmutableMap<GraphNode, Boolean> res = ImmutableMap.copyOf(visitedMap);
    hashMapLock.unlock();
    return res;
  }

  // this method is not threadsafe, but it is only invoked in threadsafe section.
  private boolean parentsAllVisited(GraphNode graphNode) {
    Objects.requireNonNull(graphNode, "graphNode argument is null.");
    if (!immutableAcyclicGraph.isInGraph(graphNode)) {
      throw new IllegalArgumentException(graphNode.getGraphNodeId() + " is not in this acyclic graph");
    }
    for (GraphNode parentOfGraphNode : graphNode.getParentNodes()) {
      Boolean isThisParentNodeVisited = Preconditions.checkNotNull(visitedMap.get(parentOfGraphNode),
          "Program logic error, this node is not in visitedMap");
      if (!isThisParentNodeVisited) {
        return false;
      }
    }
    return true;
  }

  public ImmutableAcyclicGraph getImmutableAcyclicGraph() {
    return immutableAcyclicGraph;
  }


}
