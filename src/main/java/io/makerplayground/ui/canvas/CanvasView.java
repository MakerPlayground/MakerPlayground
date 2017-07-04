package io.makerplayground.ui.canvas;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasView extends AnchorPane {
    @FXML private AnchorPane anchorPane;
    @FXML private Pane canvasPane;
    @FXML private ScrollPane scrollPane;
    @FXML private Button addStateBtn;

    private SceneViewModel sourceNode;
    private SceneViewModel desNode;

    private final CanvasViewModel canvasViewModel;

    private final ViewFactory<SceneViewModel, SceneView> viewFactory = new ViewFactory<SceneViewModel, SceneView>() {
        @Override
        public SceneView newInstance(SceneViewModel stateViewModel) {
            SceneView stateView = new SceneView(stateViewModel);
            stateView.setDesNodeOnDragDetectedEvent(event -> {
                Dragboard db = stateView.startDragAndDrop(TransferMode.ANY);
                ClipboardContent clipboard = new ClipboardContent();
                clipboard.putString(stateViewModel.getName());
                db.setContent(clipboard);
                event.consume();
            });
            stateView.setSrcNodeOnDragOverEvent(event -> {
                if (event.getGestureSource() != stateViewModel && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }

                event.consume();
            });
            stateView.setSrcNodeOnDragEnteredEvent(event -> {
                if (event.getGestureSource() != stateViewModel && event.getDragboard().hasString()) {
                }

                event.consume();
            });
            stateView.setSrcNodeOnDragExitedEvent(event -> {
                event.consume();
            });
            stateView.setSrcNodeOnDragDroppedEvent(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    canvasViewModel.connectState(db.getString(), stateViewModel.getName());
                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
            });
            stateView.setDesNodeOnDragDoneEvent(event -> {
                if (event.getTransferMode() == TransferMode.COPY) {
                }
                event.consume();
            });
            return stateView;
        }
    };
    private final NodeConsumer<Pane, SceneView> nodeConsumer = new NodeConsumer<Pane, SceneView>() {
        @Override
        public void addNode(Pane parent, SceneView node) {
            parent.getChildren().add(node);
        }

        @Override
        public void removeNode(Pane parent, SceneView node) {
            parent.getChildren().remove(node);
        }
    };

//    private final ViewFactory<SceneViewModel, StateView> viewFactory = new ViewFactory<SceneViewModel, StateView>() {
//        @Override
//        public StateView newInstance(SceneViewModel canvasViewModel) {
//            StateView canvas = new StateView(canvasViewModel);
//            return canvas;
//        }
//    };
//    private final NodeConsumer<Pane, StateView> nodeConsumer = new NodeConsumer<Pane, StateView>() {
//        @Override
//        public void addNode(Pane parent, StateView node) {
//            parent.getChildren().add(node);
//        }
//
//        @Override
//        public void removeNode(Pane parent, StateView node) {
//            parent.getChildren().remove(node);
//        }
//    };

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
        initView();
    }

    private void initView() {

        addStateBtn.setOnAction((event) -> {
            canvasViewModel.project.addState();
        });

        DynamicViewCreator<Pane, SceneViewModel, SceneView> canvasViewCreator =
            new DynamicViewCreator<>(canvasViewModel.getPaneStateViewModel(), canvasPane, viewFactory, nodeConsumer);

        DynamicViewCreator<Pane, LineViewModel , LineView> lineViewCreator =
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
    }
}
