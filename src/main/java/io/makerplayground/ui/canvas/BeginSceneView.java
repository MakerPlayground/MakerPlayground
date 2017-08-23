package io.makerplayground.ui.canvas;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Arc;

import java.io.IOException;

/**
 * Created by Mai.Manju on 13-Jul-17.
 */
public class BeginSceneView extends HBox {
    @FXML private HBox beginHBox;
    @FXML private Arc sourceNode;
    @FXML private Label labelHBox;

    private final BeginSceneViewModel beginSceneViewModel;

    private double dragDeltaX;
    private double dragDeltaY;

    public BeginSceneView(BeginSceneViewModel beginSceneViewModel) {
        this.beginSceneViewModel = beginSceneViewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/BeginScene.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        layoutXProperty().bindBidirectional(beginSceneViewModel.xProperty());
        layoutYProperty().bindBidirectional(beginSceneViewModel.yProperty());
        enableDrag();
    }

    private void enableDrag() {

        // Register an event filter for a single node and a specific event type
        labelHBox.addEventFilter(MouseEvent.MOUSE_ENTERED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!event.isPrimaryButtonDown()) {
                            getScene().setCursor(Cursor.HAND);
                        }
                        setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
                    }
                });

        labelHBox.addEventFilter(MouseEvent.MOUSE_DRAGGED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        setLayoutX(event.getSceneX() + dragDeltaX);
                        setLayoutY(event.getSceneY() + dragDeltaY);
                        setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
                    }
                });

        labelHBox.addEventFilter(MouseEvent.MOUSE_RELEASED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        getScene().setCursor(Cursor.HAND);
                        setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
                    }
                });

        labelHBox.addEventFilter(MouseEvent.MOUSE_EXITED,
                new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (!event.isPrimaryButtonDown()) {
                            getScene().setCursor(Cursor.DEFAULT);
                        }
                        setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
                    }
                });

        labelHBox.setOnMousePressed(mouseEvent -> {
            dragDeltaX = getLayoutX() - mouseEvent.getSceneX();
            dragDeltaY = getLayoutY() - mouseEvent.getSceneY();
            setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
            getScene().setCursor(Cursor.MOVE);
        });
    }

    public void setOnDesPortDragDetected(EventHandler<? super MouseEvent> e) {
        sourceNode.setOnDragDetected(e);
    }
//
//    public void setOnSrcPortDragOver(EventHandler<? super DragEvent> e) {
//        sourceNode.setOnDragOver(e);
//    }
//
//    public void setOnSrcPortDragEntered(EventHandler<? super DragEvent> e) {
//        sourceNode.setOnDragEntered(e);
//    }
//
//    public void setOnSrcPortDragExited(EventHandler<? super DragEvent> e) {
//        sourceNode.setOnDragExited(e);
//    }
//
//    public void setOnSrcPortDragDropped(EventHandler<? super DragEvent> e) {
//        sourceNode.setOnDragDropped(e);
//    }

    public void setOnDesPortDragDone(EventHandler<? super DragEvent> e) {
        sourceNode.setOnDragDone(e);
    }

    public BeginSceneViewModel getBeginSceneViewModel() {
        return beginSceneViewModel;
    }
}
