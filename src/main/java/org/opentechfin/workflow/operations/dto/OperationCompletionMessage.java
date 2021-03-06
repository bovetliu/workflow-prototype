package org.opentechfin.workflow.operations.dto;


import org.opentechfin.workflow.utils.StringUtils;

/**
 */
public class OperationCompletionMessage {

  private String operationName;

  private Class<?> returnClazz;

  private Object returnedObject;


  /**
   * @param operationNameParam operation name
   * @param returnClazzParam runtime type of returned object
   * @param returnedObjectParam returned object
   */
  public OperationCompletionMessage(String operationNameParam, Class<?> returnClazzParam,
      Object returnedObjectParam) {
    if (StringUtils.isNullOrEmpty(operationNameParam)) {
      throw new IllegalArgumentException("operation name cannot be null or empty");
    }
    operationName = operationNameParam;
    returnClazz = returnClazzParam;
    returnedObject = returnedObjectParam;
  }

  public String getOperationName() {
    return operationName;
  }

  public Class<?> getReturnClazz() {
    return returnClazz;
  }

  public Object getReturnedObject() {
    return returnedObject;
  }
}
