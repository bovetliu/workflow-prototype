package com.leantaas.workflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by boweiliu on 2/4/17.
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
public @interface ReturnedFrom {

    // required field
    String operationName();
}
