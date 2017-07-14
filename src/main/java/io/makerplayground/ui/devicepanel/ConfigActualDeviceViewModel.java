package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.Device;
import io.makerplayground.generator.validGenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Map;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {
    private final Project project;
    private final Map<ProjectDevice, List<Device>> compatibleDeviceList;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = validGenericDevice.getSupportedDeviceList(project);
        System.out.println(this.compatibleDeviceList);
    }

    public ObservableList<ProjectDevice> getOutputDevice() {
        return project.getOutputDevice();
    }

    public ObservableList<ProjectDevice> getInputDevice() {
        return project.getInputDevice();
    }

    public Map<ProjectDevice, List<Device>> getCompatibleDeviceList() {
        return compatibleDeviceList;
    }

    public List<Device> getCompatibleDevice(ProjectDevice projectDevice) {
        List<Device> compat = compatibleDeviceList.get(projectDevice);
        return compat;
    }

    public void setDevice(ProjectDevice projectDevice, Device device) {
        projectDevice.setActualDevice(device);
    }

    public ObservableList<ProjectDevice> getAllDevice() {
        return project.getAllDevice();
    }
}
