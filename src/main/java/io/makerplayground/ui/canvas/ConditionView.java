package io.makerplayground.ui.canvas;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;

import java.io.IOException;

/**
 * Created by USER on 05-Jul-17.
 */
public class ConditionView extends VBox implements Selectable {
    private final ConditionViewModel conditionViewModel;

    @FXML private Circle sourceNode;
    @FXML private Circle destNode;
    @FXML private HBox deviceIconHBox;
    @FXML private Button removeConditionBtn;
    @FXML private Button addInputButton;

    private InputDeviceSelector inputDeviceSelector;
    private double dragDeltaX;
    private double dragDeltaY;

    private BooleanProperty select;

    public ConditionView(ConditionViewModel conditionViewModel) {
        this.conditionViewModel = conditionViewModel;
        this.inputDeviceSelector = null;
        this.select = new SimpleBooleanProperty();

        this.select.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
            } else {
                setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
            }
        });

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ConditionView2.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        layoutXProperty().bindBidirectional(conditionViewModel.xProperty());
        layoutYProperty().bindBidirectional(conditionViewModel.yProperty());

        removeConditionBtn.visibleProperty().bind(select);
        enableDrag();

        addInputButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if (inputDeviceSelector != null) {
                    inputDeviceSelector.hide();
                }
                InputDeviceSelector inputDeviceSel = new InputDeviceSelector(conditionViewModel);
                inputDeviceSel.show(addInputButton,-100);
                inputDeviceSelector = inputDeviceSel;
            }
        });
        addInputButton.visibleProperty().bind(conditionViewModel.hasDeviceToAddProperty());

        DynamicViewCreator<HBox, SceneDeviceIconViewModel, ConditionDeviceIconView> dynamicViewCreator =
                new DynamicViewCreator<>(conditionViewModel.getDynamicViewModelCreator(), deviceIconHBox
                        , conditionDeviceIconViewModel -> {
                    ConditionDeviceIconView conditionDeviceIconView = new ConditionDeviceIconView(conditionDeviceIconViewModel);
                    conditionDeviceIconView.setOnRemove(event -> conditionViewModel.removeConditionDevice(conditionDeviceIconViewModel.getProjectDevice()));
                    return conditionDeviceIconView;
                }, new NodeConsumer<HBox, ConditionDeviceIconView>() {
                    @Override
                    public void addNode(HBox parent, ConditionDeviceIconView node) {
                        parent.getChildren().add(parent.getChildren().size() - 1, node);
                    }

                    @Override
                    public void removeNode(HBox parent, ConditionDeviceIconView node) {
                        parent.getChildren().remove(node);
                    }
                });
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

    private void enableDrag() {
        setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(javafx.scene.Cursor.HAND);
            }
        });
        setOnMousePressed(mouseEvent -> {
            dragDeltaX = getLayoutX() - mouseEvent.getSceneX();
            dragDeltaY = getLayoutY() - mouseEvent.getSceneY();
            getScene().setCursor(javafx.scene.Cursor.MOVE);

            select.set(true);
            mouseEvent.consume();
        });
        setOnMouseDragged(mouseEvent -> {
            setLayoutX(mouseEvent.getSceneX() + dragDeltaX);
            setLayoutY(mouseEvent.getSceneY() + dragDeltaY);
        });
        setOnMouseReleased(mouseEvent -> {
            getScene().setCursor(javafx.scene.Cursor.HAND);
        });
        setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
    }

    public ConditionViewModel getSceneViewModel() {
        return conditionViewModel;
    }

    public void setOnDesPortDragDetected(EventHandler<? super MouseEvent> e) {
        destNode.setOnDragDetected(e);
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
        destNode.setOnDragDone(e);
    }

    public void setOnAction(EventHandler<ActionEvent> event) {
        removeConditionBtn.setOnAction(event);
    }
}
