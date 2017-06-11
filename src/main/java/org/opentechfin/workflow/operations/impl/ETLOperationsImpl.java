package org.opentechfin.workflow.operations.impl;

import org.opentechfin.workflow.annotation.WorkflowOperation;
import org.opentechfin.workflow.operations.ETLOperations;
import org.opentechfin.workflow.operations.dto.FileUploadCompletionDTO;
import javax.inject.Inject;

/**
 */
public class ETLOperationsImpl implements ETLOperations {

  @Inject
  public ETLOperationsImpl() {
  }

  /**
   * demo purpose file uploading operation method
   *
   * @param fileName file name
   * @return {@link FileUploadCompletionDTO} which has tenant_name and url as instance properties.
   */
  @Override
  @WorkflowOperation
  public FileUploadCompletionDTO uploadFileOperation(String fileName) {
    System.out.println("Started uploading file: " + fileName);
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    String url = "s3://abcdefghigjlimn12312313.awss3url.amazon.com/uploaded_file/" + fileName;
    System.out.println("File uploaded to:  " + url);
    return new FileUploadCompletionDTO("uchealth", url);
  }

  /**
   * One demo purpose operation  method
   *
   * @param fileUploadCompletionDTO dto containing tenant_name and url
   * @return file_mstr_id
   */
  @Override
  @WorkflowOperation
  public String processFileOperation(FileUploadCompletionDTO fileUploadCompletionDTO) {
    System.out.println(String.format("processing file uploaded by %s at url %s ",
        fileUploadCompletionDTO.getTenantName(), fileUploadCompletionDTO.getFileS3Url()));
    String tenantName = fileUploadCompletionDTO.getTenantName();
    String s3Url = fileUploadCompletionDTO.getFileS3Url();
    try {
      System.out.println("download file from url: " + s3Url);
      Thread.sleep(2000);
      System.out.println("process file, going to take 2s.");
      Thread.sleep(2000);
      System.out.println("insert into mirror, going to take 2s");
      Thread.sleep(2000);
      System.out.println("finish processing uploaded file.");

    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    String fileMstrId = "1231232-12fa12e-vzxvxas-12312v-aefasdfas";
    System.out.println("file processed, file_mstr_id: " + fileMstrId);
    return fileMstrId;
  }
}
