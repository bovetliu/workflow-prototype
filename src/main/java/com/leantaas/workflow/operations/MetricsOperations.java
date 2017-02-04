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
            Thread.sleep(300);
            OperationCompletionMessage computeHuddleComplete = new OperationCompletionMessage("computeHuddleVolumeOperation");
            computeHuddleComplete.put("tenantName", tenantName);
            computeHuddleComplete.put("huddleVolume", "89");
            kinesisSDK.put(computeHuddleComplete);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
