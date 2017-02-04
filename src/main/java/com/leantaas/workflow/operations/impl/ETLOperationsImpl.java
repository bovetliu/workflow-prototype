package com.leantaas.workflow.operations;

import com.leantaas.workflow.operations.dto.FileUploadCompletionDTO;
import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import java.util.concurrent.BlockingQueue;

/**
 */
public class ETLOperations {

    private BlockingQueue<OperationCompletionMessage> kinesisSDK;

    public ETLOperations(BlockingQueue<OperationCompletionMessage> kinesisSDKParam) {
        kinesisSDK = kinesisSDKParam;
    }

    public void processfileOperation(OperationCompletionMessage fileUploadCompletion) {
        System.out.println(String.format("processing file uploaded by %s at url %s ",
                fileUploadCompletion.get("tenantName"), fileUploadCompletion.get("s3Url") ));
        String tenantName = fileUploadCompletion.get("tenantName");
        String s3Url = fileUploadCompletion.get("s3Url");
        try {
            System.out.println("download file going to take 2s ");
            Thread.sleep(2000);
            System.out.println("process file, going to take 2s.");
            Thread.sleep(2000);
            System.out.println("insert into mirror, going to take 2s");
            Thread.sleep(2000);
            System.out.println("finish processing uploaded file.");

            //following block will be done by method interceptor
            OperationCompletionMessage processFileCompletion = new OperationCompletionMessage("processfileOperation");
            processFileCompletion.put("tenantName", tenantName);
            processFileCompletion.put("fileMstrId", "dummy_file_mstr_id");
            kinesisSDK.put(processFileCompletion);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
