package com.leantaas.workflow.weaver;

import com.amazonaws.util.StringUtils;
import com.leantaas.workflow.acyclicgraph.GraphEdge;
import com.leantaas.workflow.acyclicgraph.GraphNode;

public class WorkflowArrangementEntry {

    private String upstreamOperationName;

    private String downstreamOperationName;

    public String getUpstreamOperationName() {
        return upstreamOperationName;
    }

    public void setUpstreamOperationName(String upstreamOperationName) {
        if (StringUtils.isNullOrEmpty(upstreamOperationName)) {
            throw new IllegalArgumentException("Operation name cannot be null or empty.");
        }
        this.upstreamOperationName = upstreamOperationName;
    }

    public String getDownstreamOperationName() {
        return downstreamOperationName;
    }

    public void setDownstreamOperationName(String downstreamOperationName) {
        if (StringUtils.isNullOrEmpty(upstreamOperationName)) {
            throw new IllegalArgumentException("Operation name cannot be null or empty.");
        }
        this.downstreamOperationName = downstreamOperationName;
    }

    public GraphEdge toGraphEdge() {
        return new GraphEdge(new GraphNode(upstreamOperationName), new GraphNode(downstreamOperationName));
    }
}
