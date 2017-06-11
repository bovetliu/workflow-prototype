package com.leantaas.workflow.weaver;

import com.google.inject.Injector;
import com.leantaas.workflow.acyclicgraph.ImmutableAcyclicGraph;
import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;

/**
 * Created by boweiliu on 2/4/17.
 */
public interface OperationWeaver {

  Injector getInjector();

  ImmutableAcyclicGraph getImmutableAcyclicGraph();

  WorkflowTraverse createTraverse(String tenantCode, LocalDate localDate);

  void proceedTraverse(WorkflowTraverse traverse, OperationCompletionMessage completionMsg,
      ExecutorService threadPool);
}
