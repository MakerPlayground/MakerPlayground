package io.makerplayground.ui;

import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.NodeConsumer;
import io.makerplayground.uihelper.ViewFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 * Created by tanyagorn on 6/13/2017.
 */
public class CanvasView extends AnchorPane {

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
        initView();
    }

    private void initView() {
        Button btnAddState = new Button("Add State");
        btnAddState.setOnAction((event) -> {
            canvasViewModel.project.addState();
        });

        HBox hb = new HBox();
        hb.setPadding(new Insets(10, 10, 10, 10));
        hb.setSpacing(10);
        hb.getChildren().addAll(btnAddState);

        setTopAnchor(hb, 8.0);
        setRightAnchor(hb, 8.0);

        Pane canvasPane = new Pane();
        DynamicViewCreator<Pane, StateViewModel, StateView> canvasViewCreator =
            new DynamicViewCreator<>(canvasViewModel.getPaneStateViewModel(), canvasPane, viewFactory, nodeConsumer);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(canvasPane);

        getChildren().addAll(hb, scrollPane);
    }

}
