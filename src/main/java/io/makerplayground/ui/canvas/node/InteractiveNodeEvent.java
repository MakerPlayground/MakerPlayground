/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.canvas.node;

import io.makerplayground.project.NodeElement;
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

    private final double x;
    private final double y;

    public InteractiveNodeEvent(InteractiveNode source, EventTarget target, EventType<? extends Event> eventType
            , NodeElement sourceNode, NodeElement destinationNode
            , double X, double Y) {
        super(source, target, eventType);
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.x = X;
        this.y = Y;
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
        return x;
    }

    public double getY() {
        return y;
    }
}
