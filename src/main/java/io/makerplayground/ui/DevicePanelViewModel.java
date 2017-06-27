package io.makerplayground.ui;

import io.makerplayground.project.Project;
import io.makerplayground.project.ProjectDevice;
import io.makerplayground.uihelper.DynamicViewModelCreator;
import javafx.collections.ObservableList;

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
        this.inputChildViewModel = new DynamicViewModelCreator<>(project.getInputDevice(), device -> new DevicePanelIconViewModel(device));
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

    public void addDevice(ObservableList<ControlAddDevicePane> device) {
        for (ControlAddDevicePane d : device) {
            for (int i = 0; i < d.getCount(); i++) {
                project.addOutputDevice(d.getGenericDevice());
            }
        }
    }

}
