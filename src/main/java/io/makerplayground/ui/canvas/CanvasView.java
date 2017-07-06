package io.makerplayground.ui.canvas;

import io.makerplayground.project.Condition;
import io.makerplayground.project.NodeElement;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

import java.io.IOException;

/**
 *
 */
public class CanvasView extends AnchorPane {
    @FXML private AnchorPane anchorPane;
    @FXML private Pane canvasPane;
    @FXML private ScrollPane scrollPane;
    @FXML private Button addStateBtn;
    @FXML private Button addConditionBtn;

    private final CanvasViewModel canvasViewModel;
    private Line guideLine;

    private NodeElement source;   // TODO: leak model into view
    private NodeElement dest;      // TODO: leak model into view

    public CanvasView(CanvasViewModel canvasViewModel) {
        this.canvasViewModel = canvasViewModel;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/CanvasView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        addStateBtn.setOnAction((event) -> {
            canvasViewModel.project.addState();
        });
        addConditionBtn.setOnAction((event) -> {
            canvasViewModel.project.addCondition();
        });

        DynamicViewCreator<Pane, SceneViewModel, SceneView> canvasViewCreator =
                new DynamicViewCreator<>(canvasViewModel.getPaneStateViewModel(), canvasPane, sceneViewModel -> {
                    SceneView sceneView = new SceneView(sceneViewModel);
                    addStateConnectionEvent(sceneView);
                    return sceneView;
                }, new NodeConsumer<Pane, SceneView>() {
                    @Override
                    public void addNode(Pane parent, SceneView node) {
                        parent.getChildren().add(node);
                    }

                    @Override
                    public void removeNode(Pane parent, SceneView node) {
                        parent.getChildren().remove(node);
                    }
                });

        DynamicViewCreator<Pane, ConditionViewModel, ConditionView> conditionViewCreator =
                new DynamicViewCreator<>(canvasViewModel.getConditionViewModel(), canvasPane, conditionViewModel -> {
                    ConditionView conditionView = new ConditionView(conditionViewModel);
                    addConditionConnectionEvent(conditionView);
                    return conditionView;
                }, new NodeConsumer<Pane, ConditionView>() {
                    @Override
                    public void addNode(Pane parent, ConditionView node) {
                        parent.getChildren().add(node);
                    }

                    @Override
                    public void removeNode(Pane parent, ConditionView node) {
                        parent.getChildren().add(node);
                    }
                });

        DynamicViewCreator<Pane, LineViewModel, LineView> lineViewCreator =
                new DynamicViewCreator<>(canvasViewModel.getLineViewModel(), canvasPane, LineView::new, new NodeConsumer<Pane, LineView>() {
                    @Override
                    public void addNode(Pane parent, LineView node) {
                        parent.getChildren().add(node);
                    }

                    @Override
                    public void removeNode(Pane parent, LineView node) {
                        parent.getChildren().remove(node);
                    }
                });

        guideLine = new Line();
        guideLine.setVisible(false);
        canvasPane.getChildren().add(guideLine);

        setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.MOVE) {
            }

            guideLine.setVisible(false);

            event.consume();
        });

        setOnDragOver(event -> {
            System.out.println(event.getSceneX() + " " + event.getSceneY());

            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());

            event.consume();
        });
    }

    private void addStateConnectionEvent(SceneView sceneView) {
        sceneView.setOnDesPortDragDetected(event -> {
            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);

            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString("");
            db.setContent(clipboard);

            guideLine.setStartX(event.getSceneX());
            guideLine.setStartY(event.getSceneY());
            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());
            guideLine.setVisible(true);

            source = sceneView.getSceneViewModel().getScene();

            event.consume();
        });
        sceneView.setOnSrcPortDragOver(event -> {
            System.out.println(event.getSceneX() + " " + event.getSceneY());

            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());

            event.consume();
        });
        sceneView.setOnSrcPortDragEntered(event -> {
            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
                // TODO: add visual feedback
            }

            event.consume();
        });
        sceneView.setOnSrcPortDragExited(event -> {
            // TODO: remove visual feedback

            event.consume();
        });
        sceneView.setOnSrcPortDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                System.out.println("Connect to => " + db.getString());
                success = true;
            }
            canvasViewModel.connectState(source, sceneView.getSceneViewModel().getScene());
            event.setDropCompleted(success);

            event.consume();
        });
    }

    private void addConditionConnectionEvent(ConditionView conditionView) {
        conditionView.setOnDesPortDragDetected(event -> {
            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);

            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString("");
            db.setContent(clipboard);

            guideLine.setStartX(event.getSceneX());
            guideLine.setStartY(event.getSceneY());
            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());
            guideLine.setVisible(true);

            source = conditionView.getSceneViewModel().getCondition();

            event.consume();
        });
        conditionView.setOnSrcPortDragOver(event -> {
            System.out.println(event.getSceneX() + " " + event.getSceneY());

            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            guideLine.setEndX(event.getSceneX());
            guideLine.setEndY(event.getSceneY());

            event.consume();
        });
        conditionView.setOnSrcPortDragEntered(event -> {
            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
                // TODO: add visual feedback
            }

            event.consume();
        });
        conditionView.setOnSrcPortDragExited(event -> {
            // TODO: remove visual feedback

            event.consume();
        });
        conditionView.setOnSrcPortDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                System.out.println("Connect to => " + db.getString());
                success = true;
            }
            canvasViewModel.connectState(source, conditionView.getSceneViewModel().getCondition());
            event.setDropCompleted(success);

            event.consume();
        });
    }
}
