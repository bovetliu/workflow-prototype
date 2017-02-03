package com.leantaas.workflow.acyclicgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class provide util, all static methods
 */
public class WorkFlowGraphUtil {
    private WorkFlowGraphUtil() {
        // disable initialization
    }

    /**
     *
     *
     * @param edges a list of graphEdge
     * @return a list of entry nodes to the graph
     */
    public static List<GraphNode> buildGraphFromEdges(List<GraphEdge> edges) {
        HashMap<GraphNode, Integer> inDegreeOf = new HashMap<>();
        // buildGraph
        for (GraphEdge graphEdge :edges) {
            GraphNode parentNode = graphEdge.getFromNode();
            GraphNode childNode = graphEdge.getToNode();
            parentNode.addChildNode(childNode);
            childNode.addParentNode(parentNode);
            inDegreeOf.put(parentNode, 0);
            inDegreeOf.put(childNode, 0);
        }

        List<GraphNode> potentialResult = new ArrayList<>();
        Queue<GraphNode> queue = new LinkedList<>();
        // populate value in map inDegreeOf
        for (Map.Entry<GraphNode, Integer> entry : inDegreeOf.entrySet()) {
            GraphNode node = entry.getKey();
            inDegreeOf.put(node, node.getParentNodes().size());
            if (node.getParentNodes().isEmpty()) {
                potentialResult.add(node);
                queue.offer(node);
            }
        }

        int countOfNodesPolledFromQueue = 0;
        while (!queue.isEmpty()) {
            GraphNode polled = queue.poll();
            countOfNodesPolledFromQueue++;
            Set<GraphNode> children = polled.getChildNodes();
            if (children.isEmpty()) {
                continue;
            }
            for (GraphNode child : children) {
                inDegreeOf.compute(child, (stillThisChildNode, oldIndegree) -> {
                    Integer newIndegree = oldIndegree - 1;
                    if (newIndegree == 0) {
                        queue.offer(stillThisChildNode);
                    }
                    return newIndegree;
                });
            }
        }
        if (countOfNodesPolledFromQueue != inDegreeOf.size()) {
            throw new IllegalStateException("the graph has cycle");
        }
        return potentialResult;
    }
}
