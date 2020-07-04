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

    @Getter private List<Scene> scenes = new ArrayList<Scene>();
    @Getter private List<Condition> conditions = new ArrayList<Condition>();
    @Getter private List<Delay> delays = new ArrayList<Delay>();
    @Getter private List<Line> lines = new ArrayList<Line>();
    @Getter private Set<ProjectDevice> devices = new HashSet<ProjectDevice>();

    public DiagramClipboardData(List<InteractiveNode> interactiveNodes)
    {
        List<Scene> sceneList = new ArrayList<Scene>();
        List<Condition> conditionList = new ArrayList<Condition>();
        List<Delay> delayList = new ArrayList<Delay>();
        List<Line> lineList = new ArrayList<Line>();
        Set<ProjectDevice> deviceList = new HashSet<ProjectDevice>();
        for (InteractiveNode node : interactiveNodes) {
            if(node instanceof LineView)
            {
                Line line = ((LineView) node).getLineViewModel().getLine();
                lineList.add(line);
            }
            if(node instanceof SceneView)
            {
                Scene scene = ((SceneView) node).getSceneViewModel().getScene();
                ObservableList<UserSetting> userSettings = scene.getAllSettings();
                sceneList.add(scene);
                for(UserSetting userSetting : userSettings)
                {
                    ProjectDevice projectDevice = userSetting.getDevice();
                    if(!VirtualProjectDevice.getDevices().contains(projectDevice))
                    {
                        deviceList.add(projectDevice);
                    }
                    Map<ProjectDevice,Set<Value>> deviceSetMap = userSetting.getAllValueUsed();
                    Set<ProjectDevice> keys = deviceSetMap.keySet();
                    deviceList.addAll(keys);
                }
            }
            else if(node instanceof ConditionView)
            {
                Condition condition = ((ConditionView) node).getConditionViewModel().getCondition();
                conditionList.add(condition);

                ObservableList<UserSetting> userSettings = condition.getSetting();
                for(UserSetting userSetting : userSettings)
                {

                    ProjectDevice projectDevice = userSetting.getDevice();
                    if(!VirtualProjectDevice.getDevices().contains(projectDevice))
                    {
                        deviceList.add(projectDevice);
                    }

                    Map<ProjectDevice,Set<Value>> deviceSetMap = userSetting.getAllValueUsed();
                    Set<ProjectDevice> keys = deviceSetMap.keySet();
                    deviceList.add(userSetting.getDevice());
                    deviceList.addAll(keys);
                }

            }
            else if(node instanceof DelayView)
            {
                Delay delay = ((DelayView) node).getDelayViewModel().getDelay();
                delayList.add(delay);
            }
        }

        this.scenes = sceneList;
        this.conditions = conditionList;
        this.delays = delayList;
        this.lines = lineList;
        this.devices = deviceList;
    }

}
