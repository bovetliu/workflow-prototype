package com.leantaas.workflow.weaver;

import com.leantaas.workflow.acyclicgraph.AcyclicGraphTraverseTest;
import com.leantaas.workflow.acyclicgraph.GraphNode;
import com.leantaas.workflow.annotation.ReturnedFrom;
import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;

/**
 */
@SuppressWarnings("WeakerAccess")
public class WorkflowTraverseTest {



    public String methodCreatedForDetermineArgsOfTest(Integer integerParamter, String stringParameter) {
        // this method is created only for test
        return integerParamter + stringParameter;
    }


    public Integer methodCreatedForDetermineArgsOfTest02(Integer integerParamter01) {
        // this method is created only for test
        return integerParamter01;
    }

    // this method is created to test two unannotated parameter will trigger exception when determineArgsOf
    public Integer methodCreatedForDetermineArgsOfTest03(Integer integerParamter1, Integer integerParam2) {
        // this method is created only for test
        return integerParamter1 + integerParam2;
    }

    // used in
    public Integer methodCreatedForDetermineArgsOfTest04(
            @ReturnedFrom(operationName = "junitOperation01") Integer integerParam1,
            @ReturnedFrom(operationName = "junitOperation02") Integer integerParam2) {
        // this method is created only for test
        return integerParam1 + integerParam2;
    }

    @Test
    public void determineArgsOfTest() throws NoSuchMethodException {
        // arrange
        Exception occurred = null;

        final String integerOperation = "integerOperation";
        final String stringOperation = "stringOperation";

        OperationCompletionMessage integerOperationMsg = new OperationCompletionMessage(integerOperation,
                Integer.class, 10);
        OperationCompletionMessage stringOperationMsg = new OperationCompletionMessage(stringOperation,
                String.class, "ten");

        List<GraphNode> listOfNodes = IntStream.range(0, 20).mapToObj(i -> new GraphNode())
                .collect(Collectors.toList());
        WorkflowTraverse workflowTraverse = new WorkflowTraverse(
                AcyclicGraphTraverseTest.quickBuildOneGraph(listOfNodes));
        HashMap<String, OperationCompletionMessage> fakeUpStreamOpCompletion = new HashMap<>();
        fakeUpStreamOpCompletion.put(integerOperationMsg.getOperationName(), integerOperationMsg);
        fakeUpStreamOpCompletion.put(stringOperationMsg.getOperationName(), stringOperationMsg);


        // action
        //methodCreatedForDetermineArgsOfTest(Integer, String)
        Method methodUnderTest = WorkflowTraverseTest.class.getDeclaredMethod(
                "methodCreatedForDetermineArgsOfTest", Integer.class, String.class);
        Object[] res = workflowTraverse.determineArgsOf(methodUnderTest, fakeUpStreamOpCompletion);
        Assert.assertEquals(2, res.length);
        Assert.assertEquals(10, res[0]);
        Assert.assertEquals("ten", res[1]);

        //methodCreatedForDetermineArgsOfTest02(Integer)
        fakeUpStreamOpCompletion.clear();
        methodUnderTest = WorkflowTraverseTest.class.getDeclaredMethod("methodCreatedForDetermineArgsOfTest02",
                Integer.class);
        try {
            workflowTraverse.determineArgsOf(methodUnderTest, fakeUpStreamOpCompletion);
        } catch (IllegalStateException ise) {
            Assert.assertEquals("Non-void-return upstream operation number is 0, while parameter number of "
                    + "this method is 1. MsgOfParents cannot be smaller than number of parameters", ise.getMessage());
            occurred = ise;
        }
        Assert.assertNotNull("above determineArgsOf is expected to throw exception, but did not", occurred);
        occurred = null;  // reset flag

        // action : test upstream operation return not assignable type
        fakeUpStreamOpCompletion.put(stringOperationMsg.getOperationName(), stringOperationMsg);
        try {
            workflowTraverse.determineArgsOf(methodUnderTest, fakeUpStreamOpCompletion);
        } catch (IllegalStateException ise) {
            occurred = ise;
            Assert.assertTrue(ise.getMessage().contains("None of non-void-return upstream operation returns "
                    + "assignable type for parameter "));
        }
        Assert.assertNotNull("above determineArgsOf is expected to throw exception, but did not", occurred);
    }


    @Test
    public void determineArgsOfTest02() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // arrange
        Exception occurred = null;

        final String integerOperation = "integerOperation";
        final String stringOperation = "stringOperation";
        final String doubleOperation = "doubleOperation";
        OperationCompletionMessage integerOperationMsg = new OperationCompletionMessage(integerOperation,
                Integer.class, 10);
        OperationCompletionMessage stringOperationMsg = new OperationCompletionMessage(stringOperation,
                String.class, "ten");
        OperationCompletionMessage doubleOperationMsg = new OperationCompletionMessage(doubleOperation,
                Double.class, 0.13d);

        WorkflowTraverse workflowTraverse = new WorkflowTraverse(
                AcyclicGraphTraverseTest.quickBuildOneGraph());
        HashMap<String, OperationCompletionMessage> fakeUpStreamOpCompletion = new HashMap<>();
        fakeUpStreamOpCompletion.put(integerOperationMsg.getOperationName(), integerOperationMsg);
        fakeUpStreamOpCompletion.put(stringOperationMsg.getOperationName(), stringOperationMsg);

