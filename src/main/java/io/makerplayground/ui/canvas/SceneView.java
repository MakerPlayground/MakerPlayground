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
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;

/**
 *
 */
public class SceneView extends HBox implements Selectable {
    private final SceneViewModel sceneViewModel;

    @FXML private VBox statePane;
    @FXML private FlowPane activeIconFlowPane;
    @FXML private TextField nameTextField;
    @FXML private TextField delayTextField;
    @FXML private Arc sourceNode;
    @FXML private Arc desNode;
    @FXML private Button removeSceneBtn;
    @FXML private ComboBox<Scene.DelayUnit> timeUnitComboBox;
    @FXML private Button addOutputButton;

    private BooleanProperty select;

    private DevicePropertyWindow devicePropertyWindow;
    private OutputDeviceSelector outputDeviceSelector;

    private double dragDeltaX;
    private double dragDeltaY;
    private boolean isPressed;

    public SceneView(SceneViewModel sceneViewModel) {
        this.sceneViewModel = sceneViewModel;
        this.devicePropertyWindow = null;
        this.outputDeviceSelector = null;
        this.select = new SimpleBooleanProperty();
        this.select.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
            } else {
                setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
            }
        });

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StateView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        layoutXProperty().bindBidirectional(sceneViewModel.xProperty());
        layoutYProperty().bindBidirectional(sceneViewModel.yProperty());
        enableDrag();

        Bindings.bindBidirectional(delayTextField.textProperty(), sceneViewModel.delayProperty(), new NumberStringConverter());


        ObservableList<Scene.DelayUnit> data = FXCollections.<Scene.DelayUnit>observableArrayList();
        for (Scene.DelayUnit d : Scene.DelayUnit.values()) {
            data.add(d);
        }

        timeUnitComboBox.getItems().addAll(data);

        timeUnitComboBox.setCellFactory(new Callback<ListView<Scene.DelayUnit>, ListCell<Scene.DelayUnit>>() {
            @Override
            public ListCell<Scene.DelayUnit> call(ListView<Scene.DelayUnit> param) {
                return new ListCell<Scene.DelayUnit>() {
                    @Override
                    protected void updateItem(Scene.DelayUnit item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText("");
                        } else {
                            if (item == Scene.DelayUnit.MilliSecond) {
                                setText("ms");
                            }
                            if (item == Scene.DelayUnit.Second) {
                                setText("s");
                            }
                        }
                    }
                };
            }
        });
        timeUnitComboBox.setButtonCell(new ListCell<Scene.DelayUnit>(){
            @Override
            protected void updateItem(Scene.DelayUnit item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText("");
                } else {
                    if (item == Scene.DelayUnit.MilliSecond) {
                        setText("ms");
                    }
                    if (item == Scene.DelayUnit.Second) {
                        setText("s");
                    }
                }
            }
        });

        timeUnitComboBox.getSelectionModel().selectFirst();
        timeUnitComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            sceneViewModel.setDelayUnit(newValue);
        });

        nameTextField.textProperty().bindBidirectional(sceneViewModel.nameProperty());
        Bindings.bindBidirectional(delayTextField.textProperty(), sceneViewModel.delayProperty(), new NumberStringConverter());

        addOutputButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (outputDeviceSelector != null) {
                    outputDeviceSelector.hide();
                }
                OutputDeviceSelector outputDeviceSel = new OutputDeviceSelector(sceneViewModel);
                outputDeviceSel.show(addOutputButton, 0);
                outputDeviceSelector = outputDeviceSel;
            }
        });
        addOutputButton.visibleProperty().bind(sceneViewModel.hasDeviceToAddProperty());


        DynamicViewCreator<FlowPane, SceneDeviceIconViewModel, SceneDeviceIconView> dynamicViewCreator =
                new DynamicViewCreator<>(sceneViewModel.getDynamicViewModelCreator(), activeIconFlowPane
                        , sceneDeviceIconViewModel -> {
                    SceneDeviceIconView sceneDeviceIconView = new SceneDeviceIconView(sceneDeviceIconViewModel);
                    sceneDeviceIconView.setOnRemove(event -> sceneViewModel.removeStateDevice(sceneDeviceIconViewModel.getProjectDevice()));
                    return sceneDeviceIconView;
                }, new NodeConsumer<FlowPane, SceneDeviceIconView>() {
                    @Override
                    public void addNode(FlowPane parent, SceneDeviceIconView node) {
                        parent.getChildren().add(parent.getChildren().size() - 1, node);
                    }

                    @Override
                    public void removeNode(FlowPane parent, SceneDeviceIconView node) {
                        parent.getChildren().remove(node);
                    }
                });
    }

    private void enableDrag() {

        // Register an event filter for a single node and a specific event type
        statePane.addEventFilter(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!event.isPrimaryButtonDown()) {
                            getScene().setCursor(Cursor.HAND);
                        }
                    }
                });

        statePane.addEventFilter(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (isPressed) {
                            setLayoutX(event.getSceneX() + dragDeltaX);
                            setLayoutY(event.getSceneY() + dragDeltaY);
                        }
                    }
                });

        statePane.addEventFilter(MouseEvent.MOUSE_RELEASED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        getScene().setCursor(Cursor.HAND);
                        isPressed = false;
                    }
                });

        statePane.addEventFilter(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!event.isPrimaryButtonDown()) {
                            getScene().setCursor(Cursor.DEFAULT);
                        }
                    }
                });


        statePane.setOnMousePressed(mouseEvent -> {
            isPressed = true;
            dragDeltaX = getLayoutX() - mouseEvent.getSceneX();
            dragDeltaY = getLayoutY() - mouseEvent.getSceneY();
            //setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
            getScene().setCursor(Cursor.MOVE);

            select.set(true);
            mouseEvent.consume();
        });

    }

    public SceneViewModel getSceneViewModel() {
        return sceneViewModel;
    }

    public void setOnDesPortDragDetected(EventHandler<? super MouseEvent> e) {
        desNode.setOnDragDetected(e);
    }

    public void setOnSrcPortDragDetected(EventHandler<? super MouseEvent> e) {
        sourceNode.setOnDragDetected(e);
    }

    public void setOnSrcPortDragOver(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragOver(e);
    }

    public void setOnDesPortDragOver(EventHandler<? super DragEvent> e) {
        desNode.setOnDragOver(e);
    }

    public void setOnSrcPortDragEntered(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragEntered(e);
    }

    public void setOnDesPortDragEntered(EventHandler<? super DragEvent> e) {
        desNode.setOnDragEntered(e);
    }

    public void setOnSrcPortDragExited(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragExited(e);
    }

    public void setOnDesPortDragExited(EventHandler<? super DragEvent> e) {
        desNode.setOnDragExited(e);
    }

    public void setOnSrcPortDragDropped(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragDropped(e);
    }

    public void setOnDesPortDragDropped(EventHandler<? super DragEvent> e) {
        desNode.setOnDragDropped(e);
    }

    public void setOnDesPortDragDone(EventHandler<? super DragEvent> e) {
        desNode.setOnDragDone(e);
    }

    public void setOnAction(EventHandler<ActionEvent> event) {
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
