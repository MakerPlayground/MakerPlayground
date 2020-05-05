package io.makerplayground.generator.diagram;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.project.InteractiveModel;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.canvas.node.usersetting.DeviceMonitorPane;
import io.makerplayground.ui.canvas.node.usersetting.SceneDevicePropertyPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InteractiveDevicePropertyWindow extends PopOver {

    public InteractiveDevicePropertyWindow(Map<ProjectDevice, ActualDevice> devices, InteractiveModel model, Project project) {
        VBox vBox = new VBox();
        for (ProjectDevice projectDevice : devices.keySet()) {
            ActualDevice actualDevice = devices.get(projectDevice);
            if (projectDevice.getGenericDevice().hasAction()) {
                SceneDevicePropertyPane devicePropertyPane = new SceneDevicePropertyPane(model.getOrCreateActionUserSetting(projectDevice), project, actualDevice);
                vBox.getChildren().add(devicePropertyPane);
            }
            if (projectDevice.getGenericDevice().hasCondition() || projectDevice.getGenericDevice().hasValue()) {
                DeviceMonitorPane deviceMonitorPane = new DeviceMonitorPane(model, projectDevice, actualDevice);
                vBox.getChildren().add(deviceMonitorPane);
            }
        }
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPadding(new Insets(20));

        setTitle(devices.keySet().stream().map(ProjectDevice::getName).collect(Collectors.joining(",")));
        setDetachable(true);

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setPrefHeight(USE_COMPUTED_SIZE);
        scrollPane.setMaxHeight(350.0);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        setContentNode(scrollPane);
    }
}