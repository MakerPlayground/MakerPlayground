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

import io.makerplayground.project.DiagramError;
import io.makerplayground.ui.canvas.InteractivePane;
import io.makerplayground.ui.canvas.helper.DynamicViewCreator;
import io.makerplayground.ui.canvas.helper.DynamicViewCreatorBuilder;
import io.makerplayground.ui.canvas.node.usersetting.SceneDeviceIconView;
import io.makerplayground.ui.canvas.node.usersetting.SceneDeviceIconViewModel;
import io.makerplayground.ui.control.AutoResizeTextField;
import io.makerplayground.ui.dialog.devicepane.output.OutputDeviceSelector;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
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

public class SceneView extends InteractiveNode {
    private final VBox parent = new VBox();

    @FXML private AutoResizeTextField nameTextField;
    @FXML private ImageView removeButton;

    @FXML private HBox titleHBox;
    @FXML private HBox mainContent;
    @FXML private HBox mainLayout;
    @FXML private VBox contentPane;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox deviceConfigIconPane;
    @FXML private Button addDeviceButton;
    @FXML private Arc inPort;
    @FXML private Arc outPort;

    @FXML private Line hintLine;
    @FXML private Text hintText;

    private final SceneViewModel sceneViewModel;
    private OutputDeviceSelector outputDeviceSelector = null;
    private static final Color highlightColor = Color.web("#12cc69");

    public SceneView(SceneViewModel sceneViewModel, InteractivePane interactivePane) {
        super(interactivePane);
        this.sceneViewModel = sceneViewModel;
        initView();
        initEvent();
    }

    private void initView() {
        // initialize view from FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/SceneView.fxml"));
        fxmlLoader.setRoot(parent);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(parent);

        // TODO: refactor into InteractiveNode
        // bind port location to the model
        ChangeListener<Bounds> boundsChangeListener = (observable, oldValue, newValue) -> {
            if (getParent() != null) {
                Bounds inPortCanvasBound = getParent().sceneToLocal(inPort.localToScene(inPort.getBoundsInLocal()));
                sceneViewModel.sourcePortXProperty().set(inPortCanvasBound.getMinX());
                sceneViewModel.sourcePortYProperty().set(inPortCanvasBound.getCenterY());

                Bounds outPortCanvasBound = getParent().sceneToLocal(outPort.localToScene(outPort.getBoundsInLocal()));
                sceneViewModel.destPortXProperty().set(outPortCanvasBound.getMaxX());
                sceneViewModel.destPortYProperty().set(outPortCanvasBound.getCenterY());
            }
        };
        boundsInParentProperty().addListener(boundsChangeListener);
        inPort.boundsInParentProperty().addListener(boundsChangeListener);
        outPort.boundsInParentProperty().addListener(boundsChangeListener);

        // dynamically create device configuration icons
        DynamicViewCreator<VBox, SceneDeviceIconViewModel, SceneDeviceIconView> dynamicViewCreator =
                new DynamicViewCreatorBuilder<VBox, SceneDeviceIconViewModel, SceneDeviceIconView>()
                        .setParent(deviceConfigIconPane)
                        .setModelLoader(sceneViewModel.getDynamicViewModelCreator())
                        .setViewFactory(sceneDeviceIconViewModel -> {
                            SceneDeviceIconView sceneDeviceIconView = new SceneDeviceIconView(sceneDeviceIconViewModel);
                            sceneDeviceIconView.setOnRemoved(event ->
                                    sceneViewModel.removeUserSetting(sceneDeviceIconViewModel.getUserSetting()));
                            return sceneDeviceIconView;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(parent.getChildren().size() - 1, node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();

        // bind scene's name to the model
        nameTextField.setText(sceneViewModel.getName());
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> sceneViewModel.setName(newValue));

        // bind scene's location to the model
        translateXProperty().bindBidirectional(sceneViewModel.xProperty());
        translateYProperty().bindBidirectional(sceneViewModel.yProperty());

        // show add output device button when there are devices left to be added
        addDeviceButton.visibleProperty().bind(sceneViewModel.hasDeviceToAddProperty());
        addDeviceButton.managedProperty().bind(addDeviceButton.visibleProperty());

        showHilight(false);

        // update hilight when error property of the condition is changed
        sceneViewModel.getScene().errorProperty().addListener((observable, oldValue, newValue) -> showHilight(false));

        // display the remove button only when the scene is selected
        removeButton.visibleProperty().bind(selectedProperty());

        // install tooltip to display error message to the user
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(250));
        if (sceneViewModel.getError() != DiagramError.NONE) {
            tooltip.setText("Error: " + sceneViewModel.getError().toString());
            Tooltip.install(this, tooltip);
        }
        sceneViewModel.errorProperty().addListener((observable, oldValue, newValue) -> {
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

        BooleanBinding showBackToBegin = sceneViewModel.hasLineInProperty().and(sceneViewModel.hasLineOutProperty().not());

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
        makeMovableWithEventHandler(deviceConfigIconPane);
        // the ScrollBar normally consume mouse drag event but we want to allow dragging by drag on the scroll bar area
        // when it is invisible so we attach and remove event filter based on the number of device (JavaFX doesn't provide
        // native method to check the visibility of the scroll bar)
        if (sceneViewModel.getStateDevice().size() >= 3) {
            removeEventFilter(scrollPane);
        } else {
            makeMovableWithEventFilter(scrollPane);
        }
        Bindings.size(sceneViewModel.getStateDevice()).greaterThanOrEqualTo(3).addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                removeEventFilter(scrollPane);
            } else {
                makeMovableWithEventFilter(scrollPane);
            }
        });

        // update scene name after the text field lose focus
        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                sceneViewModel.setName(nameTextField.getText());
            }
        });

        // show device selector dialog to add device to this scene
        addDeviceButton.setOnAction(e -> {
            if (outputDeviceSelector != null) {
                outputDeviceSelector.hide();
            }
            OutputDeviceSelector outputDeviceSel = new OutputDeviceSelector(sceneViewModel);
            outputDeviceSel.show(addDeviceButton, 0);
            outputDeviceSelector = outputDeviceSel;
        });

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
                    , sceneViewModel.getScene(), null, sceneViewModel.getDestPortX(), sceneViewModel.getDestPortY()));
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null && !sceneViewModel.hasConnectionFrom(interactivePane.getSourceNode())) {
                showHilight(true);
            }
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , interactivePane.getSourceNode(), sceneViewModel.getScene(), 0, 0));
            }
        });

        inPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , null, sceneViewModel.getScene(), sceneViewModel.getSourcePortX(), sceneViewModel.getSourcePortY()));
        });

        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null && !sceneViewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHilight(true);
            }
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , sceneViewModel.getScene(), interactivePane.getDestNode(), 0, 0));
            }
        });

        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            if (interactivePane.getSourceNode() != null && !sceneViewModel.hasConnectionFrom(interactivePane.getSourceNode())) {
                showHilight(true);
            } else if (interactivePane.getDestNode() != null && !sceneViewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHilight(true);
            }
        });

        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));

        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null && interactivePane.getSourceNode() != this.getSceneViewModel().getScene()) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , interactivePane.getSourceNode(), sceneViewModel.getScene(), 0, 0));
            }
            if (interactivePane.getDestNode() != null && interactivePane.getDestNode() != this.getSceneViewModel().getScene()) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , sceneViewModel.getScene(), interactivePane.getDestNode(), 0, 0));
            }
        });
    }

    public SceneViewModel getSceneViewModel() {
        return sceneViewModel;
    }

    @Override
    protected boolean isError() {
        return sceneViewModel.getError() != DiagramError.NONE;
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
