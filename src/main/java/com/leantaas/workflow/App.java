package com.leantaas.workflow;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.leantaas.workflow.acyclicgraph.AcyclicGraphTraverse;
import com.leantaas.workflow.acyclicgraph.GraphEdge;
import com.leantaas.workflow.acyclicgraph.GraphNode;
import com.leantaas.workflow.acyclicgraph.ImmutableAcyclicGraph;
import com.leantaas.workflow.acyclicgraph.ImmutableGraphNode;
import com.leantaas.workflow.guicemodule.WorkflowModule;
import com.leantaas.workflow.kinesis.FakeKinesis;
import com.leantaas.workflow.operations.ETLOperations;
import com.leantaas.workflow.operations.MetricsOperations;
import com.leantaas.workflow.operations.dto.FileUploadCompletionDTO;
import com.leantaas.workflow.operations.impl.ETLOperationsImpl;
import com.leantaas.workflow.operations.impl.MetricsOperationsImpl;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class App {


    private static GraphEdge link(List<GraphNode> graphNodeList, int fromId, int toId) {
        return new GraphEdge(graphNodeList.get(fromId), graphNodeList.get(toId));
    }

    private static AcyclicGraphTraverse buildWorkFlow() {
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

        String operationName = Preconditions.checkNotNull(msg.getOperationName(), "operationName cannot be null");
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

    private static final String UPLOAD_FILE_OPERATION = "uploadFileOperation";
    private static final String FILE_OP_METHOD_NAME = "processfileOperation";
    private static final String COMPUTE_HUDDLE_OP_METHOD_NAME = "computeHuddleVolumeOperation";

    public static void main(String[] args) {
        // simulate play app
        Injector injector = Guice.createInjector(new WorkflowModule());
        System.out.println("guice injector first initialize kinesis");
        System.out.println("guice injector finished initialize operation classes");

        FakeKinesis kinesis = injector.getInstance(FakeKinesis.class);

        ETLOperations etlOperations = injector.getInstance(ETLOperations.class);
        MetricsOperations metricsOperations = injector.getInstance(MetricsOperations.class);

        // TODO scan annotations and build association between operation name and method
        final Map<String, Entry<Object, Method>> nodeIdVsMethodEntry = new HashMap<>();

        try {
            Class<?>[] procesFileParameterTypes = new Class[]{FileUploadCompletionDTO.class};
            Method processfileOperationMethod = ETLOperationsImpl.class.getMethod(FILE_OP_METHOD_NAME, procesFileParameterTypes);
            nodeIdVsMethodEntry.put("graphNode : 2",
                    new AbstractMap.SimpleEntry<>(etlOperations, processfileOperationMethod));

            Class<?>[] computeHuddleParameterTypes = new Class[]{String.class};
            Method computeHuddleVolumeOperationMethod = MetricsOperationsImpl.class
                    .getMethod(COMPUTE_HUDDLE_OP_METHOD_NAME, computeHuddleParameterTypes);
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

        /*
        * TODO Following update on operationNameResolveToNode will be done by class in operationassociation class,
        * TODO information should be fetched from workflow arrangement table
        * */
        operationNameResolveToNode.put(UPLOAD_FILE_OPERATION, "graphNode : 1");
        operationNameResolveToNode.put(FILE_OP_METHOD_NAME, "graphNode : 2");
        operationNameResolveToNode.put(COMPUTE_HUDDLE_OP_METHOD_NAME, "graphNode : 3");
        System.out.println("operation name binding to work flow completed.\n");

        // Simulate file upload at 4th second
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.schedule(() -> {
            etlOperations.uploadFileOperation("demo_file.csv");
        }, 2, TimeUnit.SECONDS);

        // finish execution at 15 second
        scheduledExecutorService.schedule(() -> {
            System.out.println("finish execution");
            for (GraphNode graphNode : oneWorkFlowRun.getImmutableAcyclicGraph().getNodes()) {
                oneWorkFlowRun.visit(graphNode);
            }
            if (!oneWorkFlowRun.finishedTraverse()) {
                throw new IllegalStateException("oneWorkFlow should already finished traversing");
            }
            kinesis.put(new OperationCompletionMessage("avoid_kinesis_take()_block", String.class, "dummy content"));
        }, 18, TimeUnit.SECONDS);

        // simulate workflow running

        while (!oneWorkFlowRun.finishedTraverse()) {
            OperationCompletionMessage operationCompletionMessage = kinesis.take();
            System.out.println("[Operation Completion Received] " + operationCompletionMessage.getOperationName() + " completion received\n");
            Optional<ImmutableGraphNode> associatedNodeOptional = associateMsgToNode(
                    operationCompletionMessage, operationNameResolveToNode, nodeIdVsNode);
            if (!associatedNodeOptional.isPresent()) {
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
                        method.invoke(object, operationCompletionMessage.getReturnClazz()
                                .cast(operationCompletionMessage.getReturnedObject()));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        //close exeuctors
        scheduledExecutorService.shutdown();
        fixedThreadPool.shutdown();
    }
}
