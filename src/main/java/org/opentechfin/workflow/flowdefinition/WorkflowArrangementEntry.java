package org.opentechfin.workflow.flowdefinition;

import org.opentechfin.workflow.acyclicgraph.GraphEdge;
import org.opentechfin.workflow.acyclicgraph.GraphNode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.opentechfin.workflow.utils.StringUtils;

public class WorkflowArrangementEntry {

  private String upstreamOperationName;

  private String downstreamOperationName;

  public WorkflowArrangementEntry(String upstreamOperationNameParam, String downstreamOperationNameParam) {
    upstreamOperationName = upstreamOperationNameParam;
    downstreamOperationName = downstreamOperationNameParam;
  }

  /**
   * get list of GraphEdge based on upstreamOperationName and downstreamOperationName of a
   * {@link WorkflowArrangementEntry}
   * @param wfEntries list of <code>WorkflowArrangementEntry</code>
   * @return a list of linked GraphEdge
   */
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
