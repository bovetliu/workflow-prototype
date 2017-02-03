package com.leantaas.workflow.acyclicgraph;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * This
 */
public class ImmutableGraphNode extends GraphNode {

    protected boolean isSolidified;

    protected ImmutableSet<GraphNode> immutableParentNodes;

    protected ImmutableSet<GraphNode> immutableChildNodes;

    protected ImmutableGraphNode(GraphNode graphNode) {
        super(graphNode.getGraphNodeId());
        isSolidified = false;
    }

    // getGraphNodeId() is inherited

    @Override
    public ImmutableSet<GraphNode> getParentNodes() {
        return immutableParentNodes;
    }

    @Override
    public ImmutableSet<GraphNode> getChildNodes() {
        return immutableChildNodes;
    }

    @Override
    public void setParentNodes(Set<GraphNode> parentNodes) {
        throw new UnsupportedOperationException("ImmutableGraphNode does not accept setting parentNodes.");
    }

    @Override
    public boolean addParentNode(GraphNode graphNode) {
        throw new UnsupportedOperationException("ImmutableGraphNode does not accept adding parentNode.");
    }

    @Override
    public void setChildNodes(Set<GraphNode> childNodes) {
        throw new UnsupportedOperationException("ImmutableGraphNode does not accept setting childNodes.");
    }

    @Override
    public boolean addChildNode(GraphNode childNode) {
        throw new UnsupportedOperationException("ImmutableGraphNode does not accept add childNode.");
    }

    protected void solidify() {
        if (isSolidified) {
            throw new RuntimeException("this node is already solidified");
        }
        immutableChildNodes = ImmutableSet.copyOf(childNodes);
        immutableParentNodes = ImmutableSet.copyOf(parentNodes);
        isSolidified = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImmutableGraphNode)) {
            return false;
        }
        ImmutableGraphNode that = (ImmutableGraphNode) o;
        if (! (getGraphNodeId().equals(that.getGraphNodeId())) ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
