package org.opentechfin.workflow.weaver;

import com.amazonaws.util.StringUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.opentechfin.workflow.acyclicgraph.GraphNode;
import org.opentechfin.workflow.acyclicgraph.ImmutableAcyclicGraph;
import org.opentechfin.workflow.annotation.WorkflowOperation;
import org.opentechfin.workflow.flowdefinition.WorkflowArrangementEntry;
import org.opentechfin.workflow.guicemodule.WorkflowModule;
import org.opentechfin.workflow.operations.dto.OperationCompletionMessage;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import javax.inject.Singleton;

/**
 * This class is default implementation
 */
@Singleton
public class OperationWeaverImpl implements OperationWeaver {

  private final HashBiMap<String, SimpleEntry<Object, Method>> operationNameVsObjMethodEntry;

  private final HashBiMap<String, GraphNode> operationNameVsGraphNode;

  private final ImmutableAcyclicGraph immutableAcyclicGraph;

  private final Injector injector;

  // first I would like to hard code the package I need search
  public OperationWeaverImpl(List<WorkflowArrangementEntry> workflowArrangementEntryList) {
    if (workflowArrangementEntryList == null || workflowArrangementEntryList.isEmpty()) {
      throw new IllegalArgumentException("workflow arrangement should not be null or empty");
    }

    immutableAcyclicGraph = new ImmutableAcyclicGraph(
        WorkflowArrangementEntry.toGraphEdges(workflowArrangementEntryList));
    operationNameVsGraphNode = HashBiMap.create();
    for (GraphNode graphNode : immutableAcyclicGraph.getNodes()) {
      operationNameVsGraphNode.put(graphNode.getGraphNodeId(), graphNode);
    }
    WorkflowModule workflowModule = new WorkflowModule();
    injector = Guice.createInjector(workflowModule);

    operationNameVsObjMethodEntry = HashBiMap.create();
    try {
      ClassPath classPath = ClassPath.from(OperationWeaverImpl.class.getClassLoader());
      //TODO(Bowei) make it configurable
      ImmutableSet<ClassInfo> topLevelClasses =
          classPath.getTopLevelClasses(workflowModule.OPERATION_PACKAGE);
      // attempting to populate operationNameVsObjMethodEntry
      // it should match operationNameVsGraphNode
      for (ClassPath.ClassInfo classInfo : topLevelClasses) {
        Class<?> opClazz = classInfo.load();
        Map<String, Method> tempMapStoringAnnotatedMethods = new TreeMap<>();
        for (Method method : opClazz.getDeclaredMethods()) {
          WorkflowOperation wfOpAnnotation
              = method.getAnnotation(WorkflowOperation.class);
          if (wfOpAnnotation != null) {
            String operationName = StringUtils.isNullOrEmpty(wfOpAnnotation.operationName()) ?
                method.getName() : wfOpAnnotation.operationName();
            tempMapStoringAnnotatedMethods.put(operationName, method);
          }
        }
        // this class has WorkflowOperation annotated methods
        if (!tempMapStoringAnnotatedMethods.isEmpty()) {
          Object object = injector.getInstance(opClazz);
          for (Map.Entry<String, Method> opNameMethodEntry : tempMapStoringAnnotatedMethods.entrySet()) {
            String operationName = opNameMethodEntry.getKey();
            Method annotatedMethod = opNameMethodEntry.getValue();
            System.out.println("put into operationNameVsObjMethodEntry : " + operationName);
            operationNameVsObjMethodEntry.put(operationName,
                new AbstractMap.SimpleEntry<>(object, annotatedMethod));
          }
        }
        System.out.println("processed class : " + classInfo.getName() + "\n");
      }
      if (operationNameVsObjMethodEntry.size() != operationNameVsGraphNode.size()) {
        String errMsg = String.format("operationNameVsObjMethodEntry.size(): %d should be equal to "
            + "operationNameVsGraphNode.size(): %d", operationNameVsObjMethodEntry.size(),
            operationNameVsGraphNode.size());
        throw new IllegalStateException(errMsg);
      }
      if (!operationNameVsObjMethodEntry.keySet().stream().allMatch(
          operationNameVsGraphNode::containsKey)) {
        throw new IllegalStateException(
            "Keys do not match: operationNameVsObjMethodEntry vs operationNameVsGraphNode");
      }
      System.out.println("[Operation Weaver] Finished build associations between operation name and methods.");
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public Injector getInjector() {
    return injector;
  }

  @Override
  public ImmutableAcyclicGraph getImmutableAcyclicGraph() {
    return immutableAcyclicGraph;
  }

  public HashBiMap<String, SimpleEntry<Object, Method>> getOperationNameVsObjMethodEntry() {
    return operationNameVsObjMethodEntry;
  }

  public HashBiMap<String, GraphNode> getOperationNameVsGraphNode() {
    return operationNameVsGraphNode;
  }

  @Override
  public WorkflowTraverse createTraverse(String tenantCode, LocalDate localDate) {
    return new WorkflowTraverse(immutableAcyclicGraph, tenantCode, localDate);
  }

  @Override
  public void proceedTraverse(WorkflowTraverse traverse, OperationCompletionMessage completionMsg,
      ExecutorService threadPool) {

    GraphNode correspondingNodeOfMsg = Preconditions.checkNotNull(
        operationNameVsGraphNode.get(completionMsg.getOperationName()), "Unable to find corresponding "
            + "graphNode of this operation completion message :" + completionMsg.getOperationName());
    List<GraphNode> nextStepNodes = traverse.visit(correspondingNodeOfMsg, completionMsg);

    for (GraphNode nextStepNode : nextStepNodes) {
      // ATTENTION graphNodeId is operation name, which guaranteed in previous logic
      AbstractMap.SimpleEntry<Object, Method> objectMethod =
          operationNameVsObjMethodEntry.get(nextStepNode.getGraphNodeId());
      if (objectMethod == null) {
        throw new NullPointerException("Cannot find corresponding using operation name : " +
            nextStepNode.getGraphNodeId() + ", which is also a graphNodeId.");
      }
      Object thisObject = objectMethod.getKey();
      Method method = objectMethod.getValue();
      threadPool.submit(() -> {
        try {
          method.invoke(thisObject, traverse.determineArgsOf(method, nextStepNode));
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      });
    }
  }

}
