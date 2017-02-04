package com.leantaas.workflow.acyclicgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by boweiliu on 2/3/17.
 */
public class AcyclicGraphTraverseTest {



    private ImmutableAcyclicGraph quickBuildOneGraph(List<GraphNode> graphNodesList) {
        if (graphNodesList.size() != 20) {
            throw new IllegalArgumentException("only accept size 20 node list");
        }
        List<GraphEdge> graphEdgeList = new ArrayList<>();
        graphEdgeList.add(link(graphNodesList, 1 - 1, 2 - 1 ));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 3 - 1 ));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 4 - 1 ));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 5 - 1 ));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 6 - 1 ));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 7 - 1 ));
        graphEdgeList.add(link(graphNodesList, 3 - 1, 10 - 1 ));
        graphEdgeList.add(link(graphNodesList, 4 - 1, 9 - 1 ));
        graphEdgeList.add(link(graphNodesList, 5 - 1, 9 - 1 ));
        graphEdgeList.add(link(graphNodesList, 6 - 1, 8 - 1 ));
        graphEdgeList.add(link(graphNodesList, 7 - 1, 8 - 1 ));
        graphEdgeList.add(link(graphNodesList, 7 - 1, 15 - 1 ));
        graphEdgeList.add(link(graphNodesList, 10 - 1, 11 - 1 ));
        graphEdgeList.add(link(graphNodesList, 9 - 1, 11 - 1 ));
        graphEdgeList.add(link(graphNodesList, 8 - 1, 11 - 1 ));
        graphEdgeList.add(link(graphNodesList, 8 - 1, 13 - 1 ));
        graphEdgeList.add(link(graphNodesList, 8 - 1, 15 - 1 ));
        graphEdgeList.add(link(graphNodesList, 11 - 1, 12 - 1 ));
        graphEdgeList.add(link(graphNodesList, 13 - 1, 14 - 1 ));
        graphEdgeList.add(link(graphNodesList, 15 - 1, 17 - 1 ));
        graphEdgeList.add(link(graphNodesList, 18 - 1, 17 - 1 ));
        graphEdgeList.add(link(graphNodesList, 19 - 1, 17 - 1 ));
        graphEdgeList.add(link(graphNodesList, 17 - 1, 20 - 1 ));
        return new ImmutableAcyclicGraph(graphEdgeList);
    }


    @Test
    public void testVisit() {
        List<GraphNode> graphNodesList = new ArrayList<>();
        for (int i = 0 ; i < 20; i++) {
            graphNodesList.add(new GraphNode());
        }
        ImmutableAcyclicGraph immutableAcyclicGraph  = quickBuildOneGraph(graphNodesList);
        Set<? extends GraphNode> immutableNodes = immutableAcyclicGraph.getNodes();
        for (int i = 0 ; i < graphNodesList.size(); i++) {
            for (GraphNode graphNode : immutableNodes) {
                if (graphNode.getGraphNodeId().equals(graphNodesList.get(i).getGraphNodeId())) {
                    graphNodesList.set(i, graphNode);
                    break;
                }
            }
        }

        AcyclicGraphTraverse acyclicGraphTraverse = new AcyclicGraphTraverse(immutableAcyclicGraph);
        List<GraphNode> nextVisitableNoes = acyclicGraphTraverse.visit(graphNodesList.get(0));
        Assert.assertEquals(1, nextVisitableNoes.size());
        Assert.assertEquals(graphNodesList.get(1), nextVisitableNoes.get(0));

        nextVisitableNoes = acyclicGraphTraverse.visit(graphNodesList.get(1));
        Assert.assertEquals(5, nextVisitableNoes.size());

        // simulate more than once queue delivery
        nextVisitableNoes = acyclicGraphTraverse.visit(graphNodesList.get(1));
        Assert.assertEquals(0, nextVisitableNoes.size());

        Assert.assertEquals(0, acyclicGraphTraverse.visit(graphNodesList.get(14)).size());
        Assert.assertEquals(0, acyclicGraphTraverse.visit(graphNodesList.get(17)).size());
        Assert.assertEquals(1, acyclicGraphTraverse.visit(graphNodesList.get(18)).size());

    }

    /**
     * Scenario description: Demo test, Have a graph contains 20 nodes, 3 worker thread traverse
     * one {@link AcyclicGraphTraverse} object
     */
    @Test
    public void demoTest() throws InterruptedException {
        // the main thread is the demoTest junit thead.
        List<GraphNode> graphNodesList = new ArrayList<>();
        for (int i = 0 ; i < 20; i++) {
            graphNodesList.add(new GraphNode());
        }

        ImmutableAcyclicGraph immutableAcyclicGraph  = quickBuildOneGraph(graphNodesList);
        Set<? extends GraphNode> immutableNodes = immutableAcyclicGraph.getNodes();
        for (int i = 0 ; i < graphNodesList.size(); i++) {
            for (GraphNode graphNode : immutableNodes) {
                if (graphNode.getGraphNodeId().equals(graphNodesList.get(i).getGraphNodeId())) {
                    graphNodesList.set(i, graphNode);
                    break;
                }
            }
        }

        final GraphNode fileUploadCompletion = graphNodesList.get(0);

        // now I create one traverse of this graph
        AcyclicGraphTraverse acyclicGraphTraverse = new AcyclicGraphTraverse(immutableAcyclicGraph);

        // get one kinesis
        BlockingQueue<GraphNode> kinesis = new ArrayBlockingQueue<>(100);  // queue length is 100

        // initialize worker pool, worker is designated to execute operations indicated by GraphNode,
        // will use java reflection DynamicInvocation plust AOP method interceptor java converting one common method
        // into Workflow Operation
        ExecutorService workerFlowWorkers = Executors.newFixedThreadPool(3);


        // from iqueue-app, we receive one file uploading completion report
            // synchronously add completion message to kinesis
        System.out.println("File upload completion");
        kinesis.put(fileUploadCompletion);  // this is a blocking method, so it will raise exception

        int count = 0;
        // our work flow started consuming kinesis
        while (!acyclicGraphTraverse.finishedTraverse()) {
            GraphNode oneTaskCompletion = kinesis.take();
            List<GraphNode> nextExecutableOperations = acyclicGraphTraverse.visit(oneTaskCompletion);
            for (GraphNode oneOperation : nextExecutableOperations) {
                // if no next available thread, then wait
                workerFlowWorkers.submit(new WorkerUsedForTestOnly(oneOperation, kinesis));
            }

            // simulate timed execution.
            if (count == 12) {
                System.out.println("Timed execution happened");
                workerFlowWorkers.submit(new WorkerUsedForTestOnly(graphNodesList.get(17), kinesis));
                workerFlowWorkers.submit(new WorkerUsedForTestOnly(graphNodesList.get(18), kinesis));
            }
            count++;
        }
        workerFlowWorkers.shutdown();
    }

    private GraphEdge link(List<GraphNode> graphNodeList, int fromId, int toId) {
        return new GraphEdge(graphNodeList.get(fromId), graphNodeList.get(toId));
    }

    public class WorkerUsedForTestOnly implements Runnable {


        private final GraphNode toBeVisitedNode;

        private final BlockingQueue<GraphNode> kinesisConnector;

        private final Random random = new Random();

        public WorkerUsedForTestOnly(GraphNode toBeVisitedNodeParam, BlockingQueue<GraphNode> kinesisConnectorParam) {
            toBeVisitedNode = toBeVisitedNodeParam;
            kinesisConnector = kinesisConnectorParam;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " - I am doing something heavy, I am fulfilling "
                    + "operation of node :" + toBeVisitedNode.getGraphNodeId());
            try {
                Thread.sleep(3000L + random.nextInt(3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            kinesisConnector.offer(toBeVisitedNode);  // at this time, it has already been finished.
        }
    }
}
