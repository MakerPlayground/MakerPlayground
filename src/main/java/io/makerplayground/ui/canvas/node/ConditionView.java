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
import io.makerplayground.ui.canvas.node.usersetting.ConditionDeviceIconView;
import io.makerplayground.ui.canvas.node.usersetting.SceneDeviceIconViewModel;
import io.makerplayground.ui.control.AutoResizeTextField;
import io.makerplayground.ui.dialog.devicepane.input.InputDeviceSelector;
import javafx.beans.binding.Bindings;
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
import javafx.util.Duration;

import java.io.IOException;

public class ConditionView extends InteractiveNode {
    private VBox root = new VBox();

    @FXML private AutoResizeTextField nameTextField;
    @FXML private ImageView removeButton;

    @FXML private HBox mainLayout;
    @FXML private VBox contentPane;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox deviceConfigIconPane;
    @FXML private Button addDeviceButton;
    @FXML private Arc inPort;
    @FXML private Arc outPort;

    private final ConditionViewModel conditionViewModel;
    private InputDeviceSelector inputDeviceSelector = null;
    private static final Color highlightColor = Color.web("#ffab00");

    public ConditionView(ConditionViewModel conditionViewModel, InteractivePane interactivePane) {
        super(interactivePane);
        this.conditionViewModel = conditionViewModel;
        initView();
        initEvent();
    }