        //methodCreatedForDetermineArgsOfTest(Integer, String)
        Method methodUnderTest = WorkflowTraverseTest.class.getDeclaredMethod(
                "methodCreatedForDetermineArgsOfTest03", Integer.class, Integer.class);
        try {
            workflowTraverse.determineArgsOf(methodUnderTest, fakeUpStreamOpCompletion);
        } catch (IllegalStateException ise) {
            occurred = ise;
            Assert.assertTrue(ise.getMessage().contains("The return type of upstream operation : integerOperation "
                    + "can be assigned to multiple parameters of method "));
        }
        Assert.assertNotNull("above determineArgsOf is expected to throw exception, but did not", occurred);
        occurred = null;  // reset flag


        //methodCreatedForDetermineArgsOfTest(Integer, String)
        // test that if upstream contains more operations than number of parameters,
        methodUnderTest = WorkflowTraverseTest.class.getDeclaredMethod(
                "methodCreatedForDetermineArgsOfTest", Integer.class, String.class);
        fakeUpStreamOpCompletion.clear();
        fakeUpStreamOpCompletion.put(integerOperationMsg.getOperationName(), integerOperationMsg);
        fakeUpStreamOpCompletion.put(stringOperationMsg.getOperationName(), stringOperationMsg);
        fakeUpStreamOpCompletion.put(doubleOperationMsg.getOperationName(), doubleOperationMsg);
        Object[] res = workflowTraverse.determineArgsOf(methodUnderTest, fakeUpStreamOpCompletion);
        Assert.assertEquals(2, res.length);
        Assert.assertEquals(10, res[0]);
        Assert.assertEquals("ten", res[1]);
        Assert.assertEquals("10ten", methodUnderTest.invoke(this, res));
    }

    @Test
    public void determineArgsOfTest03() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String junitOperation01 = "junitOperation01";
        final String junitOperation02 = "junitOperation02";
        final String junitOperation03 = "junitOperation03";
        OperationCompletionMessage junitOperation01Msg = new OperationCompletionMessage(junitOperation01,
                Integer.class, 10);
        OperationCompletionMessage junitOperation02Msg = new OperationCompletionMessage(junitOperation02,
                Integer.class, 15);
        OperationCompletionMessage junitOperation03Msg = new OperationCompletionMessage(junitOperation03,
                Integer.class, 30);
        WorkflowTraverse workflowTraverse = new WorkflowTraverse(
                AcyclicGraphTraverseTest.quickBuildOneGraph());

        HashMap<String, OperationCompletionMessage> fakeUpStreamOpCompletion = new HashMap<>();
        fakeUpStreamOpCompletion.put(junitOperation01Msg.getOperationName(), junitOperation01Msg);
        fakeUpStreamOpCompletion.put(junitOperation02Msg.getOperationName(), junitOperation02Msg);

        //methodCreatedForDetermineArgsOfTest04
        Method methodUnderTest = WorkflowTraverseTest.class.getDeclaredMethod(
                "methodCreatedForDetermineArgsOfTest04", Integer.class, Integer.class);
        Object[] args = workflowTraverse.determineArgsOf(methodUnderTest, fakeUpStreamOpCompletion);
        Assert.assertEquals(2, args.length);
        Assert.assertEquals(10, args[0]);
        Assert.assertEquals(15, args[1]);
        // test invocation
        Assert.assertEquals(25, methodUnderTest.invoke(this, args));

        fakeUpStreamOpCompletion.clear();
        fakeUpStreamOpCompletion.put(junitOperation01Msg.getOperationName(), junitOperation01Msg);
        fakeUpStreamOpCompletion.put(junitOperation02Msg.getOperationName(), junitOperation02Msg);
        fakeUpStreamOpCompletion.put(junitOperation03Msg.getOperationName(), junitOperation03Msg);
        args = workflowTraverse.determineArgsOf(methodUnderTest, fakeUpStreamOpCompletion);
        Assert.assertEquals(2, args.length);
        Assert.assertEquals(10, args[0]);
        Assert.assertEquals(15, args[1]);
        // test invocation
        Assert.assertEquals(25, methodUnderTest.invoke(this, args));


        // test unnable to assign exception
        Exception shouldCatch = null;
        junitOperation02Msg = new OperationCompletionMessage(junitOperation02, String.class, "203");
        fakeUpStreamOpCompletion.clear();
        fakeUpStreamOpCompletion.put(junitOperation01Msg.getOperationName(), junitOperation01Msg);
        fakeUpStreamOpCompletion.put(junitOperation02Msg.getOperationName(), junitOperation02Msg);
        try {
            workflowTraverse.determineArgsOf(methodUnderTest, fakeUpStreamOpCompletion);
        } catch (IllegalArgumentException ise) {
            shouldCatch = ise;
            Assert.assertEquals("Operation \"junitOperation02\" returned "
                    + "java.lang.String, which cannot be assigned to type \"java.lang.Integer\".", ise.getMessage());
        }
        Assert.assertNotNull(shouldCatch);
    }


}
