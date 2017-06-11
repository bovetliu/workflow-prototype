package com.leantaas.workflow.flowdefinition;

import com.amazonaws.util.StringUtils;
import com.leantaas.workflow.acyclicgraph.GraphEdge;
import com.leantaas.workflow.acyclicgraph.GraphNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class WorkflowArrangementEntry {

  private String upstreamOperationName;

  private String downstreamOperationName;

  public WorkflowArrangementEntry(String upstreamOperationNameParam, String downstreamOperationNameParam) {
    upstreamOperationName = upstreamOperationNameParam;
    downstreamOperationName = downstreamOperationNameParam;
  }

  @SuppressWarnings("Convert2MethodRef")
  public static List<GraphEdge> toGraphEdges(Collection<WorkflowArrangementEntry> wfEntries) {
    Map<String, GraphNode> opnameVsNode = new TreeMap<>();
    Set<GraphEdge> res = new HashSet<>();
    for (WorkflowArrangementEntry entry : wfEntries) {
      GraphNode fromNode = opnameVsNode.computeIfAbsent(entry.upstreamOperationName, upstreamOperationName ->
          new GraphNode(upstreamOperationName));
      GraphNode toNode = opnameVsNode.computeIfAbsent(entry.downstreamOperationName, downstreamOperationName ->
          new GraphNode(downstreamOperationName));
      res.add(new GraphEdge(fromNode, toNode));
    }
    return new ArrayList<>(res);
  }

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
}
