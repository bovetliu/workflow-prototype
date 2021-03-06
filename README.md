# Workflow Prototype

This is the workflow prototype. The project would like to provide a centralized workflow management. 
User can compose and monitor workflow execution. To do this, user need a set of `@WorkflowOperation`s,
and (TODO bowei) a repository storing workflow dependency graph, and traverse state.

## Installation

This is a maven project. Do it maven way.

## How it works

1. In application starting phase, in the logic of `WorkflowModule`, `OperationCompletionInterceptor` intercepts completions of 
  invocations of all the methods annotated by `WorkflowOperation`. Put the returned objects, 
  methodCompletionDTOs, into a queue, `FakeKinesis`. In this phase, instances and bindings btw 
  implementation and interfaces will be created.  
2. When application is started, by supplying a list of `WorkflowArrangement`, an `OperationWeaver`
  is created. The weaver stores: a mapping between operation names (java method names) and 
  Entry<impl object instance, `java.lang.reflect.Method`>;
  a mapping between operation names (java method names) and `GraphNode`.
  Map<operation name, `GraphNode`> should match the corresponding Map<operation name, Entry<impl object instance, `java.lang.reflect.Method`>>
  Map sizes are the same. Keys are the same.
3. A list of `WorkflowArrangement`s is an list of edges stating that
  "If operation1 is completed, operation3 can be started if all requirements are met"
  "If operation2 is completed, operation3 can be started if all requirements are met".
  They are just directed edges in an acyclic graph. An acyclic graph will be created based
  on these workflow arrangements in the `OperationWeaver`
4. Use `OperationWeaver` to create a `WorkflowTraverse`.
5. Invoke all methods whose indegrees are zero. Their returned object will be put into the queue,
  by the `OperationCompletionInterceptor`.
6. keep checking `workflowTraverse.isTraverseFinished()`, if it has not finished, 
  take an `OperationCompletionMessage` from the queue, and proceed the traverse by calling
  `workflowWeaver.proceedTraverse(workflowTraverse, 
                       operationCompletionMessage, 
                       workerThreadPool)`. 
## Sample Usage

```java
public class Demo {
    public static void main(String[] args) {
        // we should provide one thread pool for our workflow
        ExecutorService workerThreadPool = Executors.newFixedThreadPool(3);

        // we provide one timed trigger to simulate some event happened at some time
        ScheduledExecutorService timedTrigger = Executors.newScheduledThreadPool(1);

        try {
            // create our weaver, weaver fetched workflow arrangement from server
            OperationWeaver workflowWeaver = 
            new OperationWeaverImpl(mockFetchWorkflowArrangementFromServer());
            System.out.println("finished initializing workflowWeaver. Program starts...\n\n");
            // Suppose tenant will upload file in 2 seconds.
            timedTrigger.schedule(() -> {
                ETLOperations etlOperations = workflowWeaver.getInjector()
                        .getInstance(ETLOperations.class);
                        etlOperations.uploadFileOperation("demo-file.csv");
            }, 2, TimeUnit.SECONDS);

            // create one traverse for tenant : "testTenant", this is a traverse of 2017-02-19
            WorkflowTraverse workflowTraverse = 
            workflowWeaver.createTraverse("testTenant", LocalDate.of(2017, 2, 19));
    
            // let me prepare the message queue I am going to read
            FakeKinesis fakeKinesis = workflowWeaver.getInjector()
                    .getInstance(FakeKinesis.class);

            // start consuming message queue.
            while (!workflowTraverse.isTraverseFinished()) {
                // blocking take, if no message in queue, following line of code will block
                OperationCompletionMessage operationCompletionMessage = fakeKinesis.take();
                // now we have the message, we proceed traverse using this completion message
                workflowWeaver.proceedTraverse(workflowTraverse, 
                    operationCompletionMessage, 
                    workerThreadPool);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // we have finished traverse, shutdown thread pool
            workerThreadPool.shutdown();
            timedTrigger.shutdownNow();
        }
    }
}
```

## Contributing

TODO

## History

TODO: Write history

## Credits

TODO: Write credits

## License

TODO: Write license