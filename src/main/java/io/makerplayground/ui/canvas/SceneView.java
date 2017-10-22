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
import io.makerplayground.ui.canvas.event.ConnectionEvent;
import io.makerplayground.ui.canvas.event.SceneEvent;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewCreatorBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
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
public class SceneView extends HBox implements Selectable {
    @FXML private VBox statePane;
    @FXML private FlowPane activeIconFlowPane;
    @FXML private TextField nameTextField;
    @FXML private TextField delayTextField;
    @FXML private Arc inPort;
    @FXML private Arc outPort;
    @FXML private Button removeSceneBtn;
    @FXML private ComboBox<Scene.DelayUnit> timeUnitComboBox;
    @FXML private Button addOutputButton;

    private static final ObservableList<Scene.DelayUnit> delayUnitList =
            FXCollections.observableArrayList(List.of(Scene.DelayUnit.values()));

    private final SceneViewModel sceneViewModel;
    private final InteractivePane interactivePane;
    private final BooleanProperty select = new SimpleBooleanProperty();
    private OutputDeviceSelector outputDeviceSelector = null;

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double translateAnchorX;
    private double translateAnchorY;
    private boolean hasDragged;

    public SceneView(SceneViewModel sceneViewModel, InteractivePane interactivePane) {
        this.sceneViewModel = sceneViewModel;
        this.interactivePane = interactivePane;
        initView();
        initEvent();
    }

    private void initView() {
        // initialize view from FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StateView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        showHilight(false);
        // TODO: tobe removed
        // setStyle("-fx-background-color: blue; -fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");

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

        // initialize delay's unit combobox
        timeUnitComboBox.getItems().addAll(delayUnitList);
        timeUnitComboBox.getSelectionModel().selectFirst();

        // bind scene's name to the model
        nameTextField.textProperty().bindBidirectional(sceneViewModel.nameProperty());

        // bind scene's location to the model
        translateXProperty().bindBidirectional(sceneViewModel.xProperty());
        translateYProperty().bindBidirectional(sceneViewModel.yProperty());

        // bind delay amount to the model
        Bindings.bindBidirectional(delayTextField.textProperty(), sceneViewModel.delayProperty()
                , new NumberStringConverter());

        // bind delay unit (bindBidirectional is not available)
        // TODO: combobox won't change if unit is changed elsewhere
        timeUnitComboBox.getSelectionModel().select(sceneViewModel.getDelayUnit());
        sceneViewModel.delayUnitProperty().bind(timeUnitComboBox.getSelectionModel().selectedItemProperty());

        // show add output device button when there are devices left to be added
        addOutputButton.visibleProperty().bind(sceneViewModel.hasDeviceToAddProperty());

        // show/hide hi-light when this scene is selected/deselected
        select.addListener((observable, oldValue, newValue) -> showHilight(newValue));
    }

    private void initEvent() {
        // update scene name after the text field lose focus
        nameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                sceneViewModel.setName(nameTextField.getText());
            }
        });

        // allow this node to be selected
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> select.set(true));

        // show device selector dialog to add device to this scene
        addOutputButton.setOnAction(e -> {
            if (outputDeviceSelector != null) {
                outputDeviceSelector.hide();
            }
            OutputDeviceSelector outputDeviceSel = new OutputDeviceSelector(sceneViewModel);
            outputDeviceSel.show(addOutputButton, 0);
            outputDeviceSelector = outputDeviceSel;
        });

        // allow node to be dragged
        statePane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            // allow dragging only when the left button is pressed
            if (!event.isPrimaryButtonDown())
                return;

            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();
            translateAnchorX = getTranslateX();
            translateAnchorY = getTranslateY();
        });
        statePane.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
            // allow dragging only when the left button is pressed
            if (!event.isPrimaryButtonDown())
                return;

            double scale = interactivePane.getScale();

            setTranslateX(translateAnchorX + ((event.getSceneX() - mouseAnchorX) / scale));
            setTranslateY(translateAnchorY + ((event.getSceneY() - mouseAnchorY) / scale));

            hasDragged = true;
            event.consume();

            fireEvent(new SceneEvent(this, null, SceneEvent.SCENE_MOVED, sceneViewModel.getScene()
                    , getBoundsInParent().getMinX(), getBoundsInParent().getMinY()));
        });
        statePane.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (hasDragged) {
                // we consume this event so that the property window will not be opened if we happened to be dragging
                // this node and release our mouse
                event.consume();
            } else {
                // TODO: to be removed
                System.out.println("Show detail!!!");
            }
            hasDragged = false;
        });

        // allow node to connect with other node
        outPort.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            startFullDrag();
