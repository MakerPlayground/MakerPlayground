package io.makerplayground.ui;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateView extends VBox {
    private final StateViewModel stateViewModel;

    @FXML private FlowPane activeIconFlowPane;
    @FXML private FlowPane inactiveIconFlowPane;
    @FXML private TextField nameTextField;
    @FXML private TextField delayTextField;

    private double dragDeltaX = 0;
    private double dragDeltaY = 0;

    public StateView(StateViewModel stateViewModel) {
        this.stateViewModel = stateViewModel;

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

        DynamicViewCreator<FlowPane, StateDeviceIconViewModel, StateDeviceIconView> dynamicViewCreatorActive = new DynamicViewCreator<>(stateViewModel.getDynamicViewModelCreatorActive(), activeIconFlowPane, new ViewFactory<StateDeviceIconViewModel, StateDeviceIconView>() {
            @Override
            public StateDeviceIconView newInstance(StateDeviceIconViewModel stateDeviceIconViewModel) {
                return new StateDeviceIconView(stateDeviceIconViewModel);
            }
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
        DynamicViewCreator<FlowPane, StateDeviceIconViewModel, StateDeviceIconView> dynamicViewCreatorInactive = new DynamicViewCreator<>(stateViewModel.getDynamicViewModelCreatorInactive(), inactiveIconFlowPane, new ViewFactory<StateDeviceIconViewModel, StateDeviceIconView>() {
            @Override
            public StateDeviceIconView newInstance(StateDeviceIconViewModel stateDeviceIconViewModel) {
                return new StateDeviceIconView(stateDeviceIconViewModel);
            }
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
        setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
            }
        });
        setOnMousePressed(mouseEvent -> {
            dragDeltaX = getLayoutX() - mouseEvent.getSceneX();
            dragDeltaY = getLayoutY() - mouseEvent.getSceneY();
            getScene().setCursor(Cursor.MOVE);
        });
        setOnMouseDragged(mouseEvent -> {
            setLayoutX(mouseEvent.getSceneX() + dragDeltaX);
            setLayoutY(mouseEvent.getSceneY() + dragDeltaY);
        });
        setOnMouseReleased(mouseEvent -> {
            getScene().setCursor(Cursor.HAND);
        });
        setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }
}
