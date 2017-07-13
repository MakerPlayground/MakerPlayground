package io.makerplayground.ui.canvas;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
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
        setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.HAND);
            }
            setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
        });
        setOnMousePressed(mouseEvent -> {
            dragDeltaX = getLayoutX() - mouseEvent.getSceneX();
            dragDeltaY = getLayoutY() - mouseEvent.getSceneY();
            setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
            getScene().setCursor(Cursor.MOVE);
        });
        setOnMouseDragged(mouseEvent -> {
            setLayoutX(mouseEvent.getSceneX() + dragDeltaX);
            setLayoutY(mouseEvent.getSceneY() + dragDeltaY);
            setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
        });
        setOnMouseReleased(mouseEvent -> {
            getScene().setCursor(Cursor.HAND);
            setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
        });
        setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(Cursor.DEFAULT);
            }
            setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
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
