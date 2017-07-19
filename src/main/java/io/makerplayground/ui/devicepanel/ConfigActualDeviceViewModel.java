package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.Action;
import io.makerplayground.device.Constraint;
import io.makerplayground.device.Device;
import io.makerplayground.device.Parameter;
import io.makerplayground.generator.validGenericDevice;
import io.makerplayground.helper.Peripheral;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Created by tanyagorn on 7/11/2017.
 */
public class ConfigActualDeviceViewModel {
    private final Project project;
    private final Map<ProjectDevice, List<Device>> compatibleDeviceList;

    public ConfigActualDeviceViewModel(Project project) {
        this.project = project;
        this.compatibleDeviceList = validGenericDevice.getSupportedDeviceList(project);
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

    // Must be called after selected controller
    public Map<ProjectDevice, List<Peripheral>> getDeviceCompatiblePort() {
        Map<ProjectDevice, List<Peripheral>> result = new HashMap<>();

        if (project.getController().getController() == null)
        {
            for (ProjectDevice projectDevice : project.getAllDevice()) {
                result.put(projectDevice, Collections.emptyList());
            }
            return result;
        }

        List<Peripheral> processorPort = project.getController().getController().getConnectivity();

        for (ProjectDevice projectDevice : project.getAllDevice()) {
            //connection from this device (key) to the processor (value)
            for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
                if (processorPort.contains(p))
                    processorPort.remove(p);
            }
        }

        for (ProjectDevice projectDevice : project.getAllDevice()) {
            List<Peripheral> possibleDevice = new ArrayList<>();
            if (!projectDevice.isAutoSelectDevice() && (projectDevice.getActualDevice() != null)) { // calculate possible only if actual device is selected
                for (Peripheral p : projectDevice.getDeviceConnection().keySet()) {
                    possibleDevice.add(p);
                }
                for (Peripheral p : processorPort) {
                    if (projectDevice.getActualDevice().getConnectivity().get(0).getType() == p.getType()) {
                        possibleDevice.add(p);
                    }
                }
            }
            result.put(projectDevice, possibleDevice);
        }

        return result;
    }
}
