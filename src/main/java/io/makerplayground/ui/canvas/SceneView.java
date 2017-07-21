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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
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
    @FXML private ComboBox<String> timeUnitComboBox;
    @FXML private Button addOutputButton;

    private BooleanProperty select;

    private DevicePropertyWindow devicePropertyWindow;
    private OutputDeviceSelector outputDeviceSelector;

    private final ObservableList<String> timeUnit = FXCollections.observableArrayList("ms", "s");
    private double dragDeltaX;
    private double dragDeltaY;

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

        timeUnitComboBox.getItems().addAll(timeUnit);
        timeUnitComboBox.getSelectionModel().selectFirst();

        nameTextField.textProperty().bindBidirectional(sceneViewModel.nameProperty());
        Bindings.bindBidirectional(delayTextField.textProperty(), sceneViewModel.delayProperty(), new NumberStringConverter());

        addOutputButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (outputDeviceSelector != null) {
                    outputDeviceSelector.hide();
                }
                OutputDeviceSelector outputDeviceSel = new OutputDeviceSelector(sceneViewModel);
                outputDeviceSel.show(addOutputButton, -100);
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
        setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
            }
            //setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
        });
        setOnMousePressed(mouseEvent -> {
            dragDeltaX = getLayoutX() - mouseEvent.getSceneX();
            dragDeltaY = getLayoutY() - mouseEvent.getSceneY();
            //setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
            getScene().setCursor(Cursor.MOVE);

            select.set(true);
            mouseEvent.consume();
        });
        setOnMouseDragged(mouseEvent -> {
            setLayoutX(mouseEvent.getSceneX() + dragDeltaX);
            setLayoutY(mouseEvent.getSceneY() + dragDeltaY);
            //setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
        });
        setOnMouseReleased(mouseEvent -> {
            getScene().setCursor(Cursor.HAND);
            //setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
        });
        setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
            }
            //setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
        });
    }

    public SceneViewModel getSceneViewModel() {
        return sceneViewModel;
    }

    public void setOnDesPortDragDetected(EventHandler<? super MouseEvent> e) {
        desNode.setOnDragDetected(e);
    }

    public void setOnSrcPortDragOver(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragOver(e);
    }

    public void setOnSrcPortDragEntered(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragEntered(e);
    }

    public void setOnSrcPortDragExited(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragExited(e);
    }

    public void setOnSrcPortDragDropped(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragDropped(e);
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
