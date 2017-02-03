package com.leantaas.workflow.acyclicgraph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by boweiliu on 2/3/17.
 */
public class ImmutableAcyclicGraphTest {

    @Test
    public void testGraphCopyUsingList() {
        GraphNode node1 = new GraphNode("1");
        GraphNode node2 = new GraphNode("2");
        GraphNode node3 = new GraphNode("3");
        GraphNode node4 = new GraphNode("4");
        GraphNode node5 = new GraphNode("5");
        GraphNode node6 = new GraphNode("6");
        GraphNode node7 = new GraphNode("7");
        GraphNode node8 = new GraphNode("8");
        GraphNode node9 = new GraphNode("9");


        GraphEdge graphEdge01 = new GraphEdge(node1, node2);
        GraphEdge graphEdge02 = new GraphEdge(node2, node3);
        GraphEdge graphEdge03 = new GraphEdge(node2, node4);
        GraphEdge graphEdge04 = new GraphEdge(node4, node5);
        GraphEdge graphEdge05 = new GraphEdge(node5, node6);

        ImmutableAcyclicGraph immutableAcyclicGraph01 = new ImmutableAcyclicGraph(Arrays.asList(graphEdge01, graphEdge02,
                graphEdge03,
                graphEdge04,
                graphEdge05));
        List<GraphEdge> graphEdgeList01 = WorkFlowGraphUtil.nodesToEdges(immutableAcyclicGraph01.getNodes());
        Collections.sort(graphEdgeList01);


        ImmutableAcyclicGraph immutableAcyclicGraph02 = new ImmutableAcyclicGraph(immutableAcyclicGraph01);
        List<GraphEdge> graphEdgeList02 = WorkFlowGraphUtil.nodesToEdges(immutableAcyclicGraph02.getNodes());
        Collections.sort(graphEdgeList02);

        Assert.assertEquals(graphEdgeList01.size(), graphEdgeList02.size());
        for (int i = 0; i < graphEdgeList01.size(); i++) {
            Assert.assertEquals(graphEdgeList01.get(i), graphEdgeList02.get(i));
        }
    }
}
