package io.makerplayground.ui;

import io.makerplayground.device.DeviceLibrary;
import io.makerplayground.device.InputDevice;
import io.makerplayground.device.OutputDevice;
import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.uihelper.DynamicViewModelCreator;

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
        this.inputChildViewModel = new DynamicViewModelCreator<>(project.getInputDevice(), (ProjectDevice device) -> {
            return new DevicePanelIconViewModel(device);
        });
        this.outputChildViewModel = new DynamicViewModelCreator<>(project.getOutputDevice(), device -> {
            return new DevicePanelIconViewModel(device);
        });
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getInputChildViewModel() {
        return inputChildViewModel;
    }

    public DynamicViewModelCreator<ProjectDevice, DevicePanelIconViewModel> getOutputChildViewModel() {
        return outputChildViewModel;
    }

    public boolean removeDevice(DevicePanelIconViewModel device) {
        ProjectDevice deviceToBeRemoved = device.getDevice();

        // TODO: avoid this cast if possible
        if (deviceToBeRemoved.getDevice() instanceof InputDevice) {
            return project.removeInputDevice(deviceToBeRemoved);
        } else if (deviceToBeRemoved.getDevice() instanceof OutputDevice) {
            return project.removeOutputDevice(deviceToBeRemoved);
        } else {
            return false;
        }
    }

    public void addDevice()
    {
        project.addOutputDevice(DeviceLibrary.INSTANCE.getOutputDevice("led"));
    }

}
