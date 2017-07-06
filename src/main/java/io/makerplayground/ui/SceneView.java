package io.makerplayground.ui;

import io.makerplayground.project.ProjectDevice;
import io.makerplayground.project.UserSetting;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import javafx.beans.binding.Bindings;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.util.converter.NumberStringConverter;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * Created by tanyagorn on 6/12/2017.
 */
public class SceneView extends HBox {
    private final SceneViewModel sceneViewModel;

    @FXML
    private VBox statePane;
    @FXML
    private FlowPane activeIconFlowPane;
    @FXML
    private FlowPane inactiveIconFlowPane;
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField delayTextField;
    @FXML
    private Arc sourceNode;
    @FXML
    private Arc desNode;

    private DevicePropertyWindow devicePropertyWindow;
    private OutputDeviceSelector outputDeviceSelector;
    private double dragDeltaX;
    private double dragDeltaY;
    @FXML private Button addOutputButton;


    public SceneView(SceneViewModel sceneViewModel) {
        this.sceneViewModel = sceneViewModel;
        this.devicePropertyWindow = null;
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