    private void initView() {
        // initialize view from FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/node/ConditionView3.fxml"));
        fxmlLoader.setRoot(root);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(root);

        // TODO: refactor into InteractiveNode
        // bind port location to the model
        boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
            if (getParent() != null) {
                Bounds inPortCanvasBound = getParent().sceneToLocal(inPort.localToScene(inPort.getBoundsInLocal()));
                conditionViewModel.sourcePortXProperty().set(inPortCanvasBound.getMinX());
                conditionViewModel.sourcePortYProperty().set(inPortCanvasBound.getCenterY());

                Bounds outPortCanvasBound = getParent().sceneToLocal(outPort.localToScene(outPort.getBoundsInLocal()));
                conditionViewModel.destPortXProperty().set(outPortCanvasBound.getMaxX());
                conditionViewModel.destPortYProperty().set(outPortCanvasBound.getCenterY());
            }
        });

        // dynamically create device configuration icons
        DynamicViewCreator<VBox, SceneDeviceIconViewModel, ConditionDeviceIconView> dynamicViewCreator =
                new DynamicViewCreatorBuilder<VBox, SceneDeviceIconViewModel, ConditionDeviceIconView>()
                        .setParent(deviceConfigIconPane)
                        .setModelLoader(conditionViewModel.getDeviceViewModelCreator())
                        .setViewFactory(conditionDeviceIconViewModel -> {
                            ConditionDeviceIconView conditionDeviceIconView = new ConditionDeviceIconView(conditionDeviceIconViewModel);
                            conditionDeviceIconView.setOnRemove(event -> conditionViewModel.removeUserSetting(conditionDeviceIconViewModel.getUserSetting()));
                            return conditionDeviceIconView;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(parent.getChildren().size() - 1, node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();

        DynamicViewCreator<VBox, SceneDeviceIconViewModel, ConditionDeviceIconView> dynamicViewCreator2 =
                new DynamicViewCreatorBuilder<VBox, SceneDeviceIconViewModel, ConditionDeviceIconView>()
                        .setParent(deviceConfigIconPane)
                        .setModelLoader(conditionViewModel.getVirtualDeviceViewModelCreator())
                        .setViewFactory(conditionDeviceIconViewModel -> {
                            ConditionDeviceIconView conditionDeviceIconView = new ConditionDeviceIconView(conditionDeviceIconViewModel);
                            conditionDeviceIconView.setOnRemove(event -> conditionViewModel.removeUserSetting(conditionDeviceIconViewModel.getUserSetting()));
                            return conditionDeviceIconView;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(parent.getChildren().size() - 1, node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();

        // bind condition's name to the model
        nameTextField.setText(conditionViewModel.getName());
        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> conditionViewModel.setName(newValue));

        // bind condition's location to the model
        translateXProperty().bindBidirectional(conditionViewModel.xProperty());
        translateYProperty().bindBidirectional(conditionViewModel.yProperty());

        // show add output device button when there are devices left to be added
        addDeviceButton.visibleProperty().bind(conditionViewModel.hasDeviceToAddProperty());
        addDeviceButton.managedProperty().bind(addDeviceButton.visibleProperty());

        showHilight(false);

        // update hilight when error property of the condition is changed
        conditionViewModel.getCondition().errorProperty().addListener((observable, oldValue, newValue) -> showHilight(false));

        // show remove button when select
        removeButton.visibleProperty().bind(selectedProperty());

        // install tooltip to display error message to the user
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(Duration.millis(250));
        if (conditionViewModel.getError() != DiagramError.NONE) {
            tooltip.setText("Error: " + conditionViewModel.getError().toString());
            Tooltip.install(this, tooltip);
        }
        conditionViewModel.errorProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == DiagramError.NONE) {
                tooltip.setText("");
                Tooltip.uninstall(this, tooltip);
            } else {
                tooltip.setText("Error: " + newValue.toString());
                Tooltip.install(this, tooltip);
            }
        });
    }

    private void initEvent() {
        // allow node to be dragged
        makeMovableWithEventHandler(contentPane);
        makeMovableWithEventHandler(deviceConfigIconPane);
        // the ScrollBar normally consume mouse drag event but we want to allow dragging by drag on the scroll bar area
        // when it is invisible so we attach and remove event filter based on the number of device (JavaFX doesn't provide
        // native method to check the visibility of the scroll bar)
        if (conditionViewModel.getDeviceSetting().size() >= 3) {
            removeEventFilter(scrollPane);
        } else {
            makeMovableWithEventFilter(scrollPane);
        }
        Bindings.size(conditionViewModel.getDeviceSetting()).greaterThanOrEqualTo(3).addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                removeEventFilter(scrollPane);
            } else {
                makeMovableWithEventFilter(scrollPane);
            }
        });

        // update scene name after the text field lose focus
        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                conditionViewModel.setName(nameTextField.getText());
            }
        });

        // show device selector dialog to add device to this condition
        addDeviceButton.setOnAction(e -> {
            if (inputDeviceSelector != null) {
                inputDeviceSelector.hide();
            }
            InputDeviceSelector inputDeviceSel = new InputDeviceSelector(conditionViewModel);
            inputDeviceSel.show(addDeviceButton,0);
            inputDeviceSelector = inputDeviceSel;
        });

        // remove condition when press the remove button
        removeButton.setOnMousePressed(event -> fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.REMOVED
                , null, null, 0, 0)));

        // TODO: refactor into InteractiveNode
        // allow node to connect with other node
        outPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , conditionViewModel.getCondition(), null, conditionViewModel.getDestPortX(), conditionViewModel.getDestPortY()));
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null && !conditionViewModel.hasConnectionFrom(interactivePane.getSourceNode())) {
                showHilight(true);
            }
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null && interactivePane.getSourceNode() != this.getConditionViewModel().getCondition()) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , interactivePane.getSourceNode(), conditionViewModel.getCondition(), 0, 0));
            }
        });

        inPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , null, conditionViewModel.getCondition(), conditionViewModel.getSourcePortX(), conditionViewModel.getSourcePortY()));
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            // highlight our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null && !conditionViewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHilight(true);
            }
        });
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));
        outPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our outPort if mouse is being dragged from other inPort
            if (interactivePane.getDestNode() != null && interactivePane.getDestNode() != this.getConditionViewModel().getCondition()) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , conditionViewModel.getCondition(), interactivePane.getDestNode(), 0, 0));
            }
        });


        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> {
            if (interactivePane.getSourceNode() != null && !conditionViewModel.hasConnectionFrom(interactivePane.getSourceNode())) {
                showHilight(true);
            }
            else if (interactivePane.getDestNode() != null && !conditionViewModel.hasConnectionTo(interactivePane.getDestNode())) {
                showHilight(true);
            }
        });

        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));

        contentPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            // allow drop to our inPort if mouse is being dragged from other outPort
            if (interactivePane.getSourceNode() != null && interactivePane.getSourceNode() != this.getConditionViewModel().getCondition()) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , interactivePane.getSourceNode(), conditionViewModel.getCondition(), 0, 0));
            }
            if (interactivePane.getDestNode() != null && interactivePane.getDestNode() != this.getConditionViewModel().getCondition()) {
                showHilight(false);
                fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_DONE
                        , conditionViewModel.getCondition(), interactivePane.getDestNode(), 0, 0));
            }
        });
    }

    public ConditionViewModel getConditionViewModel() {
        return conditionViewModel;
    }

    @Override
    protected boolean isError() {
        return conditionViewModel.getError() != DiagramError.NONE;
    }

    @Override
    protected Node getHighlightNode() {
        return mainLayout;
    }

    @Override
    protected Color getHighlightColor() {
        return highlightColor;
    }
}
