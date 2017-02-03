package com.leantaas.workflow.acyclicgraph;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by boweiliu on 2/3/17.
 */
public class AcyclicGraphTest {

    @Test
    public void testIsReachableFromTo() {
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

        AcyclicGraph acyclicGraphUnderTest = new AcyclicGraph(Arrays.asList(graphEdge01, graphEdge02, graphEdge03,
                graphEdge04,
                graphEdge05));

        Assert.assertTrue(acyclicGraphUnderTest.isReachableFromTo(node1, node2));
        Assert.assertTrue(acyclicGraphUnderTest.isReachableFromTo(node2, node3));
        Assert.assertTrue(acyclicGraphUnderTest.isReachableFromTo(node2, node4));
        Assert.assertTrue(acyclicGraphUnderTest.isReachableFromTo(node1, node5));


        Assert.assertFalse(acyclicGraphUnderTest.isReachableFromTo(node1, node9));
        Assert.assertFalse(acyclicGraphUnderTest.isReachableFromTo(node6, node8));
        Assert.assertFalse(acyclicGraphUnderTest.isReachableFromTo(node8, node7));

        Assert.assertFalse(acyclicGraphUnderTest.isReachableFromTo(node2, node1));
        Assert.assertFalse(acyclicGraphUnderTest.isReachableFromTo(node3, node2));
        Assert.assertFalse(acyclicGraphUnderTest.isReachableFromTo(node5, node1));
    }

}
