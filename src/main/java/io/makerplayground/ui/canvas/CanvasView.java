package io.makerplayground.ui.canvas;

import io.makerplayground.project.NodeElement;
import io.makerplayground.ui.canvas.event.ConnectionEvent;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewCreatorBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;

/**
 *
 */
public class CanvasView extends AnchorPane {
    private final InteractivePane mainPane = new InteractivePane();
    private final Button addStateBtn = new Button();
    private final Button addConditionBtn = new Button();
    private final TextField zoomTextField = new TextField();
    private Line guideLine = new Line();

    private final CanvasViewModel canvasViewModel;
    private final SelectionGroup selectionGroup;

    private NodeElement sourceNode; // TODO: leak model into view
    private NodeElement destNode;   // TODO: leak model into view

    //private boolean flag = false; // false means left, true means right

    public CanvasView(CanvasViewModel canvasViewModel) {
        this.canvasViewModel = canvasViewModel;
        this.selectionGroup = new SelectionGroup();
        initView();
        initEvent();
    }

    private void initView() {
        addStateBtn.setText("Add Scene");
        addStateBtn.setOnAction(event -> canvasViewModel.project.addState());
        AnchorPane.setTopAnchor(addStateBtn, 20.0);
        AnchorPane.setRightAnchor(addStateBtn, 20.0);

        addConditionBtn.setText("Add Condition");
        addConditionBtn.setOnAction(event -> canvasViewModel.project.addCondition());
        AnchorPane.setTopAnchor(addConditionBtn, 40.0);
        AnchorPane.setRightAnchor(addConditionBtn, 20.0);

        zoomTextField.setText(String.valueOf(mainPane.getScale()));
        zoomTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.isEmpty()) {
                    mainPane.setScale(Double.parseDouble(newValue));
                }
            } catch (NumberFormatException e) {
                zoomTextField.setText(oldValue);
            }
        });
        AnchorPane.setBottomAnchor(zoomTextField, 20.0);
        AnchorPane.setRightAnchor(zoomTextField, 20.0);

        BeginSceneView beginSceneView = new BeginSceneView(canvasViewModel.getBeginViewModel());
        //addBeginConnectionEvent(beginSceneView);
        mainPane.addChildren(beginSceneView);

        guideLine = new Line();
        guideLine.setVisible(false);
        guideLine.setStrokeWidth(3.25);
        guideLine.setStyle("-fx-stroke: #313644;");
        mainPane.addChildren(guideLine);

        DynamicViewCreator<InteractivePane, SceneViewModel, SceneView> canvasViewCreator =
                new DynamicViewCreatorBuilder<InteractivePane, SceneViewModel, SceneView>()
                        .setParent(mainPane)
                        .setModelLoader(canvasViewModel.getPaneStateViewModel())
                        .setViewFactory(sceneViewModel -> {
                            SceneView sceneView = new SceneView(sceneViewModel, mainPane);
                            addSceneViewConnectionEvent(sceneView);
                            selectionGroup.getSelectable().add(sceneView);
                            sceneView.setOnRemovedAction(event -> canvasViewModel.project.removeState(sceneViewModel.getScene()));
                            return sceneView;
                        })
                        .setNodeAdder(InteractivePane::addChildren)
                        .setNodeRemover(InteractivePane::removeChildren)
                        .createDynamicViewCreator();
        DynamicViewCreator<InteractivePane, ConditionViewModel, ConditionView> conditionViewCreator =
                new DynamicViewCreatorBuilder<InteractivePane, ConditionViewModel, ConditionView>()
                        .setParent(mainPane)
                        .setModelLoader(canvasViewModel.getConditionViewModel())
                        .setViewFactory(conditionViewModel -> {
                            ConditionView conditionView = new ConditionView(conditionViewModel);
                            //addConditionConnectionEvent(conditionView);
                            selectionGroup.getSelectable().add(conditionView);
                            conditionView.setOnAction(event -> canvasViewModel.project.removeCondition(conditionViewModel.getCondition()));
                            return conditionView;
                        })
                        .setNodeAdder(InteractivePane::addChildren)
                        .setNodeRemover(InteractivePane::removeChildren)
                        .createDynamicViewCreator();
        DynamicViewCreator<InteractivePane, LineViewModel, LineView> lineViewCreator =
                new DynamicViewCreatorBuilder<InteractivePane, LineViewModel, LineView>()
                        .setParent(mainPane)
                        .setModelLoader(canvasViewModel.getLineViewModel())
                        .setViewFactory(lineViewModel -> {
                            LineView lineView = new LineView(lineViewModel);
                            lineView.setOnAction(event -> canvasViewModel.project.removeLine(lineViewModel.getLine()));
                            selectionGroup.getSelectable().add(lineView);
                            return lineView;
                        })
                        .setNodeAdder(InteractivePane::addChildren)
                        .setNodeRemover(InteractivePane::removeChildren)
                        .createDynamicViewCreator();

        AnchorPane.setTopAnchor(mainPane, 0.0);
        AnchorPane.setRightAnchor(mainPane, 0.0);
        AnchorPane.setBottomAnchor(mainPane, 0.0);
        AnchorPane.setLeftAnchor(mainPane, 0.0);
        getChildren().addAll(mainPane, addStateBtn, addConditionBtn, zoomTextField);
    }

    private void initEvent() {
        // deselect when click at blank space in the canvas
        mainPane.setOnMousePressed(event -> selectionGroup.deselect());
    }

    private void addSceneViewConnectionEvent(SceneView sceneView) {
        sceneView.addEventFilter(ConnectionEvent.CONNECTION_BEGIN, event -> {
            guideLine.setStartX(event.getX());
            guideLine.setStartY(event.getY());
            guideLine.setEndX(event.getX());
            guideLine.setEndY(event.getY());
            guideLine.setVisible(true);
            sourceNode = event.getSourceNode();
        });

        mainPane.addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> {
            guideLine.setEndX(-mainPane.getViewportBounds().getMinX() + event.getX());
            guideLine.setEndY(-mainPane.getViewportBounds().getMinY() + event.getY());
            event.consume();
        });

        sceneView.addEventFilter(ConnectionEvent.CONNECTION_DONE, event -> {
            guideLine.setVisible(false);
            destNode = event.getDestinationNode();
            canvasViewModel.connect(sourceNode, destNode);
        });

        mainPane.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> guideLine.setVisible(false));
    }

