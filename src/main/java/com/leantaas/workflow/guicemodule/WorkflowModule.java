package com.leantaas.workflow.guicemodule;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.leantaas.workflow.annotation.WorkflowOperation;
import com.leantaas.workflow.kinesis.FakeKinesis;
import com.leantaas.workflow.methodinterceptor.OperationCompletionReporter;
import com.leantaas.workflow.operations.ETLOperations;
import com.leantaas.workflow.operations.MetricsOperations;
import com.leantaas.workflow.operations.impl.ETLOperationsImpl;
import com.leantaas.workflow.operations.impl.MetricsOperationsImpl;

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
        bindInterceptor(Matchers.inPackage(Package.getPackage("com.leantaas.workflow.operations.impl")),
                Matchers.annotatedWith(WorkflowOperation.class),
                reporter);
    }
}
