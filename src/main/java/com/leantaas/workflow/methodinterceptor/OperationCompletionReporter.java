package com.leantaas.workflow.methodinterceptor;

import com.amazonaws.util.StringUtils;
import com.leantaas.workflow.annotation.WorkflowOperation;
import com.leantaas.workflow.kinesis.FakeKinesis;
import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import java.lang.reflect.Method;
import javax.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class OperationCompletionReporter implements MethodInterceptor {

    @Inject
    private FakeKinesis fakeKinesis;


    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object res = methodInvocation.proceed();

        String simpleClassName = methodInvocation.getMethod().getDeclaringClass().getSimpleName();
        Method method = methodInvocation.getMethod();
        WorkflowOperation workflowOperation = methodInvocation.getMethod().getAnnotation(WorkflowOperation.class);
        String operationName = StringUtils.isNullOrEmpty(workflowOperation.operationName()) ? method.getName()
                : workflowOperation.operationName();
        Class<?> returnClazz = method.getReturnType();
        System.out.println(
                "[Invocation Intercepting] operation completion reporting for " + simpleClassName + "." + method
                        .getName() + "(...)\n");
        OperationCompletionMessage msg = new OperationCompletionMessage(operationName, returnClazz, res);
        fakeKinesis.put(msg);
        return res;
    }
}
