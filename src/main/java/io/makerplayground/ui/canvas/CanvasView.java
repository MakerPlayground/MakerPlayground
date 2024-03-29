/*
 * Copyright (c) 2019. The Maker Playground Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.makerplayground.ui.canvas;

import io.makerplayground.project.*;
import io.makerplayground.ui.canvas.helper.DynamicViewCreatorBuilder;
import io.makerplayground.ui.canvas.node.*;
import io.makerplayground.util.OSInfo;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class CanvasView extends AnchorPane {
    @FXML private InteractivePane mainPane;
    @FXML private Button addSceneBtn;
    @FXML private Button addConditionBtn;
    @FXML private Button addDelayBtn;
    @FXML private Button addBeginBtn;
    @FXML private Button zoomInButton;
    @FXML private Button zoomOutButton;
    @FXML private Button zoomDefaultButton;
    @FXML private VBox zoomControl;

    @FXML private ContextMenu contextMenu;
    @FXML private MenuItem newSceneMenuItem;
    @FXML private MenuItem newConditionMenuItem;
    @FXML private MenuItem newDelayMenuItem;
    @FXML private MenuItem newBeginMenuItem;
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

        // macos uses both backspace and delete as to delete something and there isn't any platform independent way to handle this in javafx
        if (OSInfo.getOs() == OSInfo.OS.MAC) {
            deleteMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE));
            addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.DELETE) {
                    deleteHandler();
                }
            });
        } else {
            deleteMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));
        }
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
                .setModelLoader(canvasViewModel.getSceneViewModelCreator())
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
                .setModelLoader(canvasViewModel.getConditionViewModelCreator())
                .setViewFactory(conditionViewModel -> {
                    ConditionView conditionView = new ConditionView(conditionViewModel, mainPane);
                    addConnectionEvent(conditionView);
                    conditionView.addEventHandler(InteractiveNodeEvent.REMOVED, event -> canvasViewModel.project.removeCondition(conditionViewModel.getCondition()));
                    return conditionView;
                })
                .setNodeAdder(InteractivePane::addChildren)
                .setNodeRemover(InteractivePane::removeChildren)
                .createDynamicViewCreator();

        // Dynamically generate the condition UI when condition list is changed
        new DynamicViewCreatorBuilder<InteractivePane, DelayViewModel, DelayView>()
                .setParent(mainPane)
                .setModelLoader(canvasViewModel.getDelayViewModelCreator())
                .setViewFactory(delayViewModel -> {
                    DelayView delayView = new DelayView(delayViewModel, mainPane);
                    addConnectionEvent(delayView);
                    delayView.addEventHandler(InteractiveNodeEvent.REMOVED, event -> canvasViewModel.project.removeDelay(delayViewModel.getDelay()));
                    return delayView;
                })
                .setNodeAdder(InteractivePane::addChildren)
                .setNodeRemover(InteractivePane::removeChildren)
                .createDynamicViewCreator();

        // Dynamically generate the taskNode UI when condition list is changed
        new DynamicViewCreatorBuilder<InteractivePane, BeginViewModel, BeginView>()
                .setParent(mainPane)
                .setModelLoader(canvasViewModel.getBeginViewModelCreator())
                .setViewFactory(beginViewModel -> {
                    BeginView beginView = new BeginView(beginViewModel, mainPane);
                    addConnectionEvent(beginView);
                    beginView.addEventHandler(InteractiveNodeEvent.REMOVED, event -> canvasViewModel.project.removeBegin(beginView.getBegin()));
                    return beginView;
                })
                .setNodeAdder(InteractivePane::addChildren)
                .setNodeRemover(InteractivePane::removeChildren)
                .createDynamicViewCreator();

        // Dynamically generate the line UI when line list is changed
        new DynamicViewCreatorBuilder<InteractivePane, LineViewModel, LineView>()
                .setParent(mainPane)
                .setModelLoader(canvasViewModel.getLineViewModelCreator())
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

    private void newDelayHandler(double x, double y) {
        Delay newDelay = canvasViewModel.project.newDelay();
        newDelay.setLeft(x);
        newDelay.setTop(y);
    }

    private void newBeginHandler(double x, double y) {
        Begin newTask = canvasViewModel.project.newBegin();
        newTask.setLeft(x);
        newTask.setTop(y);
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
        List<NodeElement> elements = clipboard.stream()
                .filter(interactiveNode -> (interactiveNode instanceof SceneView) || (interactiveNode instanceof ConditionView) || (interactiveNode instanceof DelayView))
                .map(interactiveNode -> {
                    if (interactiveNode instanceof SceneView) {
                        return ((SceneView) interactiveNode).getSceneViewModel().getScene();
                    } else if (interactiveNode instanceof ConditionView) {
                        return ((ConditionView) interactiveNode).getConditionViewModel().getCondition();
                    } else {
                        return ((DelayView) interactiveNode).getDelayViewModel().getDelay();
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
            } else if (element instanceof Condition) {
                Condition newCondition = canvasViewModel.project.newCondition((Condition) element);
                newCondition.setLeft(newX);
                newCondition.setTop(newY);
                elementsMap.put(element,newCondition);
            } else if (element instanceof Delay) {
                Delay newDelay = canvasViewModel.project.newDelay((Delay) element);
                newDelay.setLeft(newX);
                newDelay.setTop(newY);
                elementsMap.put(element,newDelay);
            }
        }

        for (InteractiveNode node : clipboard) {
            if (node instanceof LineView) {
                Line line = ((LineView) node).getLineViewModel().getLine();
                if (elements.contains(line.getSource()) && elements.contains(line.getDestination())){
                    canvasViewModel.project.addLine(elementsMap.get(line.getSource()), elementsMap.get(line.getDestination()));
                } else if (elements.contains(line.getSource())){
                    canvasViewModel.project.addLine(elementsMap.get(line.getSource()), line.getDestination());
                } else if (elements.contains(line.getDestination())){
                    canvasViewModel.project.addLine(line.getSource(), elementsMap.get(line.getDestination()));
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
            } else if (interactiveNode instanceof DelayView) {
                canvasViewModel.project.removeDelay(((DelayView) interactiveNode).getDelayViewModel().getDelay());
            } else if (interactiveNode instanceof LineView) {
                canvasViewModel.project.removeLine(((LineView) interactiveNode).getLineViewModel().getLine());
            } else if (interactiveNode instanceof BeginView) {
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

    private static final double AUTO_ARRANGE_SPACE_X = 50;
    private static final double AUTO_ARRANGE_SPACE_Y = 150;
    private static final double AUTO_ARRANGE_START_X = 50;
    private static final double AUTO_ARRANGE_MIN_Y = 125;

    private double setChildrenPosition(NodeElement node, double currentX, double currentY, Set<NodeElement> visitedNode) {
        node.setLeft(currentX);
        node.setTop(node.getTop() + currentY - node.getDestPortY());
        currentX = node.getDestPortX() + AUTO_ARRANGE_SPACE_X;

        List<Line> lines = canvasViewModel.getProject().getLinesFromSource(node).stream()
                .filter(l -> !visitedNode.contains(l.getDestination()))
                .sorted(Comparator.comparingDouble(l -> l.getDestination().getTop()))   // retain original node order vertically
                .collect(Collectors.toUnmodifiableList());
        if (lines.isEmpty()) {
            visitedNode.add(node);
            return AUTO_ARRANGE_SPACE_Y;
        } else {
            int offsetY = 0;
            // use two loops to handle the case when the node has connections from both node in the upper and lower level
            // (count as number of intermediate node to the begin node) in this case we treat this node as it is in the
            // upper level not as a child of node in the lower level and align it with it's sibling in the upper level
            for (Line l : lines) {
                visitedNode.add(l.getDestination());
            }
            for (Line l : lines) {
                offsetY += setChildrenPosition(l.getDestination(), currentX, currentY + offsetY, visitedNode);
            }
            return offsetY;
        }
    }

    private double getLeafNodeCount(NodeElement current, Set<NodeElement> visitedNode) {
        List<Line> lines = canvasViewModel.getProject().getLinesFromSource(current).stream()
                .filter(l -> !visitedNode.contains(l.getDestination()))
                .collect(Collectors.toUnmodifiableList());
        if (lines.isEmpty()) {
            visitedNode.add(current);
            return 1;
        } else {
            int count = 0;
            // use two loop to avoid visiting node multiple times (in case that the node is also a child of some node in the lower level)
            for (Line l : lines) {
                visitedNode.add(l.getDestination());
            }
            for (Line l : lines) {
                count += getLeafNodeCount(l.getDestination(), visitedNode);
            }
            return count;
        }
    }

    @FXML
    private void autoDiagramHandler() {
        List<Begin> beginList = new ArrayList<>(canvasViewModel.getProject().getBegin());
        beginList.sort(Comparator.comparingDouble(NodeElement::getTop));

        double diagramHeight = 0;
        for (Begin begin : beginList) {
            diagramHeight += (getLeafNodeCount(begin, new HashSet<>()) - 1) * AUTO_ARRANGE_SPACE_Y;
        }

        double canvasHeight = mainPane.getBoundsInParent().getHeight();
        double startY = Math.max((canvasHeight - diagramHeight) / 2, AUTO_ARRANGE_MIN_Y);
        for (Begin begin : beginList) {
            startY += setChildrenPosition(begin, AUTO_ARRANGE_START_X, startY, new HashSet<>());
        }
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
    private void newDelayContextMenuHandler() {
        newDelayHandler(mainPane.getMouseX(), mainPane.getMouseY());
    }

    @FXML
    private void newBeginContextMenuHandler() {
        newBeginHandler(mainPane.getMouseX(), mainPane.getMouseY());
    }

    @FXML
    private void addSceneButtonHandler() {
        newSceneHandler(mainPane.getViewportMinX() + 50, mainPane.getViewportMinY() + 50);
    }

    @FXML
    private void addConditionButtonHandler() {
        newConditionHandler(mainPane.getViewportMinX() + 50, mainPane.getViewportMinY() + 50);
    }

    @FXML
    private void addDelayButtonHandler() {
        newDelayHandler(mainPane.getViewportMinX() + 50, mainPane.getViewportMinY() + 50);
    }

    @FXML
    private void addBeginButtonHandler() {
        newBeginHandler(mainPane.getViewportMinX() + 50, mainPane.getViewportMinY() + 50);
    }
}
