package com.leantaas.workflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface WorkflowOperation {

    // if operationName is not defined, then method name will be operation name
    String operationName() default "";

}