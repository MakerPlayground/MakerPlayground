package io.makerplayground.generator.diagram;

import io.makerplayground.project.InteractiveModel;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.canvas.node.usersetting.DeviceMonitorPane;
import io.makerplayground.ui.canvas.node.usersetting.SceneDevicePropertyPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.util.List;
import java.util.stream.Collectors;

public class InteractiveDevicePropertyWindow extends PopOver {

    public InteractiveDevicePropertyWindow(List<ProjectDevice> devices, InteractiveModel model, Project project) {
        VBox vBox = new VBox();
        for (ProjectDevice projectDevice : devices) {
            if (projectDevice.getGenericDevice().hasAction()) {
                SceneDevicePropertyPane devicePropertyPane = new SceneDevicePropertyPane(model.getOrCreateUserSetting(projectDevice), project);
                vBox.getChildren().add(devicePropertyPane);
            }
            if (projectDevice.getGenericDevice().hasCondition() || projectDevice.getGenericDevice().hasValue()) {
                DeviceMonitorPane deviceMonitorPane = new DeviceMonitorPane(model, projectDevice);
                vBox.getChildren().add(deviceMonitorPane);
            }
        }
        vBox.setSpacing(10);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPadding(new Insets(20));

        setTitle(devices.stream().map(ProjectDevice::getName).collect(Collectors.joining(",")));
        setDetachable(true);
        setContentNode(vBox);
    }
}