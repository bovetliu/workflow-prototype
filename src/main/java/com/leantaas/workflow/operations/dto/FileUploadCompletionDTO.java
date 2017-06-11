package com.leantaas.workflow.operations.dto;

import com.amazonaws.util.StringUtils;

/**
 * Created by boweiliu on 2/3/17.
 */
public class FileUploadCompletionDTO {

  private String tenantName;
  private String fileS3Url;

  public FileUploadCompletionDTO(String tenantNameParam, String fileS3UrlParam) {
    if (StringUtils.isNullOrEmpty(tenantNameParam)) {
      throw new IllegalArgumentException("tenantName cannot be null or empty");
    }
    if (StringUtils.isNullOrEmpty(fileS3UrlParam)) {
      throw new IllegalArgumentException("fileS3UrlParam cannot be null or empty");
    }
    tenantName = tenantNameParam;
    fileS3Url = fileS3UrlParam;
  }


  public String getTenantName() {
    return tenantName;
  }

  public void setTenantName(String tenantName) {
    this.tenantName = tenantName;
  }

  public String getFileS3Url() {
    return fileS3Url;
  }

  public void setFileS3Url(String fileS3Url) {
    this.fileS3Url = fileS3Url;
  }
}
