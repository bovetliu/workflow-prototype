package com.leantaas.workflow;

import com.leantaas.workflow.kinesis.FakeKinesis;
import com.leantaas.workflow.operations.ETLOperations;
import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import com.leantaas.workflow.weaver.OperationWeaver;
import com.leantaas.workflow.weaver.OperationWeaverImpl;
import com.leantaas.workflow.weaver.WorkflowArrangementEntry;
import com.leantaas.workflow.weaver.WorkflowTraverse;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class App {


    private static final String UPLOAD_FILE_OPERATION = "uploadFileOperation";
    private static final String PROCESS_FILE_OPERATION = "processfileOperation";
    private static final String COMPUTE_HUDDLE_OP_METHOD_NAME = "computeHuddleVolumeOperation";


    private static List<WorkflowArrangementEntry> mockFetchWorkflowArrangementFromServer() {

        WorkflowArrangementEntry fileUploadToFileProcess =
                new WorkflowArrangementEntry(UPLOAD_FILE_OPERATION, PROCESS_FILE_OPERATION);
        WorkflowArrangementEntry fileProcessToComputeHuddle =
                new WorkflowArrangementEntry(PROCESS_FILE_OPERATION, COMPUTE_HUDDLE_OP_METHOD_NAME);
        return Arrays.asList(fileUploadToFileProcess, fileProcessToComputeHuddle);
    }

    public static void main(String[] args) {
        // we should provide one thread pool for our workflow
        ExecutorService workerThreadPool = Executors.newFixedThreadPool(3);

        // we provide one timed trigger to simulate some event happened at some time
        ScheduledExecutorService timedTrigger = Executors.newScheduledThreadPool(1);

        try {
            // create our weaver, weaver fetched workflow arrangement from server
            OperationWeaver workflowWeaver = new OperationWeaverImpl(mockFetchWorkflowArrangementFromServer());
            System.out.println("finished initializing workflowWeaver. Program starts...\n\n");
            // Suppose tenant will upload file in 2 seconds.
            timedTrigger.schedule(() -> {
                ETLOperations etlOperations = workflowWeaver.getInjector().getInstance(ETLOperations.class);
                etlOperations.uploadFileOperation("demo-file.csv");
            }, 2, TimeUnit.SECONDS);

            // create one traverse for tenant : "testTenant", this is a traverse of 2017-02-19
            WorkflowTraverse workflowTraverse = workflowWeaver.createTraverse("testTenant", LocalDate.of(2017, 2, 19));

            // let me prepare the message queue I am going to read
            FakeKinesis fakeKinesis = workflowWeaver.getInjector().getInstance(FakeKinesis.class);

            // start consuming message queue.
            while (!workflowTraverse.isTraverseFinished()) {
                // blocking take, if no message in queue, following line of code will block
                OperationCompletionMessage operationCompletionMessage = fakeKinesis.take();
                // now we have the message, we proceed traverse using this completion message
                workflowWeaver.proceedTraverse(workflowTraverse, operationCompletionMessage, workerThreadPool);
            }

        } catch (Exception ex) {
            synchronized(App.class) {
                ex.printStackTrace();
            }
        } finally {
            // we have finished traverse, shutdown thread pool
            workerThreadPool.shutdown();
            timedTrigger.shutdownNow();
        }
    }
}
