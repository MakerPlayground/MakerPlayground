/*
 * Copyright 2017 The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.canvas;

import io.makerplayground.project.Scene;
import io.makerplayground.ui.canvas.event.InteractiveNodeEvent;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewCreatorBuilder;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public class SceneView extends InteractiveNode{
    private final HBox parent = new HBox();
    @FXML private VBox statePane;
    @FXML private FlowPane activeIconFlowPane;
    @FXML private TextField nameTextField;
    @FXML private Arc inPort;
    @FXML private Arc outPort;
    @FXML private ImageView removeSceneBtn;
    @FXML private Button addOutputButton;

    private static final ObservableList<Scene.DelayUnit> delayUnitList =
            FXCollections.observableArrayList(List.of(Scene.DelayUnit.values()));

    private final SceneViewModel sceneViewModel;
    private OutputDeviceSelector outputDeviceSelector = null;

    public SceneView(SceneViewModel sceneViewModel, InteractivePane interactivePane) {
        super(interactivePane);
        this.sceneViewModel = sceneViewModel;
        initView();
        initEvent();
    }

    private void initView() {
        // initialize view from FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StateView.fxml"));
        fxmlLoader.setRoot(parent);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().add(parent);

        // dynamically create device configuration icons
        DynamicViewCreator<FlowPane, SceneDeviceIconViewModel, SceneDeviceIconView> dynamicViewCreator =
                new DynamicViewCreatorBuilder<FlowPane, SceneDeviceIconViewModel, SceneDeviceIconView>()
                        .setParent(activeIconFlowPane)
                        .setModelLoader(sceneViewModel.getDynamicViewModelCreator())
                        .setViewFactory(sceneDeviceIconViewModel -> {
                            SceneDeviceIconView sceneDeviceIconView = new SceneDeviceIconView(sceneDeviceIconViewModel);
                            sceneDeviceIconView.setOnRemoved(event ->
                                    sceneViewModel.removeStateDevice(sceneDeviceIconViewModel.getProjectDevice()));
                            return sceneDeviceIconView;
                        })
                        .setNodeAdder((parent, node) -> parent.getChildren().add(parent.getChildren().size() - 1, node))
                        .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                        .createDynamicViewCreator();

        // bind scene's name to the model
        nameTextField.textProperty().bindBidirectional(sceneViewModel.nameProperty());

        // bind scene's location to the model
        translateXProperty().bindBidirectional(sceneViewModel.xProperty());
        translateYProperty().bindBidirectional(sceneViewModel.yProperty());

        // show add output device button when there are devices left to be added
        addOutputButton.visibleProperty().bind(sceneViewModel.hasDeviceToAddProperty());

        // this is need to indicate error for empty condition
        showHilight(false);
    }

    private void initEvent() {
        // update scene name after the text field lose focus
        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                sceneViewModel.setName(nameTextField.getText());
            }
        });

        // show device selector dialog to add device to this scene
        addOutputButton.setOnAction(e -> {
            if (outputDeviceSelector != null) {
                outputDeviceSelector.hide();
            }
            OutputDeviceSelector outputDeviceSel = new OutputDeviceSelector(sceneViewModel);
            outputDeviceSel.show(addOutputButton, 0);
            outputDeviceSelector = outputDeviceSel;
        });

        // remove scene when press the remove button
        removeSceneBtn.setOnMousePressed(event -> fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.REMOVED
                , null, null, 0, 0)));

        // allow node to be dragged
        makeMovable(statePane);
        makeMovable(activeIconFlowPane);

        // TODO: refactor into InteractiveNode
        // allow node to connect with other node
        outPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , sceneViewModel.getScene(), null
                    , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (outPort.getBoundsInLocal().getWidth() / 2)
                    , getBoundsInParent().getMinY() + (outPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + (outPort.getBoundsInLocal().getHeight() / 2)));
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
                        , interactivePane.getSourceNode(), sceneViewModel.getScene()
                        , getBoundsInParent().getMinX() + (inPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (inPort.getBoundsInLocal().getWidth() / 2)
                        , getBoundsInParent().getMinY() + (inPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + (inPort.getBoundsInLocal().getHeight() / 2)));
            }
        });

        inPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new InteractiveNodeEvent(this, null, InteractiveNodeEvent.CONNECTION_BEGIN
                    , null, sceneViewModel.getScene()
                    , getBoundsInParent().getMinX() + (inPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                    + (inPort.getBoundsInLocal().getWidth() / 2)
                    , getBoundsInParent().getMinY() + (inPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                    + (inPort.getBoundsInLocal().getHeight() / 2)));
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
                        , sceneViewModel.getScene(), interactivePane.getDestNode()
                        , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (outPort.getBoundsInLocal().getWidth() / 2)
                        , getBoundsInParent().getMinY() + (outPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + (outPort.getBoundsInLocal().getHeight() / 2)));
            }
        });
    }

    public SceneViewModel getSceneViewModel() {
        return sceneViewModel;
    }

    @Override
    protected boolean isError() {
        return sceneViewModel.isError();
    }
}
