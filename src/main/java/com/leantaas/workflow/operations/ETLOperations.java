package com.leantaas.workflow.operations;

import com.leantaas.workflow.operations.dto.FileUploadCompletionDTO;

/**
 * Created by boweiliu on 2/3/17.
 */
public interface ETLOperations {

  FileUploadCompletionDTO uploadFileOperation(String fileName);

  String processFileOperation(FileUploadCompletionDTO fileUploadCompletionDTO);
}
