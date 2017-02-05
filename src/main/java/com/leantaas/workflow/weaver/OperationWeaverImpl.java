package com.leantaas.workflow.weaver;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.leantaas.workflow.acyclicgraph.GraphEdge;
import com.leantaas.workflow.acyclicgraph.GraphNode;
import com.leantaas.workflow.acyclicgraph.ImmutableAcyclicGraph;
import com.leantaas.workflow.annotation.WorkflowOperation;
import com.leantaas.workflow.guicemodule.WorkflowModule;
import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import javax.inject.Singleton;

/**
 * This class is default implementation
 */
@Singleton
public class OperationWeaverImpl {

    private HashBiMap<String, SimpleEntry<Object, Method>> operationNameVsObjMethodEntry;

    private HashBiMap<String, GraphNode> operationNameVsGraphNode;

    private ImmutableAcyclicGraph immutableAcyclicGraph;

    private final Injector injector;

    // first I would like to hard code the package I need search
    public OperationWeaverImpl(List<WorkflowArrangementEntry> workflowArrangementEntryList) {
        if (workflowArrangementEntryList == null || workflowArrangementEntryList.isEmpty()) {
            throw new IllegalArgumentException("workflow arrangement should not be null or empty");
        }

        List<GraphEdge> graphEdgeList = workflowArrangementEntryList.stream().map(WorkflowArrangementEntry::toGraphEdge)
                .collect(Collectors.toList());
        immutableAcyclicGraph = new ImmutableAcyclicGraph(graphEdgeList);
        operationNameVsGraphNode = HashBiMap.create();
        for (GraphNode graphNode : immutableAcyclicGraph.getNodes()) {
            operationNameVsGraphNode.put(graphNode.getGraphNodeId(), graphNode);
        }

        injector = Guice.createInjector(new WorkflowModule());

        operationNameVsObjMethodEntry = HashBiMap.create();
        try {
            ClassPath classPath = ClassPath.from(OperationWeaverImpl.class.getClassLoader());
            //TODO(Bowei) make it configurable
            ImmutableSet<ClassInfo> topLevelClasses =
                    classPath.getTopLevelClasses("com.leantaas.workflow.operations.impl");
            for (ClassPath.ClassInfo classInfo : topLevelClasses) {
                Class<?> opClazz = classInfo.load();
                Map<String, Method> tempMapStoringAnnotatedMethods = new TreeMap<>();
                for (Method method : opClazz.getDeclaredMethods()) {
                    WorkflowOperation workflowOperationAnnotationInstance
                            = method.getAnnotation(WorkflowOperation.class);
                    if (workflowOperationAnnotationInstance != null) {
                        System.out.println("annotated method : " + method.getName());
                        tempMapStoringAnnotatedMethods.put(workflowOperationAnnotationInstance.operationName(), method);
                    }
                }
                // this class has WorkflowOperation annotated methods
                if (!tempMapStoringAnnotatedMethods.isEmpty()) {
                    Object object = injector.getInstance(opClazz);
                    for (Map.Entry<String, Method> opNameMethodEntry : tempMapStoringAnnotatedMethods.entrySet()) {
                        String operationName = opNameMethodEntry.getKey();
                        Method annotatedMethod = opNameMethodEntry.getValue();
                        operationNameVsObjMethodEntry.put(operationName,
                                new AbstractMap.SimpleEntry<>(object, annotatedMethod));
                    }
                }
                System.out.println("processed class : " + classInfo.getName() + "\n");
            }
            System.out.println("[Operation Weaver] Finished build associations between operation name and methods.");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Injector getInjector() {
        return injector;
    }

    public ImmutableAcyclicGraph getImmutableAcyclicGraph() {
        return immutableAcyclicGraph;
    }

    public HashBiMap<String, SimpleEntry<Object, Method>> getOperationNameVsObjMethodEntry() {
        return operationNameVsObjMethodEntry;
    }

    public HashBiMap<String, GraphNode> getOperationNameVsGraphNode() {
        return operationNameVsGraphNode;
    }

    public void proceedTraverse(WorkflowTraverse traverse, OperationCompletionMessage completionMsg,
            ExecutorService threadPool) {

        GraphNode correspondingNodeOfMsg = Preconditions.checkNotNull(
                operationNameVsGraphNode.get(completionMsg.getOperationName()),"Unable to find corresponding "
                        + "graphNode of this operation completion message :" + completionMsg.getOperationName());
        List<GraphNode> nextStepNodes = traverse.visit(correspondingNodeOfMsg, completionMsg);
        for (GraphNode nextStepNode : nextStepNodes) {
            // ATTENTION graphNodeId is operation name, which guaranteed in previous logic
            AbstractMap.SimpleEntry<Object, Method> objectMethod =
                    operationNameVsObjMethodEntry.get(nextStepNode.getGraphNodeId());
            if (objectMethod == null) {
                throw new NullPointerException("Cannot find corresponding using operation name : " +
                        nextStepNode.getGraphNodeId() + ", which is also a graphNodeId");
            }
            Object thisObject = objectMethod.getKey();
            Method method = objectMethod.getValue();
            threadPool.submit(() -> {
                try {
                    method.invoke(thisObject, traverse.determineArgsOf(method, nextStepNode));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }
    }

}
