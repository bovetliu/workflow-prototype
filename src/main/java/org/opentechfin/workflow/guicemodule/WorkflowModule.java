package org.opentechfin.workflow.guicemodule;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Objects;
import org.opentechfin.workflow.annotation.WorkflowOperation;
import org.opentechfin.workflow.kinesis.FakeKinesis;
import org.opentechfin.workflow.methodinterceptor.OperationCompletionInterceptor;
import org.opentechfin.workflow.operations.ETLOperations;
import org.opentechfin.workflow.operations.MetricsOperations;
import org.opentechfin.workflow.operations.impl.ETLOperationsImpl;
import org.opentechfin.workflow.operations.impl.MetricsOperationsImpl;

/**
 * Dependency resolution
 */
public class WorkflowModule extends AbstractModule {

  public final String OPERATION_PACKAGE;
  public final Config config;


  public WorkflowModule() {
    config = ConfigFactory.load("application.conf");
    config.getString("workflow-prototype-config.OPERATION_PACKAGE");
    OPERATION_PACKAGE = config.getString("workflow-prototype-config.OPERATION_PACKAGE");
  }

  @Override
  protected void configure() {
    super.bind(ETLOperations.class).to(ETLOperationsImpl.class).asEagerSingleton();
    super.bind(MetricsOperations.class).to(MetricsOperationsImpl.class).asEagerSingleton();
    super.bind(FakeKinesis.class).asEagerSingleton();

    // Create in instance of the MethodInterceptor
    OperationCompletionInterceptor reporter = new OperationCompletionInterceptor();
    super.requestInjection(reporter);

    // Method interceptor, limited search range to impl package of
    // org.opentechfin.workflow.operations
    Package thePackage = Objects.requireNonNull(Package.getPackage(OPERATION_PACKAGE),
        "Cannot get package : " + OPERATION_PACKAGE);
    super.bindInterceptor(
        Matchers.inPackage(thePackage),
        Matchers.annotatedWith(WorkflowOperation.class),
        reporter);
  }
}
