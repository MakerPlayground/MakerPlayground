package io.makerplayground.ui;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateView extends VBox {
    private final StateViewModel stateViewModel;

    static class Delta { double x, y; }

    @FXML private FlowPane activeIconFlowPane;
    @FXML private FlowPane inactiveIconFlowPane;
    @FXML private TextField nameTextField;
    @FXML private TextField delayTextField;

    DevicePropertyWindow devicePropertyWindow;


    public StateView(StateViewModel stateViewModel) {
        this.stateViewModel = stateViewModel;
        this.devicePropertyWindow = null;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/StateView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        layoutXProperty().bindBidirectional(stateViewModel.xProperty());
        layoutYProperty().bindBidirectional(stateViewModel.yProperty());
        enableDrag();

        nameTextField.textProperty().bindBidirectional(stateViewModel.nameProperty());
        Bindings.bindBidirectional(delayTextField.textProperty(), stateViewModel.delayProperty(), new NumberStringConverter());

        DynamicViewCreator<FlowPane, StateDeviceIconViewModel, StateDeviceIconView> dynamicViewCreatorActive = new DynamicViewCreator<>(stateViewModel.getDynamicViewModelCreatorActive(), activeIconFlowPane, stateDeviceIconViewModel -> {
            StateDeviceIconView stateDeviceIconView = new StateDeviceIconView(stateDeviceIconViewModel);
            stateDeviceIconView.setOnMouseClicked(e -> {
                if (this.devicePropertyWindow != null)
                    this.devicePropertyWindow.hide();
                DevicePropertyWindow devicePropertyWindow = new DevicePropertyWindow(stateDeviceIconViewModel);
                devicePropertyWindow.show(StateView.this);
                this.devicePropertyWindow = devicePropertyWindow;
            });
            return stateDeviceIconView;
        }, new NodeConsumer<FlowPane, StateDeviceIconView>() {
            @Override
            public void addNode(FlowPane parent, StateDeviceIconView node) {
                parent.getChildren().add(node);
            }

            @Override
            public void removeNode(FlowPane parent, StateDeviceIconView node) {
                parent.getChildren().remove(node);
            }
        });
        DynamicViewCreator<FlowPane, StateDeviceIconViewModel, StateDeviceIconView> dynamicViewCreatorInactive = new DynamicViewCreator<>(stateViewModel.getDynamicViewModelCreatorInactive(), inactiveIconFlowPane, stateDeviceIconViewModel -> {
            StateDeviceIconView stateDeviceIconView = new StateDeviceIconView(stateDeviceIconViewModel);
            stateDeviceIconView.setOnMouseClicked(e -> {
                if (this.devicePropertyWindow != null)
                    this.devicePropertyWindow.hide();
                DevicePropertyWindow devicePropertyWindow = new DevicePropertyWindow(stateDeviceIconViewModel);
                devicePropertyWindow.show(StateView.this);
                this.devicePropertyWindow = devicePropertyWindow;
            });
            return stateDeviceIconView;
        }, new NodeConsumer<FlowPane, StateDeviceIconView>() {
            @Override
            public void addNode(FlowPane parent, StateDeviceIconView node) {
                parent.getChildren().add(node);
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
                dragDelta.x = StateView.this.getLayoutX() - mouseEvent.getX();
                dragDelta.y = StateView.this.getLayoutY() - mouseEvent.getY();
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
                StateView.this.setLayoutX(mouseEvent.getX() + dragDelta.x);
                StateView.this.setLayoutY(mouseEvent.getY() + dragDelta.y);
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
