package com.leantaas.workflow.operations;

import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import java.util.concurrent.BlockingQueue;

/**
 *
 */
public class MetricsOperations {

    private BlockingQueue<OperationCompletionMessage> kinesisSDK;

    public MetricsOperations(BlockingQueue<OperationCompletionMessage> kinesisSDKParam) {
        kinesisSDK = kinesisSDKParam;
    }

    public void computeHuddleVolumeOperation(OperationCompletionMessage fileProcessingCompletion) {
        String fileMstrId = fileProcessingCompletion.get("fileMstrId");
        String tenantName = fileProcessingCompletion.get("tenantName");

        try {
            System.out.println("start consuming file_mstr_id " + fileMstrId + " to compute huddle for tenant " + tenantName);
            Thread.sleep(1000);

            // following method will be executed by method interceptor
            OperationCompletionMessage computeHuddleComplete = new OperationCompletionMessage("computeHuddleVolumeOperation");
            computeHuddleComplete.put("tenantName", tenantName);
            computeHuddleComplete.put("huddleVolume", "89");
            System.out.println(String.format("finished compute huddle for tenant %s, huddle volume is %s", tenantName, "89"));
            kinesisSDK.put(computeHuddleComplete);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
