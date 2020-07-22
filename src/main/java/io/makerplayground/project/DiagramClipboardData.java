package io.makerplayground.project;

import io.makerplayground.device.shared.Value;
import io.makerplayground.ui.canvas.LineView;
import io.makerplayground.ui.canvas.node.ConditionView;
import io.makerplayground.ui.canvas.node.DelayView;
import io.makerplayground.ui.canvas.node.InteractiveNode;
import io.makerplayground.ui.canvas.node.SceneView;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.*;

public class DiagramClipboardData {

    @Getter private final List<Scene> scenes = new ArrayList<>();
    @Getter private final List<Condition> conditions = new ArrayList<>();
    @Getter private final List<Delay> delays = new ArrayList<>();
    @Getter private final List<Line> lines = new ArrayList<>();
    @Getter private final Set<ProjectDevice> devices = new HashSet<>();

    public DiagramClipboardData(List<Scene> scenes, List<Condition> conditions, List<Delay> delays, List<Line> lines) {
        this.scenes.addAll(scenes);
        this.conditions.addAll(conditions);
        this.delays.addAll(delays);
        this.lines.addAll(lines);
    }


    public DiagramClipboardData(List<InteractiveNode> interactiveNodes) {
        for (InteractiveNode node : interactiveNodes) {
            if (node instanceof LineView) {
                Line line = ((LineView) node).getLineViewModel().getLine();
                lines.add(line);
            }
            if (node instanceof SceneView) {
                Scene scene = ((SceneView) node).getSceneViewModel().getScene();
                ObservableList<UserSetting> userSettings = scene.getAllSettings();
                scenes.add(scene);
                for (UserSetting userSetting : userSettings) {
                    ProjectDevice projectDevice = userSetting.getDevice();
                    if (!VirtualProjectDevice.getDevices().contains(projectDevice)) {
                        devices.add(projectDevice);
                    }
                    Map<ProjectDevice,Set<Value>> deviceSetMap = userSetting.getAllValueUsedByActualProjectDevice();
                    Set<ProjectDevice> keys = deviceSetMap.keySet();
                    devices.addAll(keys);
                }
            }
            else if (node instanceof ConditionView) {
                Condition condition = ((ConditionView) node).getConditionViewModel().getCondition();
                conditions.add(condition);
                ObservableList<UserSetting> userSettings = condition.getSetting();
                for (UserSetting userSetting : userSettings) {
                    ProjectDevice projectDevice = userSetting.getDevice();
                    if (!VirtualProjectDevice.getDevices().contains(projectDevice)) {
                        devices.add(projectDevice);
                    }
                    Map<ProjectDevice,Set<Value>> deviceSetMap = userSetting.getAllValueUsedByActualProjectDevice();
                    Set<ProjectDevice> keys = deviceSetMap.keySet();
                    devices.add(userSetting.getDevice());
                    devices.addAll(keys);
                }
            }
            else if (node instanceof DelayView) {
                Delay delay = ((DelayView) node).getDelayViewModel().getDelay();
                delays.add(delay);
            }
        }
    }

    public List<NodeElement> getAllNode() {
        List<NodeElement> elements = new ArrayList<>();
        elements.addAll(scenes);
        elements.addAll(conditions);
        elements.addAll(delays);
        return elements;
    }

}
