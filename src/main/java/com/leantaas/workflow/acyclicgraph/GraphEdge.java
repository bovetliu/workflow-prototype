package com.leantaas.workflow.acyclicgraph;


/**
 * GraphEdge is what will be persisted in database.
 * Created by boweiliu on 12/11/16.
 */
public class GraphEdge {

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
}
