package io.makerplayground.ui.canvas.node.usersetting;

import io.makerplayground.device.actual.ActualDevice;
import io.makerplayground.device.actual.IntegratedActualDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.ui.explorer.InteractiveModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.util.List;
import java.util.function.Consumer;

public class InteractiveDevicePropertyWindow extends PopOver {

    private Consumer<ProjectDevice> deviceConsumer;

    public InteractiveDevicePropertyWindow(List<ProjectDevice> devices, InteractiveModel model, Project project) {
        String actualDeviceName;
        if (devices.size() > 1) {
            ActualDevice parentDevice = ((IntegratedActualDevice) devices.get(0).getActualDevice()).getParent();
            for (ProjectDevice projectDevice : devices) {
                if (((IntegratedActualDevice) projectDevice.getActualDevice()).getParent() != parentDevice) {
                    throw new IllegalStateException("devices must be a list of integrated device of a controller");
                }
            }
            actualDeviceName = parentDevice.getBrand() + " " + parentDevice.getModel();
        } else {
            ActualDevice actualDevice = devices.get(0).getActualDevice();
            actualDeviceName = actualDevice.getBrand() + " " + actualDevice.getModel();
        }

        VBox vBox = new VBox();
        for (ProjectDevice projectDevice : devices) {
            if (projectDevice.getGenericDevice().hasAction()) {
                Button sendActionButton = new Button("Send Action");
                sendActionButton.setOnAction(event -> deviceConsumer.accept(projectDevice));

                DevicePropertyPane devicePropertyPane = new DevicePropertyPane(model.getUserSetting(projectDevice), project, true);
                devicePropertyPane.getChildren().add(sendActionButton);

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

        setTitle(actualDeviceName);
        setDetachable(true);
        setContentNode(vBox);
    }

    public void setOnSendActionButtonPressed(Consumer<ProjectDevice> deviceConsumer) {
        this.deviceConsumer = deviceConsumer;
    }
}

