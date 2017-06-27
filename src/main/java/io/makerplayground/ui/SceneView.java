package io.makerplayground.ui;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/26/2017.
 */
public class SceneView extends HBox {
    private final SceneViewModel sceneViewModel;
    static class Delta { double x, y; }

    @FXML private FlowPane activeIconFlowPane;
    @FXML private TextField nameTextField;
    @FXML private TextField delayTextField;

    DevicePropertyWindow devicePropertyWindow;
    OutputDeviceSelector outputDeviceSelector;


    public SceneView(SceneViewModel sceneViewModel) {
        this.sceneViewModel = sceneViewModel;
        this.outputDeviceSelector = null;

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

        nameTextField.textProperty().bindBidirectional(sceneViewModel.nameProperty());
        Bindings.bindBidirectional(delayTextField.textProperty(), sceneViewModel.delayProperty(), new NumberStringConverter());

        Button addOutputButton = new Button("+");
        addOutputButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if (outputDeviceSelector != null) {
                    outputDeviceSelector.hide();
                }
                OutputDeviceSelector outputDeviceSel = new OutputDeviceSelector(sceneViewModel);
                outputDeviceSel.show(addOutputButton, -100);
                outputDeviceSelector = outputDeviceSel;
            }
        });
        activeIconFlowPane.getChildren().add(addOutputButton);

        DynamicViewCreator<FlowPane, StateDeviceIconViewModel, StateDeviceIconView> dynamicViewCreator =
                new DynamicViewCreator<>(sceneViewModel.getDynamicViewModelCreator(), activeIconFlowPane
                        , stateDeviceIconViewModel -> {
                            StateDeviceIconView stateDeviceIconView = new StateDeviceIconView(stateDeviceIconViewModel);
                            stateDeviceIconView.setOnRemove(event -> sceneViewModel.removeStateDevice(stateDeviceIconViewModel.getProjectDevice()));
                            return stateDeviceIconView;
                        }, new NodeConsumer<FlowPane, StateDeviceIconView>() {
                            @Override
                            public void addNode(FlowPane parent, StateDeviceIconView node) {
                                parent.getChildren().add(parent.getChildren().size() - 1, node);
                            }

                            @Override
                            public void removeNode(FlowPane parent, StateDeviceIconView node) {
                                parent.getChildren().remove(node);
                            }
                        });

    }

    private void enableDrag() {
        final Delta dragDelta = new Delta();
        setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = SceneView.this.getLayoutX() - mouseEvent.getSceneX();
                dragDelta.y = SceneView.this.getLayoutY() - mouseEvent.getSceneY();
                getScene().setCursor(Cursor.MOVE);
            }
        });
        setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                getScene().setCursor(Cursor.HAND);
            }
        });
        setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                SceneView.this.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
                SceneView.this.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
            }
        });
        setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    getScene().setCursor(Cursor.HAND);
                }
            }
        });
        setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                if (!mouseEvent.isPrimaryButtonDown()) {
                    getScene().setCursor(Cursor.DEFAULT);
                }
            }
        });
    }
}
