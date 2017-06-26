package io.makerplayground.ui;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.io.IOException;

/**
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasView extends AnchorPane {
    @FXML private AnchorPane anchorPane;
    @FXML private Pane canvasPane;
    @FXML private ScrollPane scrollPane;
    @FXML private Button addStateBtn;

    private final CanvasViewModel canvasViewModel;
    private final ViewFactory<StateViewModel, StateView> viewFactory = new ViewFactory<StateViewModel, StateView>() {
        @Override
        public StateView newInstance(StateViewModel canvasViewModel) {
            StateView canvas = new StateView(canvasViewModel);
            return canvas;
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
