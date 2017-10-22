package io.makerplayground.ui.canvas.event;

import io.makerplayground.project.NodeElement;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class ConnectionEvent extends Event {

    public static final EventType<ConnectionEvent> ANY =
            new EventType<>(Event.ANY, "CONNECTION");
    public static final EventType<ConnectionEvent> CONNECTION_BEGIN =
            new EventType<>(ConnectionEvent.ANY, "CONNECTION_BEGIN");
    public static final EventType<ConnectionEvent> CONNECTION_CANCEL =
            new EventType<>(ConnectionEvent.ANY, "CONNECTION_CANCEL");
    public static final EventType<ConnectionEvent> CONNECTION_DONE =
            new EventType<>(ConnectionEvent.ANY, "CONNECTION_DONE");

    private final NodeElement sourceNode;
    private final NodeElement destinationNode;

    private final double X;
    private final double Y;

    public ConnectionEvent(Object source, EventTarget target, EventType<? extends Event> eventType
            , NodeElement sourceNode, NodeElement destinationNode
            , double X, double Y) {
        super(source, target, eventType);
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.X = X;
        this.Y = Y;
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
