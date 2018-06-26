package io.makerplayground.ui.canvas.event;

import io.makerplayground.project.NodeElement;
import io.makerplayground.ui.canvas.node.InteractiveNode;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class InteractiveNodeEvent extends Event {

    public static final EventType<InteractiveNodeEvent> ANY =
            new EventType<>(Event.ANY, "INTNODE");

    public static final EventType<InteractiveNodeEvent> MOVED =
            new EventType<>(InteractiveNodeEvent.ANY, "INTNODE_MOVED");
    public static final EventType<InteractiveNodeEvent> REMOVED =
            new EventType<>(InteractiveNodeEvent.ANY, "INTNODE_REMOVED");

    public static final EventType<InteractiveNodeEvent> CONNECTION_BEGIN =
            new EventType<>(InteractiveNodeEvent.ANY, "INTNODE_CONNECTION_BEGIN");
    public static final EventType<InteractiveNodeEvent> CONNECTION_CANCEL =
            new EventType<>(InteractiveNodeEvent.ANY, "INTNODE_CONNECTION_CANCEL");
    public static final EventType<InteractiveNodeEvent> CONNECTION_DONE =
            new EventType<>(InteractiveNodeEvent.ANY, "INTNODE_CONNECTION_DONE");

    private final NodeElement sourceNode;
    private final NodeElement destinationNode;

    private final double X;
    private final double Y;

    public InteractiveNodeEvent(InteractiveNode source, EventTarget target, EventType<? extends Event> eventType
            , NodeElement sourceNode, NodeElement destinationNode
            , double X, double Y) {
        super(source, target, eventType);
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.X = X;
        this.Y = Y;
    }

    @Override
    public InteractiveNode getSource() {
        return (InteractiveNode) super.getSource();
    }

    public NodeElement getSourceNode() {
        return sourceNode;
    }

    public NodeElement getDestinationNode() {
        return destinationNode;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }
}
