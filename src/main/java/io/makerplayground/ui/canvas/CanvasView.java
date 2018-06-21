package io.makerplayground.ui.canvas;

import io.makerplayground.project.Condition;
import io.makerplayground.project.Line;
import io.makerplayground.project.NodeElement;
import io.makerplayground.project.Scene;
import io.makerplayground.ui.canvas.event.InteractiveNodeEvent;
import io.makerplayground.uihelper.DynamicViewCreator;
import io.makerplayground.uihelper.DynamicViewCreatorBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class CanvasView extends AnchorPane {
    private final InteractivePane mainPane = new InteractivePane();
    private final Button addStateBtn = new Button();
    private final Button addConditionBtn = new Button();
//    private final TextField zoomTextField = new TextField();
    private final Button zoomInButton = new Button();
    private final Button zoomOutButton = new Button();
    private final Button zoomDefaultButton = new Button();

    private final ObservableList<InteractiveNode> clipboard = FXCollections.observableArrayList();

    private final CanvasViewModel canvasViewModel;

    public CanvasView(CanvasViewModel canvasViewModel) {
        this.canvasViewModel = canvasViewModel;
        initView();
        initEvent();
    }

    private void initView() {
        getStylesheets().add(getClass().getResource("/css/CanvasView.css").toExternalForm());

        addStateBtn.setText("+ Add Scene");
        addStateBtn.setId("addStateBtn");
        addStateBtn.setOnAction(event -> canvasViewModel.project.addState());
        AnchorPane.setTopAnchor(addStateBtn, 20.0);
        AnchorPane.setRightAnchor(addStateBtn, 20.0);

        addConditionBtn.setText("+ Add Condition");
        addConditionBtn.setId("addConditionBtn");
        addConditionBtn.setOnAction(event -> canvasViewModel.project.addCondition());
        AnchorPane.setTopAnchor(addConditionBtn, 65.0);
        AnchorPane.setRightAnchor(addConditionBtn, 20.0);

        zoomDefaultButton.setId("zoomDefaultButton");
        zoomDefaultButton.setPrefSize(25,25);
        zoomDefaultButton.setOnAction(event -> mainPane.setScale(1));
//        AnchorPane.setBottomAnchor(zoomDefaultButton, 20.0);
//        AnchorPane.setRightAnchor(zoomDefaultButton, 125.0);

        zoomInButton.setId("zoomInButton");
        zoomInButton.setPrefSize(25,25);
        zoomInButton.setOnAction(event -> {
            if(mainPane.getScale()< 5)
                mainPane.setScale(mainPane.getScale() + 0.1);
        });
//        AnchorPane.setBottomAnchor(zoomInButton, 20.0);
//        AnchorPane.setRightAnchor(zoomInButton, 20.0);

        zoomOutButton.setId("zoomOutButton");
        zoomOutButton.setPrefSize(25,25);
        zoomOutButton.setOnAction(event -> {
            if(mainPane.getScale()> 0.5)
                mainPane.setScale(mainPane.getScale() - 0.1);
        });
//        AnchorPane.setBottomAnchor(zoomOutButton, 20.0);
//        AnchorPane.setRightAnchor(zoomOutButton, 95.0);

        VBox zoomControl = new VBox();
        zoomControl.setSpacing(0);
        zoomControl.getChildren().addAll(zoomInButton, zoomDefaultButton,zoomOutButton);
        AnchorPane.setRightAnchor(zoomControl, 20.0);
        AnchorPane.setBottomAnchor(zoomControl, 20.0);

//        zoomTextField.setText(String.valueOf((int) (mainPane.getScale() * 100)) + "%");
//        zoomTextField.setPrefWidth(40.0);
//        zoomTextField.textProperty().addListener((observable, oldValue, newValue) -> {
//            try {
//                if (!newValue.isEmpty()) {
//                    if (newValue.endsWith("%")) {
//                        newValue = newValue.substring(0, newValue.indexOf("%"));
//                    }
//                    int scale = Integer.parseInt(newValue);
//                    if (scale > 0) {
//                        mainPane.setScale(scale / 100.0);
//                    }
//                }
//            } catch (NumberFormatException e) {
//                zoomTextField.setText(oldValue);
//            }
//        });
//        mainPane.scaleProperty().addListener((observable, oldValue, newValue) ->
//                zoomTextField.setText(String.valueOf((int) (newValue.doubleValue() * 100)) + "%"));
//        AnchorPane.setBottomAnchor(zoomTextField, 20.0);
//        AnchorPane.setRightAnchor(zoomTextField, 50.0);

        ContextMenu contextMenu = new ContextMenu();
        BooleanBinding selectionGroupEmpty = Bindings.size(mainPane.getSelectionGroup().getSelected()).isEqualTo(0);

        MenuItem cutMenuItem = new MenuItem("Cut");
        cutMenuItem.setOnAction(event -> cutHandler());
        cutMenuItem.setAccelerator(KeyCombination.valueOf("Shortcut+X"));
        cutMenuItem.disableProperty().bind(selectionGroupEmpty);

        MenuItem copyMenuItem = new MenuItem("Copy");
        copyMenuItem.setOnAction(event -> copyHandler());
        copyMenuItem.setAccelerator(KeyCombination.valueOf("Shortcut+C"));
        copyMenuItem.disableProperty().bind(selectionGroupEmpty);

        MenuItem pasteMenuItem = new MenuItem("Paste");
        pasteMenuItem.setOnAction(event -> pasteHandler());
        pasteMenuItem.setAccelerator(KeyCombination.valueOf("Shortcut+V"));
        pasteMenuItem.disableProperty().bind(Bindings.size(clipboard).isEqualTo(0));

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(event -> deleteHandler());
        deleteMenuItem.setAccelerator(KeyCombination.valueOf("Delete"));
        deleteMenuItem.disableProperty().bind(selectionGroupEmpty);

        contextMenu.getItems().addAll(cutMenuItem, copyMenuItem, pasteMenuItem, new SeparatorMenuItem(), deleteMenuItem);
        mainPane.setContextMenu(contextMenu);

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
                        .setNodeAdder((parent, node) -> {
                            parent.addChildren(node);
                            node.toBack();  // draw line below other elements so that it won't block mouse event on in/out port
                        })
                        .setNodeRemover(InteractivePane::removeChildren)
                        .createDynamicViewCreator();

        AnchorPane.setTopAnchor(mainPane, 0.0);
        AnchorPane.setRightAnchor(mainPane, 0.0);
        AnchorPane.setBottomAnchor(mainPane, 0.0);
        AnchorPane.setLeftAnchor(mainPane, 0.0);
        getChildren().addAll(mainPane, addStateBtn, addConditionBtn, zoomControl);
    }

    private void initEvent() {
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SHIFT) {  // enable multiple selection when the shift key is pressed
                mainPane.getSelectionGroup().setMultipleSelection(true);
            }
        });
        setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SHIFT) {  // disable multiple selection when the shift key is released
                mainPane.getSelectionGroup().setMultipleSelection(false);
            }
        });
    }

    private void cutHandler() {
        clipboard.clear();
        clipboard.addAll(mainPane.getSelectionGroup().getSelected());
        deleteHandler();
    }

    private void copyHandler() {
        clipboard.clear();
        clipboard.addAll(mainPane.getSelectionGroup().getSelected());
    }

    private void pasteHandler() {
        if (clipboard.isEmpty()) {
            return;
        }
        // extract model from view
        List<NodeElement> elements = clipboard.stream().filter(interactiveNode -> (interactiveNode instanceof SceneView) || (interactiveNode instanceof ConditionView))
                .map(interactiveNode -> {
                    if (interactiveNode instanceof SceneView) {
                        return ((SceneView) interactiveNode).getSceneViewModel().getScene();
                    } else {
                        return ((ConditionView) interactiveNode).getConditionViewModel().getCondition();
                    }
                }).collect(Collectors.toList());

        // find group min x,y (x,y of the left-top most elements in the group selection)
        double minX = elements.stream().mapToDouble(NodeElement::getLeft).min().getAsDouble();
        double minY = elements.stream().mapToDouble(NodeElement::getTop).min().getAsDouble();

        Map<NodeElement, NodeElement> elementsMap = new HashMap<>();

        // add elements in clipboard to the canvas
        for (NodeElement element : elements) {
            // new x,y is the current mouse position plus the offset (old x,y - group minimum x,y)
            double newX = element.getLeft() - minX + mainPane.getMouseX();
            double newY = element.getTop() - minY + mainPane.getMouseY();
            if (element instanceof Scene) {
                Scene newScene = canvasViewModel.project.addState((Scene) element);
                newScene.setLeft(newX);
                newScene.setTop(newY);
                elementsMap.put(element,newScene);
            } else {
                Condition newCondition = canvasViewModel.project.addCondition((Condition) element);
                newCondition.setLeft(newX);
                newCondition.setTop(newY);
                elementsMap.put(element,newCondition);
            }
        }

        for (NodeElement element : elements) {
            List<Line> lines = canvasViewModel.project.getLineFrom(element);
            for (Line l : lines) {
                if (elementsMap.containsKey(l.getDestination())) {
                    canvasViewModel.project.addLine(elementsMap.get(element), elementsMap.get(l.getDestination()));
                }
            }
        }
    }

    private void deleteHandler() {
        // clone the list as the selected list will changed in the loop which will cause NoSuchElementException to be thrown
        List<InteractiveNode> removeList = new ArrayList<>(mainPane.getSelectionGroup().getSelected());
        for (InteractiveNode interactiveNode : removeList) {
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

    private void addConnectionEvent(InteractiveNode node) {
        node.addEventFilter(InteractiveNodeEvent.CONNECTION_DONE, event ->
            canvasViewModel.connect(event.getSourceNode(), event.getDestinationNode())
        );
    }
}
