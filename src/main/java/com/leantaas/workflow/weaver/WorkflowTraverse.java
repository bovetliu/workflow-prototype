package com.leantaas.workflow.weaver;

import com.amazonaws.util.StringUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.leantaas.workflow.acyclicgraph.AcyclicGraphTraverse;
import com.leantaas.workflow.acyclicgraph.GraphNode;
import com.leantaas.workflow.acyclicgraph.ImmutableAcyclicGraph;
import com.leantaas.workflow.annotation.ReturnedFrom;
import com.leantaas.workflow.operations.dto.OperationCompletionMessage;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * WorkflowTraverse is used only inside package
 */
public class WorkflowTraverse extends AcyclicGraphTraverse {

    private final String tenantCode;

    private final LocalDate localDate;

    private final Map<String, OperationCompletionMessage> operationCompletionMap;


    @VisibleForTesting
    public WorkflowTraverse(ImmutableAcyclicGraph fakeImmutableAcyclicGraph) {
        super(fakeImmutableAcyclicGraph);
        GraphNode node1 = new GraphNode();
        GraphNode node2 = new GraphNode();
        tenantCode = "onlyForTest";
        localDate = LocalDate.MIN;
        operationCompletionMap = null;
    }

    // this constructor should only be used package
    public WorkflowTraverse(ImmutableAcyclicGraph immutableAcyclicGraphParam, String tenantCodeParam,
            LocalDate localDateParam) {
        super(immutableAcyclicGraphParam);

        if (StringUtils.isNullOrEmpty(tenantCodeParam)) {
            throw new IllegalArgumentException("TenantCode parameter cannot be  null or empty.");
        }
        Objects.requireNonNull(localDateParam, "localDate parameter cannot be null");
        tenantCode = tenantCodeParam;
        localDate = localDateParam;

        operationCompletionMap = new HashMap<>();
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }



    public Optional<OperationCompletionMessage> getResultOfOperation(String operationName) {
        OperationCompletionMessage msg = operationCompletionMap.get(operationName);

        // The reason I check operationCompletionMap.containsKey is that, some operatioin method might complete with
        // returning null.
        if (msg == null && !operationCompletionMap.containsKey(operationName)) {
            return Optional.empty();
        }
        return Optional.ofNullable(msg);
    }

    @Override
    public List<GraphNode> visit(GraphNode graphNode) {
        throw new UnsupportedOperationException("In workflowTraverse implementation, please use "
                + "visit(GraphNode, OperationCompletionMessage)");
    }

    public List<GraphNode> visit(GraphNode graphNode, OperationCompletionMessage msg) {
        Objects.requireNonNull(graphNode, "graphNode cannot be null");
        Objects.requireNonNull(msg, "msg cannot be null");
        operationCompletionMap.put(msg.getOperationName(), msg);
        return super.visit(graphNode);
    }

    /**
     * This method should be private. To be able to test this method, I changed accessibility modifier to be default
     * @param method method
     * @param msgsOfParents OperationCompletionMessage keyed by operation name.
     * @return array of arguments.
     */
    @VisibleForTesting
    Object[] determineArgsOf(Method method, Map<String, OperationCompletionMessage> msgsOfParents) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) {
            return new Object[0];
        }

        if (msgsOfParents.size() < parameters.length) {
            throw new IllegalStateException(String.format("Non-void-return upstream operation number is "
                    + "%d, while parameter number of this method is %d. MsgOfParents cannot be smaller than number "
                    + "of parameters", msgsOfParents.size(), parameters.length));
        }

        boolean alreadyMatched = false;
        for (OperationCompletionMessage op : msgsOfParents.values()) {
            for (Parameter parameter : parameters) {
                if (parameter.isAnnotationPresent(ReturnedFrom.class)) {
                    continue;
                }
                // now parameter is not annotated by @ReturnFrom
                if (parameter.getType().isAssignableFrom(op.getReturnClazz())) {
                    if (alreadyMatched) {
                        String exMsg = String.format("The return type of upstream operation : %s can be assigned "
                                + "to multiple parameters of method : %s", op.getOperationName(), method.getName());
                        throw new IllegalStateException(exMsg);
                    }
                    alreadyMatched = true;
                }
            }
            alreadyMatched = false;
        }

        Object[] result = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter candidateParam = parameters[i];
            ReturnedFrom returnedFrom = candidateParam.getAnnotation(ReturnedFrom.class);
            if (returnedFrom != null) {
                result[i] = Preconditions.checkNotNull(msgsOfParents.get(returnedFrom.operationName()),
                        String.format("annotated return-from operation \"%s\" is not among upstream operations of "
                                + "this operation", returnedFrom.operationName()));
                msgsOfParents.remove(returnedFrom.operationName());
            } else {
                // have to match one by one.
                Class<?> parameterType = candidateParam.getType();
                String operationNameMatching = null;
                for (Map.Entry<String, OperationCompletionMessage> entry : msgsOfParents.entrySet()) {
                    if (parameterType.isAssignableFrom(entry.getValue().getReturnClazz())) {
                        if (operationNameMatching == null) {
                            operationNameMatching = entry.getKey();
                        } else {
                            String errorMsg = String.format("Cannot infer parameter \"%s\" of method \"%s\" returned"
                                            + " from which upstream operation. Both \"%s\" and \"%s\" return "
                                            + "assignable type.",
                                    candidateParam.getName(), method.getName(), entry.getKey(),
                                    operationNameMatching);
                            throw new IllegalStateException(errorMsg);
                        }
                    }
                }
                if (operationNameMatching == null) {
                    throw new IllegalStateException("None of non-void-return upstream operation returns assignable "
                            + "type for parameter : \"" + candidateParam.getName() + "\" of method : \"" +
                            method.getName() + "\"");
                }
                result[i] = msgsOfParents.remove(operationNameMatching).getReturnedObject();
            }
        }
        return result;
    }

    Object[] determineArgsOf(Method method, GraphNode correspondingNode) {
        Map<String, OperationCompletionMessage> msgsOfParents = correspondingNode.getParentNodes().stream().map(node -> {
            Optional<OperationCompletionMessage> operationCompletionMessageOptional = getResultOfOperation(node.getGraphNodeId());
            if (!operationCompletionMessageOptional.isPresent()) {
                throw new IllegalStateException("prerequisites not fulfilled when trying determining arguments for "
                        + "method : " + method.getName());
            }
            return operationCompletionMessageOptional.get();
        }).filter(msg -> !msg.getReturnClazz().equals( Void.TYPE)).collect(Collectors.toMap(
                OperationCompletionMessage::getOperationName, msg -> msg));
        return determineArgsOf(method, msgsOfParents);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkflowTraverse)) {
            return false;
        }

        WorkflowTraverse that = (WorkflowTraverse) o;

        if (tenantCode != null ? !tenantCode.equals(that.tenantCode) : that.tenantCode != null) {
            return false;
        }
        return localDate != null ? localDate.equals(that.localDate) : that.localDate == null;
    }

    @Override
    public int hashCode() {
        int result = tenantCode != null ? tenantCode.hashCode() : 0;
        result = 31 * result + (localDate != null ? localDate.hashCode() : 0);
        return result;
    }

    // TODO(bowei)
    private void persistTraverse() {
        System.out.println("I need to implement workflow persist behavior: visitedMap hashCode" + visitedMap.hashCode());
    }


}
