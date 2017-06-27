package io.makerplayground.ui;

import io.makerplayground.project.Project;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasView extends AnchorPane {
    @FXML private AnchorPane anchorPane;
    @FXML private Pane canvasPane;
    @FXML private ScrollPane scrollPane;
    @FXML private Button addStateBtn;

    private StateViewModel sourceNode;
    private StateViewModel desNode;

    private final CanvasViewModel canvasViewModel;
    private final ViewFactory<StateViewModel, StateView> viewFactory = new ViewFactory<StateViewModel, StateView>() {
        @Override
        public StateView newInstance(StateViewModel stateViewModel) {
            StateView stateView = new StateView(stateViewModel);
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
    private final NodeConsumer<Pane, StateView> nodeConsumer = new NodeConsumer<Pane, StateView>() {
        @Override
        public void addNode(Pane parent, StateView node) {
            parent.getChildren().add(node);
        }

        @Override
        public void removeNode(Pane parent, StateView node) {
            parent.getChildren().remove(node);
        }
    };

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

        DynamicViewCreator<Pane, StateViewModel, StateView> canvasViewCreator =
            new DynamicViewCreator<>(canvasViewModel.getPaneStateViewModel(), canvasPane, viewFactory, new NodeConsumer<Pane, StateView>() {
                @Override
                public void addNode(Pane parent, StateView node) {
                    parent.getChildren().add(node);
                }

                @Override
                public void removeNode(Pane parent, StateView node) {
                    parent.getChildren().remove(node);
                }
            });

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
