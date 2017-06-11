package org.opentechfin.workflow.guicemodule;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import org.opentechfin.workflow.annotation.WorkflowOperation;
import org.opentechfin.workflow.kinesis.FakeKinesis;
import org.opentechfin.workflow.methodinterceptor.OperationCompletionReporter;
import org.opentechfin.workflow.operations.ETLOperations;
import org.opentechfin.workflow.operations.MetricsOperations;
import org.opentechfin.workflow.operations.impl.ETLOperationsImpl;
import org.opentechfin.workflow.operations.impl.MetricsOperationsImpl;

/**
 * Dependency resolution
 */
public class WorkflowModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ETLOperations.class).to(ETLOperationsImpl.class).asEagerSingleton();
    bind(MetricsOperations.class).to(MetricsOperationsImpl.class).asEagerSingleton();
    bind(FakeKinesis.class).asEagerSingleton();

    OperationCompletionReporter reporter = new OperationCompletionReporter();
    requestInjection(reporter);

    // Method interceptor, limited search range
    bindInterceptor(Matchers.inPackage(Package.getPackage("org.opentechfin.workflow.operations.impl")),
        Matchers.annotatedWith(WorkflowOperation.class),
        reporter);
  }
}