//            System.out.println(event.getX() + " " + event.getY());
//            System.out.println(getBoundsInLocal());
//            System.out.println(getBoundsInParent());
//            System.out.println(outPort.getBoundsInLocal());
            // outPort.getBoundsInParent() doesn't take effect apply to parent (15px drop shadow) into consideration.
            // So, we need to subtract it with getBoundsInLocal().getMinX() which include effect in it's bound calculation logic.
            fireEvent(new ConnectionEvent(this, null, ConnectionEvent.CONNECTION_BEGIN
                    , sceneViewModel.getScene(), null
                    , getBoundsInParent().getMinX() + (outPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (outPort.getBoundsInLocal().getWidth() / 2)
                    , getBoundsInParent().getMinY() + (outPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + (outPort.getBoundsInLocal().getHeight() / 2)));
        });
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_ENTERED, event -> showHilight(true));
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_EXITED, event -> showHilight(false));
        inPort.addEventHandler(MouseDragEvent.MOUSE_DRAG_RELEASED, event -> {
            showHilight(false);
            fireEvent(new ConnectionEvent(this, null, ConnectionEvent.CONNECTION_DONE
                    , null, sceneViewModel.getScene()
                    , getBoundsInParent().getMinX() + (inPort.getBoundsInParent().getMinX() - getBoundsInLocal().getMinX())
                        + (inPort.getBoundsInLocal().getWidth() / 2)
                    , getBoundsInParent().getMinY() + (inPort.getBoundsInParent().getMinY() - getBoundsInLocal().getMinY())
                        + (inPort.getBoundsInLocal().getHeight() / 2)));
        });
    }

    private void showHilight(boolean b) {
        if (b) {
            setStyle("-fx-effect: dropshadow(gaussian, #5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
        } else {
            setStyle("-fx-effect: dropshadow(gaussian, derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
        }
    }

//    private void enableDrag() {
//
//        // Register an event filter for a single node and a specific event type
//        statePane.addEventFilter(MouseEvent.MOUSE_ENTERED,
//                new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent event) {
//                        if (!event.isPrimaryButtonDown()) {
//                            getScene().setCursor(Cursor.HAND);
//                        }
//                    }
//                });
//
//        statePane.addEventFilter(MouseEvent.MOUSE_DRAGGED,
//                new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent event) {
//                        if (isPressed) {
//                            setLayoutX(event.getSceneX() + dragDeltaX);
//                            setLayoutY(event.getSceneY() + dragDeltaY);
//                        }
//                    }
//                });
//
//        statePane.addEventFilter(MouseEvent.MOUSE_RELEASED,
//                new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent event) {
//                        getScene().setCursor(Cursor.HAND);
//                        isPressed = false;
//                    }
//                });
//
//        statePane.addEventFilter(MouseEvent.MOUSE_EXITED,
//                new EventHandler<MouseEvent>() {
//                    @Override
//                    public void handle(MouseEvent event) {
//                        if (!event.isPrimaryButtonDown()) {
//                            getScene().setCursor(Cursor.DEFAULT);
//                        }
//                    }
//                });
//
//
//        statePane.setOnMousePressed(mouseEvent -> {
//            isPressed = true;
//            dragDeltaX = getLayoutX() - mouseEvent.getSceneX();
//            dragDeltaY = getLayoutY() - mouseEvent.getSceneY();
//            //setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
//            getScene().setCursor(Cursor.MOVE);
//
//            select.set(true);
//            mouseEvent.consume();
//        });
//
//    }
//
//    public SceneViewModel getSceneViewModel() {
//        return sceneViewModel;
//    }
//
//    public void setOnDesPortDragDetected(EventHandler<? super MouseEvent> e) {
//        desNode.setOnDragDetected(e);
//    }
//
//    public void setOnSrcPortDragDetected(EventHandler<? super MouseEvent> e) {
//        sourceNode.setOnDragDetected(e);
//    }
//
//    public void setOnSrcPortDragOver(EventHandler<? super DragEvent> e) {
//        sourceNode.setOnDragOver(e);
//    }
//
//    public void setOnDesPortDragOver(EventHandler<? super DragEvent> e) {
//        desNode.setOnDragOver(e);
//    }
//
//    public void setOnSrcPortDragEntered(EventHandler<? super DragEvent> e) {
//        sourceNode.setOnDragEntered(e);
//    }
//
//    public void setOnDesPortDragEntered(EventHandler<? super DragEvent> e) {
//        desNode.setOnDragEntered(e);
//    }
//
//    public void setOnSrcPortDragExited(EventHandler<? super DragEvent> e) {
//        sourceNode.setOnDragExited(e);
//    }
//
//    public void setOnDesPortDragExited(EventHandler<? super DragEvent> e) {
//        desNode.setOnDragExited(e);
//    }
//
//    public void setOnSrcPortDragDropped(EventHandler<? super DragEvent> e) {
//        sourceNode.setOnDragDropped(e);
//    }
//
//    public void setOnDesPortDragDropped(EventHandler<? super DragEvent> e) {
//        desNode.setOnDragDropped(e);
//    }
//
//    public void setOnDesPortDragDone(EventHandler<? super DragEvent> e) {
//        desNode.setOnDragDone(e);
//    }
//
    public void setOnRemovedAction(EventHandler<ActionEvent> event) {
        removeSceneBtn.setOnAction(event);
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
}
