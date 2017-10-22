package io.makerplayground.ui.canvas.event;

import io.makerplayground.project.NodeElement;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;

public class SceneEvent extends Event {
    public static final EventType<SceneEvent> ANY =
            new EventType<>(Event.ANY, "SCENE");
    public static final EventType<SceneEvent> SCENE_MOVED =
            new EventType<>(SceneEvent.ANY, "SCENE_MOVED");

    private final NodeElement sourceNode;

    private final double X;
    private final double Y;

    public SceneEvent(Node source, EventTarget target, EventType<? extends Event> eventType
            , NodeElement sourceNode, double X, double Y) {
        super(source, target, eventType);
        this.sourceNode = sourceNode;
        this.X = X;
        this.Y = Y;
    }

    @Override
    public Node getSource() {
        return (Node) super.getSource();
    }

    public NodeElement getSourceNode() {
        return sourceNode;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }
}
