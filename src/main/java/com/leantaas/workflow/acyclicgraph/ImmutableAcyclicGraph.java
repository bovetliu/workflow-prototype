package com.leantaas.workflow.acyclicgraph;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
public class ImmutableAcyclicGraph extends AcyclicGraph {

    public ImmutableAcyclicGraph(List<GraphEdge> edges) {
        super();  // for readability, I am explicitly invoking one default protected constructor
        if (!WorkFlowGraphUtil.isAcyclic(edges)) {
            throw new IllegalArgumentException("edgeList cannot form an acyclic graph");
        }
        List<GraphNode> tempGraphNodes = WorkFlowGraphUtil.edgesToNodes(edges);
        ImmutableSet.Builder<ImmutableGraphNode> builder = ImmutableSet.builder();
        HashMap<String, ImmutableGraphNode> clonedMap = new HashMap<>();
        for (GraphNode graphNode : tempGraphNodes) {
            ImmutableGraphNode cloned = cloneNode(graphNode, clonedMap);
            cloned.solidify();
            builder.add(cloned);
        }
        super.nodes = builder.build();
    }

    public ImmutableAcyclicGraph(AcyclicGraph acyclicGraph) {
        Objects.requireNonNull(acyclicGraph, "acyclicGraph cannot be null");
        if (acyclicGraph instanceof ImmutableAcyclicGraph && acyclicGraph == this) {
            throw new IllegalArgumentException("cannot create one ImmutableAcyclicGraph from self");
        }
        ImmutableSet.Builder<ImmutableGraphNode> builder = ImmutableSet.builder();
        HashMap<String, ImmutableGraphNode> clonedMap = new HashMap<>();
        for (GraphNode graphNode : acyclicGraph.getNodes()) {
            ImmutableGraphNode cloned = cloneNode(graphNode, clonedMap);
            cloned.solidify();
            builder.add(cloned);
        }
        super.nodes = builder.build();
    }

    @Override
    public void setNodes(Set<GraphNode> graphNodeSet) {
        throw new UnsupportedOperationException("does not support");
    }

    @Override
    public boolean isInGraph(GraphNode graphNode) {
        Objects.requireNonNull(graphNode, "graphNode cannot be null");
        if (! (graphNode instanceof  ImmutableGraphNode)) {
            throw new IllegalArgumentException("ImmutableAcyclicGraph only supports ImmutableGraphNode");
        }
        ImmutableGraphNode thatNode = (ImmutableGraphNode) graphNode;
        return super.nodes.contains(thatNode);
    }

    private ImmutableGraphNode cloneNode(GraphNode graphNode, HashMap<String, ImmutableGraphNode> clonedMap) {
        if (graphNode == null) {
            return null;
        }
        ImmutableGraphNode cloned = clonedMap.get(graphNode.getGraphNodeId());
        if (cloned != null) {
            return cloned;
        }
        // cloned is null here
        cloned = new ImmutableGraphNode(graphNode);
        clonedMap.put(cloned.getGraphNodeId(), cloned);
        for (GraphNode child : graphNode.getChildNodes()) {
            ImmutableGraphNode clonedChild = cloneNode(child, clonedMap);
            cloned.childNodes.add(clonedChild);
            clonedChild.parentNodes.add(cloned);
        }
        return cloned;
    }
}
