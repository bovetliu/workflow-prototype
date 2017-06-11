package org.opentechfin.workflow.kinesis;

import org.opentechfin.workflow.operations.dto.OperationCompletionMessage;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.inject.Singleton;

/**
 * just wrap one ArrayBlockingQueue has type parameter {@link OperationCompletionMessage}
 */
@Singleton
public class FakeKinesis {

  private BlockingQueue<OperationCompletionMessage> blockingQueue
      = new ArrayBlockingQueue<OperationCompletionMessage>(40);

  public FakeKinesis() {
  }

  public void put(OperationCompletionMessage msg) {
    try {

      blockingQueue.put(msg);
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public OperationCompletionMessage take() {
    try {
      return blockingQueue.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
