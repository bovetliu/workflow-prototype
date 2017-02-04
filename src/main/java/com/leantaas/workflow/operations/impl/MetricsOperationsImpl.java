package com.leantaas.workflow.operations.impl;

import com.leantaas.workflow.annotation.WorkflowOperation;
import com.leantaas.workflow.operations.MetricsOperations;
import javax.inject.Inject;

/**
 *
 */
public class MetricsOperationsImpl implements MetricsOperations {

    @Inject
    public MetricsOperationsImpl() {
    }

    @Override
    @WorkflowOperation
    public Integer computeHuddleVolumeOperation(String fileMstrId) {
        System.out.println("Entering huddle volume operation, processing fileMstrId : \""
                + fileMstrId + "\"");
        try {
            System.out.println("start consuming file_mstr_id " + fileMstrId + " to compute huddle for tenant uchealth" );
            Thread.sleep(1000);
            System.out.println("finished consuming file_mstr_id " + fileMstrId);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return 78;
    }
}
