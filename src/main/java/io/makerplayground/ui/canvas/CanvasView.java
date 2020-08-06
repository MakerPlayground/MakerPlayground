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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.generic.GenericDevice;
import io.makerplayground.device.shared.DelayUnit;
import io.makerplayground.device.shared.Value;
import io.makerplayground.project.*;
import io.makerplayground.ui.canvas.helper.DynamicViewCreatorBuilder;
import io.makerplayground.ui.canvas.node.*;
import io.makerplayground.util.OSInfo;
import io.makerplayground.version.ProjectVersionControl;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.OpenOption;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.WindowEvent;


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

//    private final ObservableList<InteractiveNode> clipboard = FXCollections.observableArrayList();
    private final Clipboard clipboardSystem = Clipboard.getSystemClipboard();
    private final DataFormat appMakerPlayGround = new DataFormat("application/makerplayground");
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

        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {
                pasteMenuItem.setDisable(clipboardSystem.getContent(appMakerPlayGround) == null);
            }
        });
        cutMenuItem.disableProperty().bind(selectionGroupEmpty);
        copyMenuItem.disableProperty().bind(selectionGroupEmpty);
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
//        clipboard.clear();
//        clipboard.addAll(mainPane.getSelectionGroup().getSelected());
        copyHandler();
        deleteHandler();
    }

    @FXML
    private void copyHandler() {
        if (canvasViewModel.getProject().getProjectConfiguration().getController() != null) {
            ClipboardContent content = new ClipboardContent();
            ObjectMapper objectMapper = new ObjectMapper();

            DiagramClipboardData diagramClipboardData = new DiagramClipboardData(mainPane.getSelectionGroup().getSelected(),canvasViewModel.project.getProjectName());

            try {
                String json = objectMapper.writeValueAsString(diagramClipboardData);
                content.put(appMakerPlayGround,json);
            } catch (IOException e) {
                e.printStackTrace();
            }

            clipboardSystem.setContent(content);
        }
    }

    @FXML
    private void pasteHandler() {
        String modelString = (String) clipboardSystem.getContent(appMakerPlayGround);
        if (modelString != null && canvasViewModel.getProject().getProjectConfiguration().getController() != null) {
            ObjectMapper objectMapper = new ObjectMapper();

            SimpleModule module = new SimpleModule();
            module.addDeserializer(DiagramClipboardData.class, new DiagramClipboardDataDeserializer(canvasViewModel.getProject()));
            objectMapper.registerModule(module);

            List<NodeElement> elements = new ArrayList<>();
            List<Line> lines = new ArrayList<>();
            try {
                JsonNode root = objectMapper.readTree(modelString);
                DiagramClipboardData diagramClipboardData = objectMapper.readValue(root.traverse(),new TypeReference<DiagramClipboardData>() {});
                elements.addAll(diagramClipboardData.getScenes());
                elements.addAll(diagramClipboardData.getConditions());
                elements.addAll(diagramClipboardData.getDelays());
                lines.addAll(diagramClipboardData.getLines());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(!elements.isEmpty()) {
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
                        elementsMap.put(element, newScene);
                    } else if (element instanceof Condition) {
                        Condition newCondition = canvasViewModel.project.newCondition((Condition) element);
                        newCondition.setLeft(newX);
                        newCondition.setTop(newY);
                        elementsMap.put(element, newCondition);
                    } else if (element instanceof Delay) {
                        Delay newDelay = canvasViewModel.project.newDelay((Delay) element);
                        newDelay.setLeft(newX);
                        newDelay.setTop(newY);
                        elementsMap.put(element, newDelay);
                    }
                }

                for(Line line : lines)
                {
                    NodeElement source = null;
                    NodeElement destination = null;
                    for(NodeElement nodeElement : elements)
                    {
                        if(nodeElement.getName().equalsIgnoreCase(line.getSource().getName()))
                        {
                            source = nodeElement;
                        }
                        else if(nodeElement.getName().equalsIgnoreCase(line.getDestination().getName()))
                        {
                            destination = nodeElement;
                        }
                        if(source != null && destination != null)
                        {
                            canvasViewModel.project.addLine(elementsMap.get(source), elementsMap.get(destination));
                            break;
                        }
                    }
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
