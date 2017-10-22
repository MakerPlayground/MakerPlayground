package io.makerplayground.ui.canvas;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewCreatorBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    @FXML private ScrollPane scrollPane;

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
                inputDeviceSel.show(addInputButton,0);
                inputDeviceSelector = inputDeviceSel;
            }
        });
        addInputButton.visibleProperty().bind(conditionViewModel.hasDeviceToAddProperty());

        DynamicViewCreator<HBox, SceneDeviceIconViewModel, ConditionDeviceIconView> dynamicViewCreator =
                new DynamicViewCreatorBuilder<HBox, SceneDeviceIconViewModel, ConditionDeviceIconView>()
                    .setParent(deviceIconHBox)
                    .setModelLoader(conditionViewModel.getDynamicViewModelCreator())
                    .setViewFactory(conditionDeviceIconViewModel -> {
                        ConditionDeviceIconView conditionDeviceIconView = new ConditionDeviceIconView(conditionDeviceIconViewModel);
                        conditionDeviceIconView.setOnRemove(event -> conditionViewModel.removeConditionDevice(conditionDeviceIconViewModel.getProjectDevice()));
                        return conditionDeviceIconView;
                    })
                    .setNodeAdder((parent, node) -> parent.getChildren().add(parent.getChildren().size() - 1, node))
                    .setNodeRemover((parent, node) -> parent.getChildren().remove(node))
                    .createDynamicViewCreator();
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
        // Register an event filter for a single node and a specific event type
        scrollPane.addEventFilter(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!event.isPrimaryButtonDown()) {
                            getScene().setCursor(Cursor.HAND);
                        }
                    }
                });

        scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        setLayoutX(event.getSceneX() + dragDeltaX);
                        setLayoutY(event.getSceneY() + dragDeltaY);
                    }
                });

        scrollPane.addEventFilter(MouseEvent.MOUSE_RELEASED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        getScene().setCursor(Cursor.HAND);
                    }
                });

        scrollPane.addEventFilter(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!event.isPrimaryButtonDown()) {
                            getScene().setCursor(Cursor.DEFAULT);
                        }
                    }
                });

        scrollPane.setOnMousePressed(mouseEvent -> {
            dragDeltaX = getLayoutX() - mouseEvent.getSceneX();
            dragDeltaY = getLayoutY() - mouseEvent.getSceneY();
            getScene().setCursor(Cursor.MOVE);

            select.set(true);
            mouseEvent.consume();
        });
    }

    public ConditionViewModel getSceneViewModel() {
        return conditionViewModel;
    }

    public void setOnDesPortDragDetected(EventHandler<? super MouseEvent> e) {
        destNode.setOnDragDetected(e);
    }

    public void setOnSrcPortDragDetected(EventHandler<? super MouseEvent> e) {
        sourceNode.setOnDragDetected(e);
    }

    public void setOnSrcPortDragOver(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragOver(e);
    }

    public void setOnDesPortDragOver(EventHandler<? super DragEvent> e) {
        destNode.setOnDragOver(e);
    }

    public void setOnSrcPortDragEntered(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragEntered(e);
    }

    public void setOnDesPortDragEntered(EventHandler<? super DragEvent> e) {
        destNode.setOnDragEntered(e);
    }

    public void setOnSrcPortDragExited(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragExited(e);
    }

    public void setOnDesPortDragExited(EventHandler<? super DragEvent> e) {
        destNode.setOnDragExited(e);
    }

    public void setOnSrcPortDragDropped(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragDropped(e);
    }

    public void setOnDesPortDragDropped(EventHandler<? super DragEvent> e) {
        destNode.setOnDragDropped(e);
    }

    public void setOnDesPortDragDone(EventHandler<? super DragEvent> e) {
        destNode.setOnDragDone(e);
    }

    public void setOnAction(EventHandler<ActionEvent> event) {
        removeConditionBtn.setOnAction(event);
    }
}
