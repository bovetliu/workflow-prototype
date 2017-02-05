package com.leantaas.workflow.acyclicgraph;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Graph node is not stored in DB
 * By default they all handles integer
 * Created by boweiliu on 12/11/16.
 */
public class GraphNode {

    private static AtomicInteger graphNodeIdRecord = new AtomicInteger(1);

    private final String graphNodeId;

    protected Set<GraphNode> parentNodes;

    protected Set<GraphNode> childNodes;

    public GraphNode() {
        this("graphNode : " + graphNodeIdRecord.getAndIncrement());
    }

    public GraphNode(String graphNodeIdParam) {
        Objects.requireNonNull(graphNodeIdParam, "graphNodeIdParam should not be null");
        if (graphNodeIdParam.isEmpty()) {
            throw new IllegalArgumentException("graphNodeIdParam cannot be empty");
        }
        graphNodeId = graphNodeIdParam;
        parentNodes = new HashSet<>();
        childNodes = new HashSet<>();
    }

    public String getGraphNodeId() {
        return graphNodeId;
    }

    public Set<GraphNode> getParentNodes() {
        return parentNodes;
    }

    public void setParentNodes(Set<GraphNode> parentNodes) {
        this.parentNodes = parentNodes;
    }

    public boolean addParentNode(GraphNode graphNode) {
        return parentNodes.add(graphNode);
    }

    public Set<GraphNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(Set<GraphNode> childNodes) {
        this.childNodes = childNodes;
    }

    public boolean addChildNode(GraphNode graphNode) {
        return childNodes.add(graphNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GraphNode)) {
            return false;
        }

        GraphNode graphNode = (GraphNode) o;

        return graphNodeId.equals(graphNode.graphNodeId);
    }

    @Override
    public int hashCode() {
        return graphNodeId.hashCode();
    }
}
