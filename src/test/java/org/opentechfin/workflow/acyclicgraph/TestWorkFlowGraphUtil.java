package org.opentechfin.workflow.acyclicgraph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by boweiliu on 2/3/17.
 */
public class TestWorkFlowGraphUtil {


  /**
   * Scenario Description: the most common use case of buildAcyclicGraphFromEdges
   */
  @Test
  public void testBuildGraphHappyPath01() {
    GraphNode node1 = new GraphNode("1");
    GraphNode node2 = new GraphNode("2");
    GraphNode node3 = new GraphNode("3");
    GraphNode node4 = new GraphNode("4");
    GraphNode node5 = new GraphNode("5");
    GraphNode node6 = new GraphNode("6");
    GraphNode node7 = new GraphNode("7");
    GraphNode node8 = new GraphNode("8");
    GraphNode node9 = new GraphNode("9");

    GraphEdge graphEdge01 = new GraphEdge(node1, node5);
    GraphEdge graphEdge02 = new GraphEdge(node1, node2);
    GraphEdge graphEdge03 = new GraphEdge(node2, node4);
    GraphEdge graphEdge04 = new GraphEdge(node2, node3);
    GraphEdge graphEdge05 = new GraphEdge(node5, node6);
    GraphEdge graphEdge06 = new GraphEdge(node4, node6);
    GraphEdge graphEdge07 = new GraphEdge(node6, node7);
    GraphEdge graphEdge08 = new GraphEdge(node3, node7);
    GraphEdge graphEdge09 = new GraphEdge(node8, node7);
    GraphEdge graphEdge10 = new GraphEdge(node8, node9);

    List<GraphNode> entriesToGraph = GraphUtil.buildAcyclicGraphFromEdges(Arrays.asList(graphEdge01, graphEdge02,
        graphEdge03,
        graphEdge04,
        graphEdge05,
        graphEdge06,
        graphEdge07,
        graphEdge08,
        graphEdge09,
        graphEdge10));
    Set<GraphNode> setOfEntries = new HashSet<>(entriesToGraph);
    Assert.assertEquals(2, setOfEntries.size());
    Assert.assertTrue(setOfEntries.contains(node1));
    Assert.assertTrue(setOfEntries.contains(node8));
  }

  @Test(expected = IllegalStateException.class)
  public void testBuildGraphFromEdgesSadPath() {
    GraphNode node1 = new GraphNode("1");
    GraphNode node2 = new GraphNode("2");
    GraphNode node3 = new GraphNode("3");
    GraphNode node4 = new GraphNode("4");
    GraphNode node5 = new GraphNode("5");
    GraphNode node6 = new GraphNode("6");
    GraphNode node7 = new GraphNode("7");
    GraphNode node8 = new GraphNode("8");
    GraphNode node9 = new GraphNode("9");

    GraphEdge graphEdge01 = new GraphEdge(node1, node5);
    GraphEdge graphEdge02 = new GraphEdge(node1, node2);
    GraphEdge graphEdge03 = new GraphEdge(node2, node4);
    GraphEdge graphEdge04 = new GraphEdge(node2, node3);
    GraphEdge graphEdge05 = new GraphEdge(node5, node6);
    GraphEdge graphEdge06 = new GraphEdge(node4, node6);
    GraphEdge graphEdge07 = new GraphEdge(node6, node7);
    GraphEdge graphEdge08 = new GraphEdge(node3, node7);
    GraphEdge graphEdge09 = new GraphEdge(node8, node7);
    GraphEdge graphEdge10 = new GraphEdge(node8, node9);
    GraphEdge graphEdge11 = new GraphEdge(node7, node2); // this will trigger exception

    // following execution will raise exception, since graphEdge11 will trigger exception.
    List<GraphNode> entriesToGraph = GraphUtil.buildAcyclicGraphFromEdges(Arrays.asList(graphEdge01, graphEdge02,
        graphEdge03,
        graphEdge04,
        graphEdge05,
        graphEdge06,
        graphEdge07,
        graphEdge08,
        graphEdge09,
        graphEdge10,
        graphEdge11));
  }
}
