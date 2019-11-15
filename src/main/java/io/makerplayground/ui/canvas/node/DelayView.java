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

import io.makerplayground.device.shared.DelayUnit;
import io.makerplayground.project.DiagramError;
import io.makerplayground.ui.canvas.InteractivePane;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class DelayView extends InteractiveNode {
    private final VBox parent = new VBox();

    @FXML private ImageView removeButton;

    @FXML private HBox titleHBox;
    @FXML private HBox mainContent;
    @FXML private HBox mainLayout;
    @FXML private VBox contentPane;
    @FXML private TextField delayTextField;
    @FXML private ComboBox<DelayUnit> timeUnitComboBox;
    @FXML private Arc inPort;
    @FXML private Arc outPort;

    @FXML private Line hintLine;
    @FXML private Text hintText;

    private final DelayViewModel viewModel;
    private static final Color highlightColor = Color.web("#ffab00");
    private static final ObservableList<DelayUnit> delayUnitList = FXCollections.observableArrayList(List.of(DelayUnit.values()));
    private static final DecimalFormat df = new DecimalFormat("0.###");

    public DelayView(DelayViewModel viewModel, InteractivePane interactivePane) {
        super(interactivePane);
        this.viewModel = viewModel;
        initView();
        initEvent();
    }

    private void initView() {
        // initialize view from FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/DelayView.fxml"));
        fxmlLoader.setRoot(parent);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(parent);

        // initialize delay text field
        if (viewModel.getDelayValue() == 0) {
            delayTextField.setText("0");
        } else {
            delayTextField.setText(df.format(viewModel.getDelayValue()));
        }

        // initialize delay's unit combobox
        timeUnitComboBox.getItems().addAll(delayUnitList);
        timeUnitComboBox.getSelectionModel().selectFirst();

        // TODO: refactor into InteractiveNode
        // bind port location to the model
        ChangeListener<Bounds> boundsChangeListener = (observable, oldValue, newValue) -> {
            if (getParent() != null) {
                Bounds inPortCanvasBound = getParent().sceneToLocal(inPort.localToScene(inPort.getBoundsInLocal()));
                viewModel.sourcePortXProperty().set(inPortCanvasBound.getMinX());
                viewModel.sourcePortYProperty().set(inPortCanvasBound.getCenterY());

                Bounds outPortCanvasBound = getParent().sceneToLocal(outPort.localToScene(outPort.getBoundsInLocal()));
                viewModel.destPortXProperty().set(outPortCanvasBound.getMaxX());
                viewModel.destPortYProperty().set(outPortCanvasBound.getCenterY());
            }
        };
        boundsInParentProperty().addListener(boundsChangeListener);
        inPort.boundsInParentProperty().addListener(boundsChangeListener);
        outPort.boundsInParentProperty().addListener(boundsChangeListener);

        // bind scene's location to the model
        translateXProperty().bindBidirectional(viewModel.xProperty());
        translateYProperty().bindBidirectional(viewModel.yProperty());

        showHilight(false);

        // update hilight when error property of the condition is changed
        viewModel.getDelay().errorProperty().addListener((observable, oldValue, newValue) -> showHilight(false));

        // display the remove button only when the scene is selected
        removeButton.visibleProperty().bind(selectedProperty());

        // install tooltip to display error message to the user
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(250));
        if (viewModel.getError() != DiagramError.NONE) {
            tooltip.setText("Error: " + viewModel.getError().toString());
            Tooltip.install(this, tooltip);
        }
        viewModel.errorProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == DiagramError.NONE) {
                tooltip.setText("");
                Tooltip.uninstall(this, tooltip);
            } else {
                tooltip.setText("Error: " + newValue.toString());
                Tooltip.install(this, tooltip);
            }
        });

        titleHBox.maxWidthProperty().bind(mainContent.widthProperty());
        titleHBox.minWidthProperty().bind(mainContent.widthProperty());

        hintLine.startXProperty().bind(outPort.centerXProperty());
        hintLine.startYProperty().bind(outPort.centerYProperty());

        BooleanBinding showBackToBegin = viewModel.hasLineInProperty().and(viewModel.hasLineOutProperty().not());

        hintLine.endXProperty().bind(outPort.centerYProperty().add(50));
        hintLine.endYProperty().bind(outPort.centerYProperty());
        hintLine.visibleProperty().bind(showBackToBegin);

        hintText.xProperty().bind(outPort.centerXProperty().add(52));
        hintText.yProperty().bind(outPort.centerYProperty());
        hintText.visibleProperty().bind(showBackToBegin);
    }

    private void initEvent() {
        // allow node to be dragged
        makeMovableWithEventHandler(contentPane);

        // bind delay amount to the model (revert to old value if new value is invalid)
        delayTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                viewModel.setDelayValue(0);
            } else if (newValue.matches("\\d+\\.?\\d*")) {
                double delay = Double.parseDouble(newValue);
                viewModel.setDelayValue(delay);
            } else {
                delayTextField.setText(oldValue);
            }
        });
        delayTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (delayTextField.getText().isEmpty()) {
                delayTextField.setText("0");
            }
        });

        // bind unit to the model
        timeUnitComboBox.getSelectionModel().select(viewModel.getDelayUnit());
        viewModel.delayUnitProperty().bind(timeUnitComboBox.getSelectionModel().selectedItemProperty());

        // remove scene when press the remove button
        removeButton.setOnMousePressed(event -> fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.REMOVED
                , null, null, 0, 0)));

        // TODO: refactor into InteractiveNode
        // allow node to connect with other node
        outPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , viewModel.getDelay(), null, viewModel.getDestPortX(), viewModel.getDestPortY()));
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null && !viewModel.hasConnectionFrom(interactivePane.getSourceNode())) {
                showHilight(true);
            }
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , interactivePane.getSourceNode(), viewModel.getDelay(), 0, 0));
            }
        });

        inPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , null, viewModel.getDelay(), viewModel.getSourcePortX(), viewModel.getSourcePortY()));
        });

        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null && !viewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHilight(true);
            }
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , viewModel.getDelay(), interactivePane.getDestNode(), 0, 0));
            }
        });

        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            if (interactivePane.getSourceNode() != null && !viewModel.hasConnectionFrom(interactivePane.getSourceNode())) {
                showHilight(true);
            } else if (interactivePane.getDestNode() != null && !viewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHilight(true);
            }
        });

        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));

        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null && interactivePane.getSourceNode() != this.getDelayViewModel().getDelay()) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , interactivePane.getSourceNode(), viewModel.getDelay(), 0, 0));
            }
            if (interactivePane.getDestNode() != null && interactivePane.getDestNode() != this.getDelayViewModel().getDelay()) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , viewModel.getDelay(), interactivePane.getDestNode(), 0, 0));
            }
        });
    }

    public DelayViewModel getDelayViewModel() {
        return viewModel;
    }

    @Override
    protected boolean isError() {
        return viewModel.getError() != DiagramError.NONE;
    }

    @Override
    protected Node getHighlightNode() {
        return mainContent;
    }

    @Override
    protected Color getHighlightColor() {
        return highlightColor;
    }
}
