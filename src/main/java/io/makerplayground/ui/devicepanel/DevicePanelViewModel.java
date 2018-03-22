package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.uihelper.DynamicViewModelCreator;

import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelViewModel {
    private final Project project;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> inputChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> outputChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> connectivityChildViewModel;

    public DevicePanelViewModel(Project project) {
        this.project = project;
        this.inputChildViewModel = new DynamicViewModelCreator<>(project.getSensor(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
        this.outputChildViewModel = new DynamicViewModelCreator<>(project.getActuator(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
        this.connectivityChildViewModel = new DynamicViewModelCreator<>(project.getConnectivity(), projectDevice -> new DevicePanelIconViewModel(projectDevice, project));
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getInputChildViewModel() {
        return inputChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getOutputChildViewModel() {
        return outputChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getConnectivityChildViewModel() {
        return connectivityChildViewModel;
    }

    public boolean removeOutputDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeActuator(deviceToBeRemoved);
    }

    public boolean removeInputDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeSensor(deviceToBeRemoved);
    }

    public boolean removeConnectivityDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeConnectivity(deviceToBeRemoved);
    }

    public void addDevice(Map<GenericDevice, Integer> device) {
        for (GenericDevice genericDevice : device.keySet()) {
            if (DeviceLibrary.INSTANCE.getGenericInputDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addSensor(genericDevice);
                }
            } else if (DeviceLibrary.INSTANCE.getGenericOutputDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addActuator(genericDevice);
                }
            } else if (DeviceLibrary.INSTANCE.getGenericConnectivityDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addConnectivity(genericDevice);
                }
            } else {
                throw new IllegalStateException("We are in great danger!!!");
            }
        }
    }

}
