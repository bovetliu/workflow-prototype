package org.opentechfin.workflow.operations.impl;

import org.opentechfin.workflow.annotation.WorkflowOperation;
import org.opentechfin.workflow.operations.MetricsOperations;
import javax.inject.Inject;

/**
 * In {@link org.opentechfin.workflow.guicemodule.WorkflowModule}, this class is configured to be
 * singleton instance of interface MetricsOperations
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
      System.out.println("start consuming file_mstr_id " + fileMstrId + " to compute huddle for "
          + "tenant uchealth");
      Thread.sleep(1000);
      System.out.println("finished consuming file_mstr_id " + fileMstrId);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return 78;
  }
}
