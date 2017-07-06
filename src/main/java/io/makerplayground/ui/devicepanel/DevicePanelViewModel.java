package io.makerplayground.ui.devicepanel;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.GenericDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import javafx.collections.ObservableList;

import java.util.Map;

/**
 *
 * Created by Nuntipat Narkthong on 6/6/2017 AD.
 */
public class DevicePanelViewModel {
    private final Project project;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> inputChildViewModel;
    private final DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> outputChildViewModel;

    public DevicePanelViewModel(Project project) {
        this.project = project;
        this.inputChildViewModel = new DynamicViewModelCreator<>(project.getInputDevice(), DevicePanelIconViewModel::new);
        this.outputChildViewModel = new DynamicViewModelCreator<>(project.getOutputDevice(), DevicePanelIconViewModel::new);
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getInputChildViewModel() {
        return inputChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getOutputChildViewModel() {
        return outputChildViewModel;
    }

    public boolean removeDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();
        return project.removeOutputDevice(deviceToBeRemoved);
    }

    public void addDevice(Map<GenericDevice, Integer> device) {
        for (GenericDevice genericDevice : device.keySet()) {
            if (DeviceLibrary.INSTANCE.getInputDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addInputDevice(genericDevice);
                }
            } else if (DeviceLibrary.INSTANCE.getOutputDevice().contains(genericDevice)) {
                for (int i = 0; i < device.get(genericDevice); i++) {
                    project.addOutputDevice(genericDevice);
                }
            } else {
                throw new IllegalStateException("We are in great danger!!!");
            }
        }
    }

}
