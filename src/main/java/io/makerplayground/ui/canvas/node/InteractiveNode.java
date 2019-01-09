/*
 * Copyright (c) 2018. The Maker Playground Authors.
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

import io.makerplayground.ui.canvas.InteractivePane;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public abstract class InteractiveNode extends Group implements Selectable {

    protected final InteractivePane interactivePane;
    private final BooleanProperty select = new SimpleBooleanProperty();
    private Node hilightNode;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private boolean hasDragged;

    public InteractiveNode(InteractivePane interactivePane) {
        this.interactivePane = interactivePane;
        // node to show hilight to indicate selection or error
        this.hilightNode = this;

        // allow this node to be selected (use event filter without consuming the event to allow children of this
        // node to process mouse press event)
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.isPrimaryButtonDown()) {
                if (interactivePane.getSelectionGroup().isMultipleSelection()) {
                    select.set(!select.get());
                } else {
                    select.set(true);
                }
            }
        });
        // consume mouse pressed event in the event handler so that we can differentiate between mouser press in the node
        // and mouser press in the pane i.e. pressing inside the node will not trigger MOUSE_PRESSED event of the pane
        addEventHandler(MouseEvent.MOUSE_PRESSED, Event::consume);

        // show/hide hi-light when this scene is selected/deselected
        select.addListener((observable, oldValue, newValue) -> showHilight(newValue));
    }

    protected void makeMovable(Node n) {
        // allow node to be dragged
        n.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            // allow dragging only when the left button is pressed
            if (!event.isPrimaryButtonDown())
                return;

            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            translateAnchorX = getTranslateX();
            translateAnchorY = getTranslateY();

            hasDragged = true;
        });
        n.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            // allow dragging only when the left button is pressed and this node is being selected
            if (!event.isPrimaryButtonDown() || !isSelected())
                return;

            // we should set the mouse anchor here to ensure correct drag behaviour in case that MOUSE_DRAGGED fired
            // without MOUSE_PRESSED for example when we close the property dialog by pressing at the empty space
            // of the selected scene
            if (!hasDragged) {
                mouseAnchorX = event.getSceneX();
                mouseAnchorY = event.getSceneY();
                translateAnchorX = getTranslateX();
                translateAnchorY = getTranslateY();
                hasDragged = true;
            }

            double deltaX = ((event.getSceneX() - mouseAnchorX) / interactivePane.getScale());
            double deltaY = ((event.getSceneY() - mouseAnchorY) / interactivePane.getScale());

            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();

            event.consume();

            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.MOVED, null, null
                    , deltaX, deltaY));
        });
        n.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            hasDragged = false;
        });
    }

    public void moveNode(double deltaX, double deltaY) {
        setTranslateX(getTranslateX() + deltaX);
        setTranslateY(getTranslateY() + deltaY);
    }

    /**
     * Show green hilight when node is selected otherwise hilight in red if error is found in the node
     * @param forceHilight true to show green hilight regardless of the selection property otherwise the node will be
     *                     hilighted in green when it is selected, red when {@link this.isError} is true and black
     *                     (default shadow effect only) otherwise
     */
    protected void showHilight(boolean forceHilight) {
        if (forceHilight || isSelected()) {
            hilightNode.setStyle("-fx-effect: dropshadow(gaussian, #008ef4, 5.0 , 0.5, 0.0 , 0.0);");
            /*setStyle("-fx-effect: dropshadow(gaussian, #5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");*/
        } else if (isError()) {
            hilightNode.setStyle("-fx-effect: dropshadow(gaussian, #ff0000, 5.0 , 0.5, 0.0 , 0.0);");
            /*setStyle("-fx-effect: dropshadow(gaussian, #c25a5a, 15.0 , 0.5, 0.0 , 0.0);");*/
        } else {
            hilightNode.setStyle("-fx-effect: dropshadow(gaussian, derive(black,85%), 5.0 , 0.0, 0.0 , 0.0);");
            /*setStyle("-fx-effect: dropshadow(gaussian, derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");*/
        }
    }

    @Override
    public BooleanProperty selectedProperty() {
        return select;
    }

    @Override
    public boolean isSelected() {
        return select.get();
    }

    @Override
    public void setSelected(boolean b) {
        select.set(b);
    }

    public Node getHilightNode() {
        return hilightNode;
    }

    public void setHilightNode(Node hilightNode) {
        this.hilightNode = hilightNode;
    }

    protected abstract boolean isError();
}

