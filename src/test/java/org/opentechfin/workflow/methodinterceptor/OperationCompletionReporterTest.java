package org.opentechfin.workflow.methodinterceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class OperationCompletionReporterTest {


  private void testVoidReturnType() {
  }

  // just want to make sure Void.TYPE can be equal to method.getReturnType() when the method referenced is returning
  // void.
  @Test
  public void testVoidReturnTypeMethod()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    Method testPrintMethod = OperationCompletionReporterTest.class.getDeclaredMethod("testVoidReturnType");
    Assert.assertEquals(Void.TYPE, testPrintMethod.getReturnType());
    testPrintMethod.invoke(this);
  }

}
