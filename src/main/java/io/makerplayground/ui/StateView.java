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
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class StateView extends HBox {
    private final StateViewModel stateViewModel;

    @FXML private VBox statePane;
    @FXML private FlowPane activeIconFlowPane;
    @FXML private FlowPane inactiveIconFlowPane;
    @FXML private TextField nameTextField;
    @FXML private TextField delayTextField;
    @FXML private Arc sourceNode;
    @FXML private Arc desNode;

    private double dragDeltaX;
    private double dragDeltaY;

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
        statePane.setOnMousePressed(mouseEvent -> {
            // record a delta distance for the drag and drop operation.
            dragDeltaX = StateView.this.getLayoutX() - mouseEvent.getX();
            dragDeltaY = StateView.this.getLayoutY() - mouseEvent.getY();
            getScene().setCursor(Cursor.MOVE);
        });
        statePane.setOnMouseReleased(mouseEvent -> {
            getScene().setCursor(Cursor.HAND);
        });
        statePane.setOnMouseDragged(mouseEvent -> {
            StateView.this.setLayoutX(mouseEvent.getX() + dragDeltaX);
            StateView.this.setLayoutY(mouseEvent.getY() + dragDeltaY);
        });
        statePane.setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
            }
        });
        statePane.setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
            }
        });
    }

    public void setDesNodeOnDragDetectedEvent(EventHandler<MouseEvent> e) {
        desNode.setOnDragDetected(e);
    }

    public void setSrcNodeOnDragOverEvent(EventHandler<DragEvent> e) {
        sourceNode.setOnDragOver(e);
    }

    public void setSrcNodeOnDragEnteredEvent(EventHandler<DragEvent> e) {
        sourceNode.setOnDragEntered(e);
    }

    public void setSrcNodeOnDragExitedEvent(EventHandler<DragEvent> e) {
        sourceNode.setOnDragExited(e);
    }

    public void setSrcNodeOnDragDroppedEvent(EventHandler<DragEvent> e) {
        sourceNode.setOnDragDropped(e);
    }

    public void setDesNodeOnDragDoneEvent(EventHandler<DragEvent> e) {
        desNode.setOnDragDone(e);
    }
}
