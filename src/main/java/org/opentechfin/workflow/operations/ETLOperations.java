package org.opentechfin.workflow.operations;

import org.opentechfin.workflow.operations.dto.FileUploadCompletionDTO;

/**
 * Created by boweiliu on 2/3/17.
 */
public interface ETLOperations {

  FileUploadCompletionDTO uploadFileOperation(String fileName);

  String processFileOperation(FileUploadCompletionDTO fileUploadCompletionDTO);
}
