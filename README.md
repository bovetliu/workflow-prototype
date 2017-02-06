# Workflow Prototype

This is the workflow prototype. The project would like to provide a centralized workflow management. 
User can compose and monitor workflow execution. To do this, user need a set of `@WorkflowOperation`s,
and (TODO bowei) a repository storing workflow dependency graph, and traverse state.

## Installation

This is a maven project. Do it maven way.

## Usage

```
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
//                ex.printStackTrace();
            }
        } finally {
            // we have finished traverse, shutdown thread pool
            workerThreadPool.shutdown();
            timedTrigger.shutdownNow();
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