//    private void addStateConnectionEvent(SceneView sceneView) {
//        sceneView.setOnDesPortDragDetected(event -> {
//            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);
//
//            ClipboardContent clipboard = new ClipboardContent();
//            clipboard.putString("");
//            db.setContent(clipboard);
//
//            guideLine.setStartX(event.getSceneX());
//            guideLine.setStartY(event.getSceneY() - 32.5);
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY());
//            guideLine.setVisible(true);
//
//            source = sceneView.getSceneViewModel().getScene();
//            flag = true; // right port
//
//            event.consume();
//        });
//        sceneView.setOnSrcPortDragDetected(event -> {
//            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);
//
//            ClipboardContent clipboard = new ClipboardContent();
//            clipboard.putString("");
//            db.setContent(clipboard);
//
//            guideLine.setStartX(event.getSceneX());
//            guideLine.setStartY(event.getSceneY() - 32.5);
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY());
//            guideLine.setVisible(true);
//
//            source = sceneView.getSceneViewModel().getScene();
//            flag = false; // left port
//
//            event.consume();
//        });
//        sceneView.setOnSrcPortDragOver(event -> {
//            System.out.println(event.getSceneX() + " " + event.getSceneY());
//            if (!flag) {
//                return;
//            }
//            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
//                sceneView.setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
//                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//            }
//
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY() - 32.5);
//            event.consume();
//        });
//        sceneView.setOnDesPortDragOver(event -> {
//            System.out.println(event.getSceneX() + " " + event.getSceneY());
//
//            if (flag) {
//                return;
//            }
//            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
//                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//            }
//
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY() - 32.5);
//
//            event.consume();
//        });
//        sceneView.setOnSrcPortDragEntered(event -> {
//            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
//                // TODO: add visual feedback
//            }
//
//            event.consume();
//        });
//        sceneView.setOnDesPortDragEntered(event -> {
//            if (event.getGestureSource() != sceneView && event.getDragboard().hasString()) {
//                // TODO: add visual feedback
//            }
//
//            event.consume();
//        });
//        sceneView.setOnSrcPortDragExited(event -> {
//            // TODO: remove visual feedback
//            sceneView.setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
//            event.consume();
//        });
//        sceneView.setOnDesPortDragExited(event -> {
//            // TODO: remove visual feedback
//
//            event.consume();
//        });
//        sceneView.setOnSrcPortDragDropped(event -> {
//            Dragboard db = event.getDragboard();
//            boolean success = false;
//            if (db.hasString()) {
//                System.out.println("Connect to => " + db.getString());
//                success = true;
//            }
//            canvasViewModel.connectState(source, sceneView.getSceneViewModel().getScene());
//            event.setDropCompleted(success);
//
//            event.consume();
//        });
//        sceneView.setOnDesPortDragDropped(event -> {
//            Dragboard db = event.getDragboard();
//            boolean success = false;
//            if (db.hasString()) {
//                System.out.println("Connect to => " + db.getString());
//                success = true;
//            }
//            canvasViewModel.connectState(sceneView.getSceneViewModel().getScene(), source);
//            event.setDropCompleted(success);
//
//            event.consume();
//        });
//    }
//
//    private void addConditionConnectionEvent(ConditionView conditionView) {
//        conditionView.setOnDesPortDragDetected(event -> {
//            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);
//
//            ClipboardContent clipboard = new ClipboardContent();
//            clipboard.putString("");
//            db.setContent(clipboard);
//
//            guideLine.setStartX(event.getSceneX());
//            guideLine.setStartY(event.getSceneY() - 32.5);
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY());
//            guideLine.setVisible(true);
//
//            source = conditionView.getSceneViewModel().getCondition();
//            flag = true; // right port
//
//            event.consume();
//        });
//
//        conditionView.setOnSrcPortDragDetected(event -> {
//            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);
//
//            ClipboardContent clipboard = new ClipboardContent();
//            clipboard.putString("");
//            db.setContent(clipboard);
//
//            guideLine.setStartX(event.getSceneX());
//            guideLine.setStartY(event.getSceneY() - 32.5);
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY());
//            guideLine.setVisible(true);
//
//            source = conditionView.getSceneViewModel().getCondition();
//            flag = false; // left port
//
//            event.consume();
//        });
//
//        conditionView.setOnSrcPortDragOver(event -> {
//            System.out.println(event.getSceneX() + " " + event.getSceneY());
//
//            if (flag == false) {
//                return;
//            }
//            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
//                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//                conditionView.setStyle("-fx-effect: dropshadow(gaussian,#5ac2ab, 15.0 , 0.5, 0.0 , 0.0);");
//            }
//
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY() - 32.5);
//
//            event.consume();
//        });
//
//        conditionView.setOnDesPortDragOver(event -> {
//            System.out.println(event.getSceneX() + " " + event.getSceneY());
//
//            if (flag == true) {
//                return;
//            }
//            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
//                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//            }
//
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY() - 32.5);
//
//            event.consume();
//        });
//
//        conditionView.setOnSrcPortDragEntered(event -> {
//            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
//                // TODO: add visual feedback
//            }
//
//            event.consume();
//        });
//
//        conditionView.setOnDesPortDragEntered(event -> {
//            if (event.getGestureSource() != conditionView && event.getDragboard().hasString()) {
//                // TODO: add visual feedback
//            }
//
//            event.consume();
//        });
//        conditionView.setOnSrcPortDragExited(event -> {
//            // TODO: remove visual feedback
//            conditionView.setStyle("-fx-effect: dropshadow(gaussian,derive(black,75%), 15.0 , 0.0, 0.0 , 0.0);");
//            event.consume();
//        });
//        conditionView.setOnDesPortDragExited(event -> {
//            // TODO: remove visual feedback
//
//            event.consume();
//        });
//        conditionView.setOnSrcPortDragDropped(event -> {
//            Dragboard db = event.getDragboard();
//            boolean success = false;
//            if (db.hasString()) {
//                System.out.println("Connect to => " + db.getString());
//                success = true;
//            }
//            canvasViewModel.connectState(source, conditionView.getSceneViewModel().getCondition());
//            event.setDropCompleted(success);
//
//            event.consume();
//        });
//
//        conditionView.setOnDesPortDragDropped(event -> {
//            Dragboard db = event.getDragboard();
//            boolean success = false;
//            if (db.hasString()) {
//                System.out.println("Connect to => " + db.getString());
//                success = true;
//            }
//            canvasViewModel.connectState(conditionView.getSceneViewModel().getCondition(), source);
//            event.setDropCompleted(success);
//
//            event.consume();
//        });
//    }
//
//    private void addBeginConnectionEvent(BeginSceneView beginSceneView) {
//        beginSceneView.setOnDesPortDragDetected(event -> {
//            Dragboard db = CanvasView.this.startDragAndDrop(TransferMode.ANY);
//
//            ClipboardContent clipboard = new ClipboardContent();
//            clipboard.putString("");
//            db.setContent(clipboard);
//
//            guideLine.setStartX(event.getSceneX());
//            guideLine.setStartY(event.getSceneY() - 32.5);
//            guideLine.setEndX(event.getSceneX());
//            guideLine.setEndY(event.getSceneY());
//            guideLine.setVisible(true);
//
//            source = beginSceneView.getBeginSceneViewModel().getBegin();
//            flag = true;
//
//            event.consume();
//        });
//    }
}
