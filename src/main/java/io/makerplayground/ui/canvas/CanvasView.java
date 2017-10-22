package io.makerplayground.ui.canvas;

import io.makerplayground.project.NodeElement;
import io.makerplayground.ui.canvas.event.InteractiveNodeEvent;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewCreatorBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

/**
 *
 */
public class CanvasView extends AnchorPane {
    private final InteractivePane mainPane = new InteractivePane();
    private final Button addStateBtn = new Button();
    private final Button addConditionBtn = new Button();
    private final TextField zoomTextField = new TextField();

    private final CanvasViewModel canvasViewModel;

    private NodeElement sourceNode; // TODO: leak model into view
    private NodeElement destNode;   // TODO: leak model into view

    public CanvasView(CanvasViewModel canvasViewModel) {
        this.canvasViewModel = canvasViewModel;
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
                    double scale = Double.parseDouble(newValue);
                    if (scale > 0) {
                        mainPane.setScale(scale);
                    }
                }
            } catch (NumberFormatException e) {
                zoomTextField.setText(oldValue);
            }
        });
        AnchorPane.setBottomAnchor(zoomTextField, 20.0);
        AnchorPane.setRightAnchor(zoomTextField, 20.0);

        BeginSceneView beginSceneView = new BeginSceneView(canvasViewModel.getBeginViewModel(), mainPane);
        addConnectionEvent(beginSceneView);
        mainPane.addChildren(beginSceneView);

        DynamicViewCreator<InteractivePane, SceneViewModel, SceneView> canvasViewCreator =
                new DynamicViewCreatorBuilder<InteractivePane, SceneViewModel, SceneView>()
                        .setParent(mainPane)
                        .setModelLoader(canvasViewModel.getPaneStateViewModel())
                        .setViewFactory(sceneViewModel -> {
                            SceneView sceneView = new SceneView(sceneViewModel, mainPane);
                            addConnectionEvent(sceneView);
                            sceneView.addEventHandler(InteractiveNodeEvent.REMOVED, event -> canvasViewModel.project.removeState(sceneViewModel.getScene()));
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
                            ConditionView conditionView = new ConditionView(conditionViewModel, mainPane);
                            addConnectionEvent(conditionView);
                            conditionView.addEventHandler(InteractiveNodeEvent.REMOVED, event -> canvasViewModel.project.removeCondition(conditionViewModel.getCondition()));
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
                            LineView lineView = new LineView(lineViewModel, mainPane);
                            lineView.addEventHandler(InteractiveNodeEvent.REMOVED, event -> canvasViewModel.project.removeLine(lineViewModel.getLine()));
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
        // allow node to be deleted using the delete key
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                for (InteractiveNode interactiveNode : mainPane.getSelectedNode()) {
                    if (interactiveNode instanceof SceneView) {
                        canvasViewModel.project.removeState(((SceneView) interactiveNode).getSceneViewModel().getScene());
                    } else if (interactiveNode instanceof ConditionView) {
                        canvasViewModel.project.removeCondition(((ConditionView) interactiveNode).getConditionViewModel().getCondition());
                    } else if (interactiveNode instanceof LineView) {
                        canvasViewModel.project.removeLine(((LineView) interactiveNode).getLineViewModel().getLine());
                    } else if (interactiveNode instanceof BeginSceneView) {
                        // we shouldn't delete begin from the canvas
                    } else {
                        throw new IllegalStateException("Found invalid object in the canvas!!!");
                    }
                }
            }
        });
    }

    private void addConnectionEvent(InteractiveNode node) {
        node.addEventFilter(InteractiveNodeEvent.CONNECTION_BEGIN, event -> {
            sourceNode = event.getSourceNode();
        });

        node.addEventFilter(InteractiveNodeEvent.CONNECTION_DONE, event -> {
            destNode = event.getDestinationNode();
            canvasViewModel.connect(sourceNode, destNode);
        });
    }
}
