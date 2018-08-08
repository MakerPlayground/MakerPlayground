package io.makerplayground.ui.canvas;

import io.makerplayground.project.Condition;
import io.makerplayground.project.Line;
import io.makerplayground.project.NodeElement;
import io.makerplayground.project.Scene;
import io.makerplayground.ui.canvas.node.InteractiveNodeEvent;
import io.makerplayground.ui.canvas.node.*;
import io.makerplayground.ui.canvas.helper.DynamicViewCreatorBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class CanvasView extends AnchorPane {
    @FXML private InteractivePane mainPane;
    @FXML private Button addStateBtn;
    @FXML private Button addConditionBtn;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomDefaultButton;
    @FXML private VBox zoomControl;

    @FXML private ContextMenu contextMenu;
    @FXML private MenuItem newSceneMenuItem;
    @FXML private MenuItem newConditionMenuItem;
    @FXML private MenuItem cutMenuItem;
    @FXML private MenuItem copyMenuItem;
    @FXML private MenuItem pasteMenuItem;
    @FXML private MenuItem deleteMenuItem;

    private final ObservableList<InteractiveNode> clipboard = FXCollections.observableArrayList();

    private final CanvasViewModel canvasViewModel;

    public CanvasView(CanvasViewModel canvasViewModel) {
        this.canvasViewModel = canvasViewModel;
        initView();
        initEvent();
    }

    private void initView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/canvas/CanvasView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BooleanBinding selectionGroupEmpty = Bindings.size(mainPane.getSelectionGroup().getSelected()).isEqualTo(0);
        cutMenuItem.disableProperty().bind(selectionGroupEmpty);
        copyMenuItem.disableProperty().bind(selectionGroupEmpty);
        pasteMenuItem.disableProperty().bind(Bindings.size(clipboard).isEqualTo(0));
        deleteMenuItem.disableProperty().bind(selectionGroupEmpty);

        BeginSceneView beginSceneView = new BeginSceneView(canvasViewModel.getBeginViewModel(), mainPane);
        addConnectionEvent(beginSceneView);
        mainPane.addChildren(beginSceneView);
    }

    private void initEvent() {
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SHIFT) {  // enable multiple selection when the shift key is pressed
                mainPane.getSelectionGroup().setMultipleSelection(true);
            } else if (event.isShortcutDown() && (event.getCode().equals(KeyCode.EQUALS) || event.getText().equals("+"))) {
                zoomInHandler();
            } else if (event.isShortcutDown() && event.getText().equals("-")) {
                zoomOutHandler();
            } else if (event.isShortcutDown() && event.getText().equals("0")) {
                zoomDefaultHandler();
            }
        });
        setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.SHIFT) {  // disable multiple selection when the shift key is released
                mainPane.getSelectionGroup().setMultipleSelection(false);
            }
        });

        // Dynamically generate the scene UI when scene list is changed
        new DynamicViewCreatorBuilder<InteractivePane, SceneViewModel, SceneView>()
                .setParent(mainPane)
                .setModelLoader(canvasViewModel.getPaneStateViewModel())
                .setViewFactory(sceneViewModel -> {
                    SceneView sceneView = new SceneView(sceneViewModel, mainPane);
                    addConnectionEvent(sceneView);
                    sceneView.addEventHandler(InteractiveNodeEvent.REMOVED, event -> canvasViewModel.project.removeScene(sceneViewModel.getScene()));
                    return sceneView;
                })
                .setNodeAdder(InteractivePane::addChildren)
                .setNodeRemover(InteractivePane::removeChildren)
                .createDynamicViewCreator();


        // Dynamically generate the condition UI when condition list is changed
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

        // Dynamically generate the line UI when line list is changed
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
    }

    private void newSceneHandler(double x, double y) {
        Scene newScene = canvasViewModel.project.newScene();
        newScene.setLeft(x);
        newScene.setTop(y);
    }

    private void newConditionHandler(double x, double y) {
        Condition newCondition = canvasViewModel.project.newCondition();
        newCondition.setLeft(x);
        newCondition.setTop(y);
    }

    @FXML
    private void cutHandler() {
        clipboard.clear();
        clipboard.addAll(mainPane.getSelectionGroup().getSelected());
        deleteHandler();
    }

    @FXML
    private void copyHandler() {
        clipboard.clear();
        clipboard.addAll(mainPane.getSelectionGroup().getSelected());
    }

    @FXML
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
                Scene newScene = canvasViewModel.project.newScene((Scene) element);
                newScene.setLeft(newX);
                newScene.setTop(newY);
                elementsMap.put(element,newScene);
            } else {
                Condition newCondition = canvasViewModel.project.newCondition((Condition) element);
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

    @FXML
    private void deleteHandler() {
        // clone the list as the selected list will changed in the loop which will cause NoSuchElementException to be thrown
        List<InteractiveNode> removeList = new ArrayList<>(mainPane.getSelectionGroup().getSelected());
        for (InteractiveNode interactiveNode : removeList) {
            if (interactiveNode instanceof SceneView) {
                canvasViewModel.project.removeScene(((SceneView) interactiveNode).getSceneViewModel().getScene());
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

    @FXML
    private void zoomInHandler() {
        if(mainPane.getScale()< 5) {
            mainPane.setScale(mainPane.getScale() + 0.1);
        }
    }

    @FXML
    private void zoomOutHandler() {
        if(mainPane.getScale()> 0.5) {
            mainPane.setScale(mainPane.getScale() - 0.1);
        }
    }

    @FXML
    private void zoomDefaultHandler() {
        mainPane.setScale(1);
    }

    private void addConnectionEvent(InteractiveNode node) {
        node.addEventFilter(InteractiveNodeEvent.CONNECTION_DONE, event ->
            canvasViewModel.connect(event.getSourceNode(), event.getDestinationNode())
        );
    }

    @FXML
    private void newSceneContextMenuHandler() {
        newSceneHandler(mainPane.getMouseX(), mainPane.getMouseY());
    }

    @FXML
    private void newConditionContextMenuHandler() {
        newConditionHandler(mainPane.getMouseX(), mainPane.getMouseY());
    }

    @FXML
    private void addStateButtonHandler() {
        newSceneHandler(mainPane.getViewportMinX() + 50, mainPane.getViewportMinY() + 50);
    }

    @FXML
    private void addConditionButtonHandler() {
        newConditionHandler(mainPane.getViewportMinX() + 50, mainPane.getViewportMinY() + 50);
    }
}
