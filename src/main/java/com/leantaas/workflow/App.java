package com.leantaas.workflow;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.leantaas.workflow.acyclicgraph.AcyclicGraphTraverse;
import com.leantaas.workflow.acyclicgraph.GraphEdge;
import com.leantaas.workflow.acyclicgraph.GraphNode;
import com.leantaas.workflow.acyclicgraph.ImmutableAcyclicGraph;
import com.leantaas.workflow.acyclicgraph.ImmutableGraphNode;
import com.leantaas.workflow.operations.ETLOperations;
import com.leantaas.workflow.operations.MetricsOperations;
import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class App {


    private static GraphEdge link(List<GraphNode> graphNodeList, int fromId, int toId) {
        return new GraphEdge(graphNodeList.get(fromId), graphNodeList.get(toId));
    }

    public static AcyclicGraphTraverse buildWorkFlow() {
        List<GraphNode> graphNodesList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            graphNodesList.add(new GraphNode());
        }

        if (graphNodesList.size() != 20) {
            throw new IllegalArgumentException("only accept size 20 node list");
        }
        List<GraphEdge> graphEdgeList = new ArrayList<>();
        graphEdgeList.add(link(graphNodesList, 1 - 1, 2 - 1));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 3 - 1));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 4 - 1));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 5 - 1));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 6 - 1));
        graphEdgeList.add(link(graphNodesList, 2 - 1, 7 - 1));
        graphEdgeList.add(link(graphNodesList, 3 - 1, 10 - 1));
        graphEdgeList.add(link(graphNodesList, 4 - 1, 9 - 1));
        graphEdgeList.add(link(graphNodesList, 5 - 1, 9 - 1));
        graphEdgeList.add(link(graphNodesList, 6 - 1, 8 - 1));
        graphEdgeList.add(link(graphNodesList, 7 - 1, 8 - 1));
        graphEdgeList.add(link(graphNodesList, 7 - 1, 15 - 1));
        graphEdgeList.add(link(graphNodesList, 10 - 1, 11 - 1));
        graphEdgeList.add(link(graphNodesList, 9 - 1, 11 - 1));
        graphEdgeList.add(link(graphNodesList, 8 - 1, 11 - 1));
        graphEdgeList.add(link(graphNodesList, 8 - 1, 13 - 1));
        graphEdgeList.add(link(graphNodesList, 8 - 1, 15 - 1));
        graphEdgeList.add(link(graphNodesList, 11 - 1, 12 - 1));
        graphEdgeList.add(link(graphNodesList, 13 - 1, 14 - 1));
        graphEdgeList.add(link(graphNodesList, 15 - 1, 17 - 1));
        graphEdgeList.add(link(graphNodesList, 18 - 1, 17 - 1));
        graphEdgeList.add(link(graphNodesList, 19 - 1, 17 - 1));
        graphEdgeList.add(link(graphNodesList, 17 - 1, 20 - 1));
        return new AcyclicGraphTraverse(new ImmutableAcyclicGraph(graphEdgeList));
    }


    private static Map<String, ImmutableGraphNode> buildNodeIdVsNodeAssociation(ImmutableAcyclicGraph acyclicGraph) {
        Map<String, ImmutableGraphNode> res = new HashMap<>();
        for (GraphNode graphNode : acyclicGraph.getNodes()) {
            res.put(graphNode.getGraphNodeId(), (ImmutableGraphNode) graphNode);
        }
        return res;
    }


    private static Optional<ImmutableGraphNode> associateMsgToNode(OperationCompletionMessage msg,
            HashBiMap<String, String> operationNameResolveToNodeId,
            Map<String, ImmutableGraphNode> nodeIdVsNode) {

        String operationName = Preconditions.checkNotNull(msg.get("operationName"), "operationName cannot be null");
        String nodeId = operationNameResolveToNodeId.get(operationName);
        if (nodeId == null) {
            return Optional.empty();
        }
        ImmutableGraphNode immutableGraphNode = nodeIdVsNode.get(nodeId);
        if (immutableGraphNode != null) {
            return Optional.of(immutableGraphNode);
        } else {
            return Optional.empty();
        }
    }

    private static final String FILE_OP_METHOD_NAME  = "processfileOperation";
    private static final String COMPUTE_HUDDLE_OP_METHOD_NAME = "computeHuddleVolumeOperation";

    public static void main(String[] args) {
        // simulate play app
        System.out.println("guice injector first initialize kinesis");
        BlockingQueue<OperationCompletionMessage> kinesis = new ArrayBlockingQueue<>(100);

        System.out.println("guice injector finished initialize operation classes");
        ETLOperations etlOperations = new ETLOperations(kinesis);
        MetricsOperations metricsOperations = new MetricsOperations(kinesis);

        // scan annotations and build association between operation name and method
        final Map<String, Entry<Object, Method>> nodeIdVsMethodEntry = new HashMap<>();
        Class<?>[] parameterTypes = new Class[]{OperationCompletionMessage.class};
        try {
            Method processfileOperationMethod = ETLOperations.class.getMethod(FILE_OP_METHOD_NAME, parameterTypes);
            nodeIdVsMethodEntry.put("graphNode : 2",
                    new AbstractMap.SimpleEntry<>(etlOperations, processfileOperationMethod));
            Method computeHuddleVolumeOperationMethod = MetricsOperations.class
                    .getMethod(COMPUTE_HUDDLE_OP_METHOD_NAME, parameterTypes);

            nodeIdVsMethodEntry.put("graphNode : 3",
                    new AbstractMap.SimpleEntry<>(metricsOperations, computeHuddleVolumeOperationMethod));
            System.out.println("Workflow weaver has finished building association between operation name and annotated "
                    + "methods");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        AcyclicGraphTraverse oneWorkFlowRun = buildWorkFlow();
        Map<String, ImmutableGraphNode> nodeIdVsNode = buildNodeIdVsNodeAssociation(
                oneWorkFlowRun.getImmutableAcyclicGraph());

        HashBiMap<String, String> operationNameResolveToNode = HashBiMap.create();
        operationNameResolveToNode.put("fileUploadCompletion", "graphNode : 1");
        operationNameResolveToNode.put(FILE_OP_METHOD_NAME, "graphNode : 2");
        operationNameResolveToNode.put(COMPUTE_HUDDLE_OP_METHOD_NAME, "graphNode : 3");

        System.out.println("operation name binding to work flow completed.");

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("finished uploading file to s3!");
                    OperationCompletionMessage fileUploadCompletion = new OperationCompletionMessage(
                            "fileUploadCompletion");
                    fileUploadCompletion.put("tenantName", "uchealth");
                    fileUploadCompletion.put("s3Url", "s3://abcdefghigjlimn12312313.awss3url.amazon.com");
                    kinesis.put(fileUploadCompletion);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }, 4, TimeUnit.SECONDS);

        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("finish execution");
                for (GraphNode graphNode : oneWorkFlowRun.getImmutableAcyclicGraph().getNodes()) {
                    oneWorkFlowRun.visit(graphNode);
                }
                if (!oneWorkFlowRun.finishedTraverse()) {
                    throw new IllegalStateException("oneWorkFlow should already finished traversing");
                }
                try {
                    kinesis.put(new OperationCompletionMessage("avoid_kinesis_take()_block"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }, 15, TimeUnit.SECONDS);
        AtomicInteger operationInvoked = new AtomicInteger();
        try {
            while (!oneWorkFlowRun.finishedTraverse()) {
                OperationCompletionMessage operationCompletionMessage = kinesis.take();
                System.out.println("####" + operationCompletionMessage.get("operationName") + " completion received");

                Optional<ImmutableGraphNode> associatedNodeOptional = associateMsgToNode(
                        operationCompletionMessage, operationNameResolveToNode, nodeIdVsNode);
                if (!associatedNodeOptional.isPresent()) {
                    System.out.println("finishedTraverse? " + oneWorkFlowRun.finishedTraverse());
                    continue;
                }
                ImmutableGraphNode immutableGraphNode = associatedNodeOptional.get();
                List<GraphNode> nextExecutableNodes = oneWorkFlowRun.visit(immutableGraphNode);
                for (GraphNode nextOperation : nextExecutableNodes) {
                    Entry<Object, Method> objectMethodEntry = nodeIdVsMethodEntry.get(nextOperation.getGraphNodeId());
                    if (objectMethodEntry == null) {
                        continue;
                    }
                    Object object = objectMethodEntry.getKey();
                    Method method = objectMethodEntry.getValue();
                    fixedThreadPool.submit(() -> {
                        try {
                            method.invoke(object, operationCompletionMessage);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        } catch (InterruptedException interruptedEx) {
            throw new RuntimeException(interruptedEx);
        }
        scheduledExecutorService.shutdown();
        fixedThreadPool.shutdown();
    }
}
