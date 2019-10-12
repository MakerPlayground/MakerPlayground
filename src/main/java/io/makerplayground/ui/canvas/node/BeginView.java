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

import io.makerplayground.project.Begin;
import io.makerplayground.ui.canvas.InteractivePane;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;

import java.io.IOException;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class BeginView extends InteractiveNode {
    private final VBox beginVBox = new VBox();
    @FXML private Arc outPort;
    @FXML private Button removeBeginBtn;
    @FXML private Label labelHBox;
    @FXML private Pane labelPane;

    private final BeginViewModel beginViewModel;

    public BeginView(BeginViewModel beginViewModel, InteractivePane interactivePane) {
        super(interactivePane);
        this.beginViewModel = beginViewModel;

        // initialize view from FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/BeginScene.fxml"));
        fxmlLoader.setRoot(beginVBox);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(beginVBox);
        makeMovable(labelHBox);

        // bind begin's location to the model
        translateXProperty().bindBidirectional(beginViewModel.xProperty());
        translateYProperty().bindBidirectional(beginViewModel.yProperty());

        // show remove button when select
        removeBeginBtn.visibleProperty().bind(selectedProperty().and(getBegin().getBeginCountBinding().greaterThan(1)));
        // remove condition when press the remove button
        removeBeginBtn.setOnMousePressed(event -> fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.REMOVED
                , null, null, 0, 0)));

        // TODO: refactor into InteractiveNode
        // allow node to connect with other node
        outPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , beginViewModel.getBegin(), null
                    , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                    + (outPort.getBoundsInLocal().getWidth() / 2)
                    , getBoundsInParent().getMinY() + (labelPane.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                    + outPort.getBoundsInParent().getMinY() + (outPort.getBoundsInLocal().getHeight() / 2)));
        });

        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , beginViewModel.getBegin(), interactivePane.getDestNode()
                        , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (outPort.getBoundsInLocal().getWidth() / 2)
                        , getBoundsInParent().getMinY() + (outPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + (outPort.getBoundsInLocal().getHeight() / 2)));
            }
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null && !beginViewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHilight(true);
            }
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));

        // TODO: Consume the event to avoid the interactive pane from accepting it and deselect every node
        setOnMousePressed(Event::consume);

        // this is need to indicate error for non connected begin node
        showHilight(false);
    }

    @Override
    protected boolean isError() {
        return beginViewModel.isError();
    }

    public Begin getBegin() {
        return this.beginViewModel.getBegin();
    }
}
